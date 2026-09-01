package com.bantads.msauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "com.bantads")
@EnableMongoRepositories(basePackages = "com.bantads.repository")
public class MsauthApplication 
{
    public static void main(String[] args) 
    {
        SpringApplication.run(MsauthApplication.class, args);
    }
}