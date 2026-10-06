package com.bantads.orchestration.services;

import java.io.Serializable;
import java.time.Duration; 
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.bantads.orchestration.model.StepDefinition;
import org.jspecify.annotations.NonNull;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.bantads.orchestration.config.RabbitMQConfig;
import com.bantads.orchestration.model.SagaStates;
import com.bantads.orchestration.model.SagaStatus;
import com.bantads.orchestration.model.SagaSteps;

@Service 
public class OrchestrationService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void startSaga(SagaSteps step, Map<String, Object> payload) {
        final UUID sagaId = UUID.randomUUID();

        SagaStates sagaStates = SagaStates.builder()
                .sagaId(sagaId)
                .status(SagaStatus.EM_ANDAMENTO)
                .step(step)
                .stepIndex(0)
                .payload(payload)
                .build();

        redisTemplate.opsForValue().set("saga:" + sagaId, sagaStates);
        this.publishSagaCmd(sagaStates);
    }

    public void publishSagaCmd(@NonNull SagaStates sagaStates) {
        Map<String, Object> cmd = sagaStates.getPayload();

        cmd.put("sagaId", sagaStates.getSagaId());
        cmd.put("stepIndex", sagaStates.getStepIndex());
        cmd.put("type", sagaStates.getType());

        int attempt = (int) cmd.getOrDefault("attempt", 0);
        cmd.put("attempt", attempt);

        System.out.println(cmd);
        System.out.println(cmd.get("idSolicitacao"));

        String queue = sagaStates.currentStep();
        rabbitTemplate.convertAndSend(queue, cmd);

        Map<String, Object> timeoutToken = new HashMap<>();
        timeoutToken.put("sagaId", sagaStates.getSagaId());
        timeoutToken.put("stepIndex", sagaStates.getStepIndex()); 
        
        rabbitTemplate.convertAndSend(queue + ".wait", timeoutToken);
    }

    @RabbitListener(queues = RabbitMQConfig.ORQUESTRADOR_REPLY)
    public void handleReply(Map<String, Object> reply) {
        UUID sagaId = UUID.fromString(reply.get("sagaId").toString());
        String sagaKey = "saga:" + sagaId;
        SagaStates sagaStates = (SagaStates) redisTemplate.opsForValue().get(sagaKey);

        if (sagaStates == null) return;

        String status = reply.get("status").toString();
        int attempt = (int) reply.get("attempt");
        int stepIndex = Integer.parseInt(reply.get("stepIndex").toString()); 

        if (sagaStates.getStatus() == SagaStatus.COMPENSACAO || sagaStates.getStatus() == SagaStatus.FALHA) {
            if ("SUCESSO".equals(status)) {
                compensateSingleStep(sagaStates, stepIndex);
            }
            return;
        }

        if ("SUCESSO".equals(status)) {
            if (sagaStates.isLastStep()) {
                sagaStates.setStatus(SagaStatus.CONCLUIDO);
                redisTemplate.opsForValue().set(sagaKey, sagaStates);
                redisTemplate.expire(sagaKey, Duration.ofHours(24)); 
            } else {
                sagaStates.setStepIndex(sagaStates.getStepIndex() + 1);
                redisTemplate.opsForValue().set(sagaKey, sagaStates);
                publishSagaCmd(sagaStates);
            }
        } else if ("FALHA".equals(status)) {
            if(attempt < 3) {
                reply.put("attempt", attempt  + 1);
                sagaStates.setStepIndex(sagaStates.getStepIndex());
                redisTemplate.opsForValue().set(sagaKey, sagaStates);
                publishSagaCmd(sagaStates);
            } else {
                sagaStates.setStatus(SagaStatus.COMPENSACAO);
                sagaStates.setError(reply.getOrDefault("error", "ERRO_DESCONHECIDO").toString());
                redisTemplate.opsForValue().set(sagaKey, sagaStates);
                compensate(sagaStates);
            }
        }
    }

    @RabbitListener(queues = RabbitMQConfig.ORQUESTRADOR_TIMEOUT)
    public void handleTimeout(Map<String, Object> timedOut) {
        UUID sagaId = UUID.fromString(timedOut.get("sagaId").toString());
        int timeoutStepIndex = Integer.parseInt(timedOut.get("stepIndex").toString());

        String sagaKey = "saga:" + sagaId;
        SagaStates sagaStates = (SagaStates) redisTemplate.opsForValue().get(sagaKey);

        if (sagaStates == null) return;

        if (sagaStates.getStatus() != SagaStatus.EM_ANDAMENTO || sagaStates.getStepIndex() != timeoutStepIndex) {
            return;
        }

        sagaStates.setStatus(SagaStatus.COMPENSACAO);
        sagaStates.setError("TIMEOUT");
        redisTemplate.opsForValue().set(sagaKey, sagaStates);
        compensate(sagaStates);
    }

    public void compensate(SagaStates sagaStates) {  
        for (int i = sagaStates.getStepIndex(); i >= 0; i--) {
            compensateSingleStep(sagaStates, i);
        }

        sagaStates.setStatus(SagaStatus.FALHA);
        sagaStates.setError("COMPENSACAO");
        
        String sagaKey = "saga:" + sagaStates.getSagaId();
        redisTemplate.opsForValue().set(sagaKey, sagaStates);
        redisTemplate.expire(sagaKey, Duration.ofHours(24));
    }

    private void compensateSingleStep(SagaStates sagaStates, int stepIndex) {
        if (stepIndex < 0 || stepIndex >= sagaStates.getStep().getSteps().size()) return;

        StepDefinition step = sagaStates.getStep().getSteps().get(stepIndex);
        String queue = step.getQueue() + ".dql";

        Map<String, Object> comp = new HashMap<>();
        comp.put("sagaId", sagaStates.getSagaId());
        comp.put("stepIndex", stepIndex);
        comp.put("status", "COMPENSACAO");
        comp.put("payload", sagaStates.getPayload());

        rabbitTemplate.convertAndSend(queue, comp);
    }

}