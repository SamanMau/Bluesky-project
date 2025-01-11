package com.example.Twitetr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
	public static void main(String[] args) {
		System.out.println("Current working directory: " + System.getProperty("user.dir"));

		SpringApplication.run(Main.class, args);
			
	}

}
