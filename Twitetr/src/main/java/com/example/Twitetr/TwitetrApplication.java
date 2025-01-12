package com.example.Twitetr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TwitetrApplication {
	/*
	 * Detta är huvudmetoden som kör backend och initierar spring ramverket
	 */
	public static void main(String[] args) {
		System.out.println("Current working directory: " + System.getProperty("user.dir"));

		SpringApplication.run(TwitetrApplication.class, args);
			
	}

}
