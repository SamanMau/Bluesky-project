package com.example.Twitetr.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LibrisManager {
    private static final String LIBRIS_API_URL = "http://api.libris.kb.se/bibspell/spell?query=%s&key=%s&format=json";
    //hej

     /*
      * Skickar en förfrågan till LIBRIS API för att kontrollera stavningen av användarens text.
      Det som returneras är en hashmap.
      */
    public HashMap<String, String> checkSpelling(String userInput) {
        RestTemplate restTemplate = new RestTemplate();
        HashMap<String, String> responseMap = new HashMap<>();
        String key = getKey();
        String URL = String.format(LIBRIS_API_URL, URLEncoder.encode(userInput, StandardCharsets.UTF_8), key);
        String correctedWord = "";

        try {
            // skicka get-förfrågan till libris
            ResponseEntity<String> response = restTemplate.getForEntity(URL, String.class);
            String result = response.getBody();

            System.out.println("LIBRIS API svar: " + result);

            if (result != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> jsonMap = objectMapper.readValue(result, Map.class);

                // Hämta det som står innaför "suggestion"s måsvingar.
                Map<String, Object> suggestion = (Map<String, Object>) jsonMap.get("suggestion");
               
                if (suggestion != null) {
                    var terms = (List<Map<String, Object>>) suggestion.get("terms");
                    
                    if (terms != null && !terms.isEmpty()) {
                        correctedWord = (String) terms.get(0).get("value");
                        System.out.println("Här är det korrigerade ordet: " + correctedWord);
                    } else{
                        System.out.println("Tomt svar.");
                    }
                    
                } else{
                    System.out.println("inga suggestions"); //Denna rad verkar exekveras när LIBRIS inte lyckas returnera stavningsförslag.
                }
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }

        responseMap.put("before", userInput);
        responseMap.put("after", correctedWord);

        return responseMap;
    }

    /**
     * Hämtar API-nyckeln från en .env-fil som lagrar känslig information.
     *
     * @return API-nyckeln som behövs för att göra förfrågningar till LIBRIS API.
     */
    public String getKey() {
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .filename(".env")
                .load();

        return dotenv.get("LIBRIS_API_NYCKEL");
    }
}
