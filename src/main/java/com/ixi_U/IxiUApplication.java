package com.ixi_U;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class IxiUApplication {

	public static void main(String[] args) {
		SpringApplication.run(IxiUApplication.class, args);
	}

}
