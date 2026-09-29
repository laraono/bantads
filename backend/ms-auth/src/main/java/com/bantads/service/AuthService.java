package com.bantads.service;
import com.bantads.config.RabbitMQConfig;
import com.bantads.entity.Auth;
import com.bantads.entity.Request;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.*;

@Service
public class AuthService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RequestRepository requestRepository;

    private final AuthRepository authRepository;

    public AuthService(AuthRepository authRepository) 
    {
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

        boolean alreadyProcessed = requestRepository.existsBySagaIdAndType(command.getSagaId(), command.getType());

        if (alreadyProcessed) {
            return;
        }

        RabbitAnswer answer = RabbitAnswer.builder()
                .sagaId(command.getSagaId())
                .type(command.getType())
                .status(Status.SUCESSO)
                .build();

        Request req = new Request();

        req.setSagaId(command.getSagaId());
        req.setType(command.getType());
        req.setId(new ObjectId());

        String type = String.valueOf(command.getType());

        Map<String, Object> payload = new HashMap<>();

        if(type.contains("criar-auth")) {
            try {
                Map<String, String> request = (Map<String, String>) payload.get("requsicao");
                String password = this.create(
                        request.get("id"),
                        request.get("cpf"),
                        request.get("tipo"),
                        request.get("email"),
                        request.get("senha"),
                        true
                );

                Map<String, String> email = new HashMap<>();

                email.put("email", request.get("email"));
                email.put("senha", password);

                payload.put("email", email);

            } catch (Exception e) {
                answer.setStatus(Status.FALHA);
                answer.setTimestamp(new Date());
                e.printStackTrace();
                throw e;
            }
        }

        answer.setPayload(payload);
        answer.setTimestamp(new Date());

        this.sendReadModelCommand(answer);
        requestRepository.save(req);
    }

    public void sendReadModelCommand(RabbitAnswer answer) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORQUESTRADOR_QUEUE, answer);
    }
}