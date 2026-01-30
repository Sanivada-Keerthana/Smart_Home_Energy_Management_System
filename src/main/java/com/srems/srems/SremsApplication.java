package com.srems.srems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SremsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SremsApplication.class, args);
	}

}
