package com.example.Twitetr.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@Service
public class LibrisManager {
    private static final String LIBRIS_API_URL = "https://api.libris.kb.se/bibspell?text={text}&lang={lang}&key={apiKey}";



    public HashMap<String, Object> checkSpelling(String userInput, String specified_language) {
        RestTemplate restTemplate = new RestTemplate();
        HashMap<String, Object> map = new HashMap<>();

        if(specified_language.isEmpty()){
            specified_language = "sv";
        }

        // Kontrollera input
        if (userInput.isEmpty()) {
            map.put("invalid", "Text saknas.");
            return map;
        }

        try {
            String key = System.getenv("LIBRIS_API_NYCKEL");
            String URL = "http://api.libris.kb.se/bibspell/spell?query=" +
             URLEncoder.encode(userInput, StandardCharsets.UTF_8) +
             "&key=6AB126D40FB02C823F1AECAD8396E96A";

            // skicka GET-förfrågan
            ResponseEntity<String> response = restTemplate.getForEntity(URL, String.class);

            ObjectMapper oM = new ObjectMapper();

         //   HashMap<String, Object> newMap = oM.readValue(response.getBody(), HashMap.class);

            HashMap<String, Object> newMap = new ObjectMapper().readValue(response.getBody(), HashMap.class);

            HashMap<String, Object> hashMap = newMap;

            return hashMap;
          
        } catch (Exception e) {
            e.printStackTrace();
            map.put("invalid", "Fel uppstod vid kommunikation med LIBRIS. ");
            return map;
        }
    }
}