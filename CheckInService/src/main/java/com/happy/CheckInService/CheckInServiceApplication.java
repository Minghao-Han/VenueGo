package com.happy.CheckInService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CheckInServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CheckInServiceApplication.class, args);
	}

}
