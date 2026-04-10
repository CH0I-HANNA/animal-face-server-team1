package com.likelion.animalface;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AnimalFaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnimalFaceApplication.class, args);
	}

}
