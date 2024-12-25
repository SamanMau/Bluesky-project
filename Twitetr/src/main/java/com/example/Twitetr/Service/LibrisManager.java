package com.example.Twitetr.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;

@Service
public class LibrisManager {
    private static final String LIBRIS_API_URL = "http://api.libris.kb.se/bibspell/spell?query=%s&key=%s";

    public LibrisManager(){

    }

    public HashMap<String, Object> checkSpelling(String userInput, String specified_language) {
        RestTemplate restTemplate = new RestTemplate();
        
        HashMap<String, Object> map = new HashMap<>();
        String key = getKey();
        String URL = String.format(LIBRIS_API_URL, URLEncoder.encode(userInput, StandardCharsets.UTF_8), key);
        
        /*
        System.out.println("LIBRIS API URL: " + URL);
        System.out.println("nyckel: "+ key);
        */
    
        try {
            // get förfrågan skickas
            ResponseEntity<String> response = restTemplate.getForEntity(URL, String.class);
            
            String result = response.getBody();

            System.out.println("LIBRIS API Response: " + result);
    
            System.out.println("HTTP Status Code: " + result);
    
            if(result != null){
                XmlMapper xml = new XmlMapper();
                map = xml.readValue(result, HashMap.class);
            } else {
                map.put("Invalid", "LIBRIS returned an empty response");
            }

        } catch (Exception e) {
            e.printStackTrace();
            map.put("invalid", "LIBRIS API-fel: " + e.getMessage());
        }
        
        return map;
    }

    public String getKey(){
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .filename(".env")
                .load();

        String key = dotenv.get("LIBRIS_API_NYCKEL");
        return key;
    }

}