package com.example.forgetPassword;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ForgetPasswordApplication {

	public static void main(String[] args) {
		SpringApplication.run(ForgetPasswordApplication.class, args);
	}

}
