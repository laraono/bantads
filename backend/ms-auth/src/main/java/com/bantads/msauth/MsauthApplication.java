package com.bantads.msauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.bantads")
public class MsauthApplication 
{
    public static void main(String[] args) 
    {
        SpringApplication.run(MsauthApplication.class, args);
    }
}