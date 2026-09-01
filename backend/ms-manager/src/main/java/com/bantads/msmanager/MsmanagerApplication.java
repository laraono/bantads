package com.bantads.msmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.bantads")
@EnableJpaRepositories(basePackages = "com.bantads.repository")
public class MsmanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsmanagerApplication.class, args);
	}

}
