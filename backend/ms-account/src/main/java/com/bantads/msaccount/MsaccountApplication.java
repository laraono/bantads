package com.bantads.msaccount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.bantads")
@EntityScan(basePackages = "com.bantads.entity")
public class MsaccountApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsaccountApplication.class, args);
	}

}
