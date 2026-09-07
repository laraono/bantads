package com.bantads.msmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication(scanBasePackages = "com.bantads")
@EnableJpaRepositories(basePackages = "com.bantads.repository")
@EntityScan("com.bantads.entity")
public class MsmanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsmanagerApplication.class, args);
	}

}
