package com.bantads.controller;

import com.bantads.entity.Auth;
import com.bantads.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

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
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request
    ) 
    {
        String login = request.get("login");
        String password = request.get("password");
        Auth auth = authService.authenticate(login, password);
        Map<String, Object> response = new HashMap<>();
        response.put("auth", true);
        response.put("cpf", auth.getCpf());
        response.put("tipo", auth.getType());
        response.put("login", auth.getLogin());
        return ResponseEntity.ok(response);
    }
    @PostMapping
    public Auth createAuth(@RequestBody Auth auth) 
    {
        return authService.create(
                auth.getCpf(),
                auth.getType(),
                auth.getLogin(),
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