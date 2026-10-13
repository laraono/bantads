package com.bantads.service;
import com.bantads.config.RabbitMQConfig;
import com.bantads.entity.Auth;
import com.bantads.entity.Request;
import com.bantads.handlers.SagaStepHandler;
import com.bantads.model.RabbitAnswer;
import com.bantads.model.RabbitRequest;
import com.bantads.model.Status;
import com.bantads.repository.AuthRepository;
import com.bantads.repository.RequestRepository;
import com.password4j.Password;

import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    private final AuthRepository authRepository;

    private final Map<String, SagaStepHandler> handlers;

    public AuthService(AuthRepository authRepository, List<SagaStepHandler> handlersToInject) 
    {
        this.handlers = handlersToInject.stream()
                .collect(Collectors.toMap(SagaStepHandler::action, h -> h));
        this.authRepository = authRepository;
    }

    public Auth authenticate(String login, String password) 
    {

        Auth auth = authRepository.findByLogin(login).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"NÃO AUTORIZADO"));

        if (!Boolean.TRUE.equals(auth.getActive())) 
            {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"NÃO AUTORIZADO"
            );
        }

        boolean passwordValid = Password.check(password, auth.getPassword()).withArgon2();

        if (!passwordValid) 
        {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"NÃO AUTORIZADO");
        }

        return auth;
    }

    public String create(
            String userId,
            String cpf,
            String type,
            String login,
            String rawPassword,
            Boolean active
    ) {

        if (authRepository.existsByLogin(login)) 
        {
            throw new ResponseStatusException( HttpStatus.CONFLICT,"CONTA JÁ EXISTE");
        }

        Auth auth = new Auth();

        if (userId != null) {
            auth.setUserId(userId);
        } else {
            auth.setUserId(UUID.randomUUID().toString());
        }
        auth.setCpf(cpf);
        auth.setType(type);
        auth.setLogin(login);
        auth.setActive(active);

        String password = rawPassword.isEmpty() ? generateRandomPassword() : rawPassword;

        String hash = Password.hash(password).withArgon2().getResult();

        auth.setPassword(hash);

        return password;

    }

    public void invalidateAuth(String cpf) {
        Auth auth = authRepository.findByCpf(cpf);

        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }

        auth.setActive(false);
        authRepository.save(auth);

        redisTemplate.opsForValue().set("bloqueado:" + cpf, "1").block();
    }

    public List<Auth> findAll() 
    {
        return authRepository.findAll();
    }

    public static String generateRandomPassword() {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 6; i++)
        {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }

        return sb.toString();
    }


    @RabbitListener(queues = RabbitMQConfig.AUTH_QUEUE)
    public void handleAccountCommand(RabbitRequest command) {

        boolean is_compensation = command.isCompensation();

        String action = command.getType();

        if (!this.handlers.containsKey(action)) {
            throw new RuntimeException("Handler não encontrado para a ação " + action);
        }

        SagaStepHandler handler = this.handlers.get(action);

        Map<String, Object> payload = new HashMap<>();

        Status status = Status.SUCESSO;
        String error = null;
        try {
            if (command.isCompensation()) {
                handler.compensate(command.getPayload());
            } else {
                payload = handler.execute(command.getPayload());
                status = Status.SUCESSO;
            }
        } catch (Exception e) {
            status = Status.FALHA;
            error = e.getMessage();
        }
        Request req = Request.builder()
                .sagaId(command.getSagaId())
                .type(command.getType())
                .compensation(command.isCompensation())
                .build();

        RabbitAnswer answer = RabbitAnswer.builder()
                .sagaId(command.getSagaId())
                .type(command.getType())
                .error(error)
                .status(status)
                .stepIndex(command.getStepIndex())
                .build();

        answer.setPayload(payload);
        answer.setTimestamp(new Date());

        this.sendReadModelCommand(answer);
        requestRepository.save(req);
    }

    public void sendReadModelCommand(RabbitAnswer answer) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORQUESTRADOR_QUEUE, answer);
    }
}