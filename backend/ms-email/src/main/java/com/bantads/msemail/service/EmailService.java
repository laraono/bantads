package com.bantads.msemail.service;

import com.bantads.msemail.config.RabbitMQConfig;
import com.bantads.msemail.model.RabbitRequest;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service 
public class EmailService {
    
    @Autowired 
    private JavaMailSender javaMailSender;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }

    @RabbitListener(queues = RabbitMQConfig.MS_EMAIL_CMD)
    public void handleAccountCommand(RabbitRequest command) {

        String type = String.valueOf(command.getType());

        Map<String, Object> payload = new HashMap<>();

        if(type.contains("enviar-senha")) {
            try {


                Map<String, String> emailObject = (Map<String, String>) command.getPayload().get("email");
                String email = emailObject.get("email");
                String password = emailObject.get("senha");

                this.sendEmail(email, "Sua nova senha", password);

            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

    }


}
