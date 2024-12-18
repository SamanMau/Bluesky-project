package com.example.Twitetr.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        HashMap<String, Object> errorResponse = new HashMap<>();

        // Kontrollera input
        if (text == null || text.isEmpty() || language == null || language.isEmpty()) {
            errorResponse.put("error", "Text eller språk saknas.");
            return errorResponse;
        }

        try {
            // Bygg URL med kodad text
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = LIBRIS_API_URL
                    .replace("{text}", encodedText)
                    .replace("{lang}", language)
                    .replace("{apiKey}", API_KEY);

            // Skicka GET-förfrågan
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            // Kontrollera om API returnerade HTML
            if (response.getHeaders().getContentType() != null &&
                    response.getHeaders().getContentType().toString().contains("text/html")) {
                errorResponse.put("error", "LIBRIS API returnerade en oväntad HTML-sida. Kontrollera API-nyckel och förfrågan.");
                return errorResponse;
            }

            // Om allt ser bra ut, konvertera JSON-svaret till HashMap
            ObjectMapper objectMapper = new ObjectMapper();
            HashMap<String, Object> responseBody = objectMapper.readValue(response.getBody(), HashMap.class);
            return responseBody;

        } catch (Exception e) {
            e.printStackTrace();
            errorResponse.put("error", "Kunde inte kommunicera med LIBRIS API. Det kan bero på nätverksfel eller ogiltiga parametrar.");
            return errorResponse;
        }
    }
}