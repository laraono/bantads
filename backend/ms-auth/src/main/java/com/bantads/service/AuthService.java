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
    public void handleAuthQueue(Map<String, Object> command) {

        UUID sagaId = UUID.fromString(command.get("sagaId").toString());
        String type = command.get("type").toString();

        boolean alreadyProcessed = requestRepository.existsBySagaIdAndType(sagaId, type);
        if (alreadyProcessed) {
            return;
        }

        Request req = new Request();

        req.setSagaId(sagaId);
        req.setType(type);
        req.setId(new ObjectId());

        command.put("status", Status.SUCESSO);
        command.put("timestamp", new Date());

        if(type.contains("criar-auth")) {
            try {
                Map<String, String> request = (Map<String, String>) command.get("requsicao");
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

                command.put("email", email);

                requestRepository.save(req);

            } catch (Exception e) {
                command.put("status", Status.FALHA);
                command.put("timestamp", new Date());
                e.printStackTrace();
                throw e;
            }
        }

        this.sendReadModelCommand(command);
    }

    @RabbitListener(queues = RabbitMQConfig.AUTH_QUEUE_DLQ)
    public void handleDlqQueue(Map<String, Object> payload) {

        UUID sagaId = UUID.fromString(payload.get("sagaId").toString());
        String type = payload.get("type").toString();

        boolean alreadyProcessed = requestRepository.existsBySagaIdAndType(sagaId, type);
        if (alreadyProcessed) {
            return;
        }

        Request req = new Request();

        req.setSagaId(sagaId);
        req.setType(type);
        req.setId(new ObjectId());


        if(type.contains("criar-auth")) {
            try {
                Map<String, String> request = (Map<String, String>) payload.get("requsicao");
                String login = request.get("email");

                this.authRepository.deleteByLogin(login);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        requestRepository.save(req);
    }

    public void sendReadModelCommand(Map<String, Object> answer) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORQUESTRADOR_QUEUE, answer);
    }
}