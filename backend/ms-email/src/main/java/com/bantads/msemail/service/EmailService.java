package com.bantads.msemail.service;

import com.bantads.msemail.config.RabbitMQConfig;
import com.bantads.msemail.model.RabbitRequest;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        javaMailSender.send(message);
    }

    @RabbitListener(queues = RabbitMQConfig.MS_EMAIL_CMD)
    public void handleAccountCommand(Map<String, Object> command) {

        String type = String.valueOf(command.get("type"));
        Map<String, String> emailObject = (Map<String, String>) command.get("email");

        if(type.contains("enviar-senha")) {
            try {
                String clientEmail = emailObject.get("email");
                String password = emailObject.get("senha");

                this.sendEmail(clientEmail, "Sua nova senha", password);

            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        if(type.contains("novo-gerente")) {
            try {
                Map<String, String> gerente = (Map<String, String>) command.get("gerente");
                String managerName = gerente.get("nome");
                String clientEmail = emailObject.get("email").toString();
                String clientName = emailObject.get("nome").toString();

                this.sendEmail(clientEmail, "Novo gerente", "Olá, " + clientName + "Seu novo gerente é:  " + managerName);

            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        if (type.contains("enviar-rejeicao")) {
            String text = "Olá " + emailObject.get("nome") + ", sua solicitação de cadastro foi rejeitada.";
            if (emailObject.get("motivo") != null) {
                text += " Motivo: " + emailObject.get("motivo");
            }
            this.sendEmail(emailObject.get("email"), "Solicitação rejeitada", text);
        }

    }


}
