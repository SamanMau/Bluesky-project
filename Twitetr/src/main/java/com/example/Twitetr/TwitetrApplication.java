package com.example.Twitetr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.Twitetr.Service.LibrisManager;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class TwitetrApplication {
	public static void main(String[] args) {
		System.out.println("Current working directory: " + System.getProperty("user.dir"));

		Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .filename(".env")
                .load();

				/* 
				LibrisManager librisManager = new LibrisManager();
				if(!librisManager.verifyApiKey()){
					System.err.println("API-nyckel saknas eller är ogiltig. Kontrollera .env-filen.");
				}
					*/

   		
		SpringApplication.run(TwitetrApplication.class, args);
			
	}

}
