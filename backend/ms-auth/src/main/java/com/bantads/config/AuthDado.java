package com.bantads.config;

import com.bantads.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthDado 
{
    @Bean
    CommandLineRunner initAuthData (AuthService authService) 
    {
        return args -> {
            String password = "123456";
            authService.create
            (
                "12912861012",
                "CLIENTE",
                "cli1@bantads.com.br",
                password,
                true
            );

            authService.create
            (
                    "09506382000",
                    "CLIENTE",
                    "cli2@bantads.com.br",
                    password,
                    true
            );

            authService.create
            (
                    "85733854057",
                    "CLIENTE",
                    "cli3@bantads.com.br",
                    password,
                    true
            );

            authService.create
            (
                    "58872160006",
                    "CLIENTE",
                    "cli4@bantads.com.br",
                    password,
                    true
            );

            authService.create
            (
                    "76179646090",
                    "CLIENTE",
                    "cli5@bantads.com.br",
                    password,
                    true
            );

            authService.create
            (
                    "98574307084",
                    "GERENTE",
                    "ger1@bantads.com.br",
                    password,
                    true
            );

            authService.create
            (
                    "64065268052",
                    "GERENTE",
                    "ger2@bantads.com.br",
                    password,
                    true
            );

            authService.create
            (
                    "23862179060",
                    "GERENTE",
                    "ger3@bantads.com.br",
                    password,
                    true
            );

            authService.create
            (
                    "40501740066",
                    "GERENTE",
                    "ger4@bantads.com.br",
                    password,
                    true
            );
        };
    }
}