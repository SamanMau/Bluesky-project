package com.example.Twitetr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class TwitetrApplication {

	//Klassen kör servern.
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .filename(".env")
                .load();
				System.out.println("Loaded API Key: " + dotenv.get("LIBRIS_API_NYCKEL"));
   		
		SpringApplication.run(TwitetrApplication.class, args);

	//	System.out.println("Loaded API Key: " + System.getenv("LIBRIS_API_NYCKEL"));

			
	}

}
