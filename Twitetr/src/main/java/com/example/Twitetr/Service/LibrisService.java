package com.example.Twitetr.Service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

 @Service
public class LibrisService {
    private static final String LIBRIS_API_URL = "https://api.libris.kb.se/bibspell?text={text}&lang={lang}&key={apiKey}";
    private static final String API_KEY = "DIN_API_NYCKEL";


    public HashMap<String, Object> checkSpelling(String text, String language) {
        RestTemplate restTemplate = new RestTemplate();

        if (text == null || text.isEmpty() || language == null || language.isEmpty()) {
            HashMap<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Text eller språk saknas.");
            return errorResponse;
        }

        try {

            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);

            String url = LIBRIS_API_URL
                    .replace("{text}", encodedText)
                    .replace("{lang}", language)
                    .replace("{apiKey}", API_KEY);


            ResponseEntity<HashMap> response = restTemplate.getForEntity(url, HashMap.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();

            HashMap<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "kunde inte kommunicera med LIBRIS API.");
            return errorResponse;
        }

    }

}
