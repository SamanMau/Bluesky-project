package com.example.Twitetr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.Twitetr.Service.LibrisManager;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class TwitetrApplication {

	//Klassen kör servern.
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .filename(".env")
                .load();

				String apiKey = dotenv.get("LIBRIS_API_NYCKEL");

				LibrisManager librisManager = new LibrisManager();
				if(!librisManager.verifyApiKey()){
					System.err.println("API-nyckel saknas eller är ogiltig. Kontrollera .env-filen.");
				}

   		
		SpringApplication.run(TwitetrApplication.class, args);
			
	}

}
