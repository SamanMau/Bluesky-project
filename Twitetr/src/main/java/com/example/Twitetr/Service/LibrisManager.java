package com.example.Twitetr.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.HashMap;

@Service
public class LibrisManager {
    private static final String LIBRIS_API_URL = "http://api.libris.kb.se/bibspell/spell?query=%s&key=%s";

    public LibrisManager(){

    }

    public HashMap<String, Object> checkSpelling(String userInput, String specified_language) {
        RestTemplate restTemplate = new RestTemplate();
        
        HashMap<String, Object> map = new HashMap<>();
        String URL = String.format(LIBRIS_API_URL, URLEncoder.encode(userInput, StandardCharsets.UTF_8), key);
        
        System.out.println("LIBRIS API URL: " + URL);
    
        try {
            // Skickar GET-förfrågan
            ResponseEntity<String> response = restTemplate.getForEntity(URL, String.class);
            
            // Logga svaret från API
            System.out.println("LIBRIS API Response: " + response.getBody());
    
            // Om du vill granska HTTP-statuskoden
            System.out.println("HTTP Status Code: " + response.getStatusCode());
    
            // Processera API-svaret
            String result = response.getBody();
            if (result != null) {
                map = parseJSON(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            map.put("invalid", "LIBRIS API-fel: " + e.getMessage());
        }
    
        return map;
    }

    public boolean verifyApiKey(){
        String key = System.getenv("LIBRIS_API_NYCKEL");

        if(key != null){
            return true;
        }

        if(!key.isEmpty()){
            return true;
        }

        return false;
    }
}