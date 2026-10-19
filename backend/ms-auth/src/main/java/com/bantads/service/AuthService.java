package com.bantads.service;

import com.bantads.entity.Auth;
import com.bantads.repository.AuthRepository;
import com.password4j.Password;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.*;

@Service
public class AuthService {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    private final AuthRepository authRepository;

    public AuthService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public Auth authenticate(String login, String password) {

        Auth auth = authRepository.findByLogin(login)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "NÃO AUTORIZADO"));

        if (!Boolean.TRUE.equals(auth.getActive())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "NÃO AUTORIZADO");
        }

        boolean passwordValid = Password.check(password, auth.getPassword()).withArgon2();

        if (!passwordValid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "NÃO AUTORIZADO");
        }

        return auth;
    }

    public String create(
            String cpf,
            String type,
            String login,
            String rawPassword,
            Boolean active
    ) {

        if (authRepository.existsByLogin(login)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CONTA JÁ EXISTE");
        }

        Auth auth = new Auth();

        auth.setUserId(UUID.randomUUID().toString());

        auth.setCpf(cpf);
        auth.setType(type);
        auth.setLogin(login);
        auth.setActive(active);

        String password = rawPassword.isEmpty() ? generateRandomPassword() : rawPassword;

        String hash = Password.hash(password).withArgon2().getResult();

        auth.setPassword(hash);

        authRepository.save(auth);

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

    public List<Auth> findAll() {
        return authRepository.findAll();
    }

    public static String generateRandomPassword() {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }

        return sb.toString();
    }
}
