package com.bantads.msemail.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bantads.msemail.dto.EmailRequestDTO;
import com.bantads.msemail.service.EmailService;

// Mantive apenas para testes, não é necessário para o funcionamento do sistema
@Controller 
@RequestMapping ("/email")
public class EmailController {
    
    @Autowired 
    private EmailService emailService;

    @PostMapping ("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequestDTO request) {
        try{
            emailService.sendEmail(request.getTo(), request.getSubject(), request.getMessage());
            return ResponseEntity.ok("Email enviado com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao enviar email");
        }

    }
}
