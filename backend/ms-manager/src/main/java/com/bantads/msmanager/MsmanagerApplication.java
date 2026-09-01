package com.bantads.msmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.bantads")
public class MsmanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsmanagerApplication.class, args);
	}

}
