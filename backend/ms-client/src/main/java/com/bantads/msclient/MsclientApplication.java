package com.bantads.msclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.bantads")
@EnableJpaRepositories(basePackages = "com.bantads.repository")
public class MsclientApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsclientApplication.class, args);
	}

}
