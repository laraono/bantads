package com.bantads.controller;

import com.bantads.dto.LoginDTO;
import com.bantads.entity.Auth;
import com.bantads.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController 
{

    private final AuthService authService;

    public AuthController(AuthService authService) 
    {
        this.authService = authService;
    }

    @GetMapping("/ping")
    public String ping() 
    {
        return "pong";
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody @Valid LoginDTO login) 
    {

        String email = login.getEmail();
        String password = login.getSenha();

        Auth auth = authService.authenticate(email, password);

        Map<String, Object> response = new HashMap<>();

        response.put("auth", true);
        response.put("cpf", auth.getCpf());
        response.put("tipo", auth.getType());
        response.put("login", auth.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public void createAuth(@RequestBody Auth auth)
    {
        authService.create(
                auth.getCpf(),
                auth.getType(),
                auth.getEmail(),
                auth.getPassword(),
                auth.getActive()
        );
    }

    @GetMapping
    public Iterable<Auth> listAuths() 
    {
        return authService.findAll();
    }
}