package com.bantads.orchestration.services;

import java.time.Duration; 
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.bantads.orchestration.model.*;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.bantads.orchestration.config.RabbitMQConfig;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service 
public class OrchestrationService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final int ATTEMP_MAX = 3;

    public void startSaga(SagaSteps step, Map<String, Object> payload, UUID sagaId) {

        SagaStates sagaStates = SagaStates.builder()
                .sagaId(sagaId)
                .status(SagaStatus.EM_ANDAMENTO)
                .step(step)
                .stepIndex(0)
                .payload(payload)
                .build();

        redisTemplate.opsForValue().set("saga:" + sagaId, sagaStates);

        RabbitRequest request = new RabbitRequest();
        request.setAttempt(0);
        publishSagaCmd(sagaStates, request);
    }

    public void publishSagaCmd(@NonNull SagaStates sagaStates, RabbitRequest request) {
        Map<String, Object> cmd = sagaStates.getPayload();
        cmd.put("compensation", false);

        StepDefinition step = sagaStates.getStep().getSteps().get(sagaStates.getStepIndex());

        request.setSagaId(sagaStates.getSagaId());
        request.setType(step.getActionType());
        request.setPayload(cmd);
        request.setStepIndex(sagaStates.getStepIndex());

        String queue = sagaStates.currentStep();
        rabbitTemplate.convertAndSend(queue, request);

        Map<String, Object> timeoutToken = new HashMap<>();
        timeoutToken.put("sagaId", sagaStates.getSagaId());
        timeoutToken.put("stepIndex", sagaStates.getStepIndex());

        rabbitTemplate.convertAndSend(queue + ".wait", timeoutToken);
    }

    public int dealWithEmail(SagaStates sagaStates, RabbitRequest reply) {
        StepDefinition step = sagaStates.getStep().getSteps().get(sagaStates.getStepIndex());

        if(step.getActionType().contains("email.")) {
            String sagaKey = "saga:" + sagaStates.getSagaId();

            this.publishSagaCmd(sagaStates, reply);

            redisTemplate.opsForValue().set(sagaKey, sagaStates);

            if (sagaStates.isLastStep()) {

                sagaStates.setStatus(SagaStatus.CONCLUIDO);

                redisTemplate.opsForValue().set(sagaKey, sagaStates);
                redisTemplate.expire(sagaKey, Duration.ofHours(24));

                updateJob(sagaStates.getSagaId(), "CONCLUIDO");

                return 0;
            }
            return sagaStates.getStepIndex() + 1;

        }

        return sagaStates.getStepIndex();
    }

    @RabbitListener(queues = RabbitMQConfig.ORQUESTRADOR_REPLY)
    public void handleReply(RabbitRequest reply) {
        UUID sagaId = reply.getSagaId();
        String sagaKey = "saga:" + sagaId;

        Object object = redisTemplate.opsForValue().get(sagaKey);
        SagaStates sagaStates = object == null ? null : objectMapper.convertValue(object, SagaStates.class);

        if (sagaStates == null) {
            log.warn("Saga {} sem estado no Redis (perdido/expirado?); reply '{}' descartado", sagaId, reply.getStatus());
            return;
        }

        String status = reply.getStatus();
        int attempt = reply.getAttempt();

        Map<String, Object> payload = reply.getPayload();

        if (sagaStates.getStatus() == SagaStatus.COMPENSACAO) {
            return;
        }

        if ("SUCESSO".equals(status)) {

            if (sagaStates.isLastStep()) {
                sagaStates.setStatus(SagaStatus.CONCLUIDO);
                sagaStates.setPayload(payload);

                redisTemplate.opsForValue().set(sagaKey, sagaStates);
                redisTemplate.expire(sagaKey, Duration.ofHours(24));

                updateJob(sagaId, "CONCLUIDO");

                sagaStates.setPayload(payload);
                publishSagaCmd(sagaStates, reply);
            } else {
                reply.setAttempt(0);
                if(SagaSteps.INSERIR_GERENTE == sagaStates.getStep() && "conta.identificar-conta".equals(sagaStates.getType())) {
                    String accountNumber = payload.getOrDefault("numeroConta", "sem-conta").toString();
                    
                    if(accountNumber.equals("sem-conta")) {
                        sagaStates.setStatus(SagaStatus.CONCLUIDO);
                        redisTemplate.opsForValue().set(sagaKey, sagaStates);
                        redisTemplate.expire(sagaKey, Duration.ofHours(24));

                        updateJob(sagaId, "CONCLUIDO");
                        return;
                    }
                }

                int nextStep = sagaStates.getStepIndex() + 1;

                sagaStates.setStepIndex(nextStep);
                sagaStates.setPayload(payload);

                StepDefinition step = sagaStates.getStep().getSteps().get(nextStep);

                if(step.getActionType().contains("email.")) {
                    nextStep = this.dealWithEmail(sagaStates, reply);
                }

                if(nextStep > 0) {
                    sagaStates.setStepIndex(nextStep);
                    publishSagaCmd(sagaStates, reply);

                    this.updateRedis(sagaStates);
                }
            }
        } else if ("FALHA".equals(status)) {
            if(attempt < ATTEMP_MAX) {
                if("CONTA JÁ EXISTE".equals(reply.getError())) {

                    this.updateJob(sagaId, "FALHA", reply.getError());

                    sagaStates.setStatus(SagaStatus.COMPENSACAO);
                    sagaStates.setError(reply.getError());

                    compensate(sagaStates);

                    this.updateRedis(sagaStates);
                } else {
                    reply.setAttempt(attempt  + 1);
                    sagaStates.setPayload(payload);
                    sagaStates.setStepIndex(sagaStates.getStepIndex());
                    publishSagaCmd(sagaStates, reply);

                    this.updateRedis(sagaStates);
                }
            } else {
                sagaStates.setStatus(SagaStatus.COMPENSACAO);
                String erro = (reply.getError() == null || reply.getError().isBlank()) ? "ERRO-DESCONHECIDO" : reply.getError();
                sagaStates.setError(erro);

                this.updateJob(sagaId, "FALHA", erro);
                this.updateRedis(sagaStates);

                compensate(sagaStates);
            }
        }
    }

    public void updateRedis(SagaStates sagaStates) {
        Map<String, Object> payload = sagaStates.getPayload();

        payload.put("email", "");
        sagaStates.setPayload(payload);

        redisTemplate.opsForValue().set("saga:" + sagaStates.getSagaId(), sagaStates);
    }

    public void updateJob(UUID sagaId, String status) {
        updateJob(sagaId, status, null);
    }

    public void updateJob(UUID sagaId, String status, String erro) {
        Object jobObject = redisTemplate.opsForValue().get("job-" + sagaId);
        if (jobObject == null) {
            log.warn("Job {} não encontrada no Redis; status '{}' não atualizado", sagaId, status);
            return;
        }
        Map<String, String> job = objectMapper.convertValue(jobObject, Map.class);
        job.put("status", status);
        if (erro != null) {
            job.put("erro", erro);
        }
        redisTemplate.opsForValue().set("job-" + sagaId, job);
    }

    @RabbitListener(queues = RabbitMQConfig.SAGA_CMD)
    public void handleSagaQueue(Map<String, Object> payload) {
        UUID sagaId = UUID.fromString(payload.get("sagaId").toString());

        String type = payload.get("type").toString();

        switch(type) {
            case "CRIACAO_CONTA":
                this.startSaga(SagaSteps.CRIACAO_CONTA, payload, sagaId);
                break;
            case "INSERIR_GERENTE":
                this.startSaga(SagaSteps.INSERIR_GERENTE, payload, sagaId);
                break;
            case "DELETAR_GERENTE":
                this.startSaga(SagaSteps.DELETAR_GERENTE, payload, sagaId);
                break;
            default:
                break;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.ORQUESTRADOR_TIMEOUT)
    public void handleTimeout(Map<String, Object> timedOut) {
        UUID sagaId = UUID.fromString(timedOut.get("sagaId").toString());
        int timeoutStepIndex = Integer.parseInt(timedOut.get("stepIndex").toString());

        String sagaKey = "saga:" + sagaId;
        Object object = redisTemplate.opsForValue().get(sagaKey);
        SagaStates sagaStates = object == null ? null : objectMapper.convertValue(object, SagaStates.class);

        if (sagaStates == null) {
            log.warn("Saga {} sem estado no Redis (perdido/expirado?); timeout do passo {} ignorado", sagaId, timeoutStepIndex);
            return;
        }

        if (sagaStates.getStatus() != SagaStatus.EM_ANDAMENTO || sagaStates.getStepIndex() != timeoutStepIndex) {
            return;
        }

        sagaStates.setStatus(SagaStatus.COMPENSACAO);
        sagaStates.setError("TIMEOUT");
        redisTemplate.opsForValue().set(sagaKey, sagaStates);
        updateJob(sagaId, "FALHA", "TIMEOUT");
        compensate(sagaStates, true);
    }

    public void compensate(SagaStates sagaStates) {
        compensate(sagaStates, false);
    }

    public void compensate(SagaStates sagaStates, boolean includeCurrent) {
        int first = includeCurrent ? sagaStates.getStepIndex() : sagaStates.getStepIndex() - 1; // se ja coloquei dados, ele reverte
        for (int i = first; i >= 0; i--) {
            compensateSingleStep(sagaStates, i);
        }

        sagaStates.setStatus(SagaStatus.FALHA);
        
        String sagaKey = "saga:" + sagaStates.getSagaId();
        redisTemplate.opsForValue().set(sagaKey, sagaStates);
        redisTemplate.expire(sagaKey, Duration.ofHours(24));
    }

    private void compensateSingleStep(SagaStates sagaStates, int stepIndex) {
        if (stepIndex < 0 || stepIndex >= sagaStates.getStep().getSteps().size()) return;

        StepDefinition step = sagaStates.getStep().getSteps().get(stepIndex);
        String queue = step.getQueue() + ".dlq";

        Map<String, Object> comp = sagaStates.getPayload();
        comp.put("compensation", true);

        RabbitRequest request = new RabbitRequest();


        request.setSagaId(sagaStates.getSagaId());
        request.setType(step.getActionType());
        request.setPayload(comp);
        request.setError(sagaStates.getError());
        request.setStepIndex(sagaStates.getStepIndex());

        rabbitTemplate.convertAndSend(queue, request);

        if(sagaStates.isLastStep() && sagaStates.getStep() == SagaSteps.CRIACAO_CONTA) {
            request.setSagaId(sagaStates.getSagaId());
            request.setType("email.enviar-rejeicao");
            request.setPayload(comp);
            request.setStepIndex(sagaStates.getStepIndex());

            rabbitTemplate.convertAndSend(RabbitMQConfig.MS_EMAIL_CMD + ".dlq", request);
        }
    }

}