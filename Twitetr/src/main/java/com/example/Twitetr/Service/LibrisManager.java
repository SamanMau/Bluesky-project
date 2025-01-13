package com.example.Twitetr.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Den här klassen ansvarar för att kommunicera med LIBRIS API. Ett objekt av
 * klassen skapas i Bluesky_Controller. Variabeln "LIBRIS_API_URL" är en bas URL
 * som anger att data ska returneras i JSON format.
 */
@Service
public class LibrisManager {
    private static final String LIBRIS_API_URL = "http://api.libris.kb.se/bibspell/spell?query=%s&key=%s&format=json";

     /*
      * Metoden skickar en förfrågan till LIBRIS API för att kontrollera stavningen av användarens text.
      Det som returneras är en hashmap.
      */
    public HashMap<String, String> checkSpelling(String userInput) {
        RestTemplate restTemplate = new RestTemplate();
        HashMap<String, String> responseMap = new HashMap<>();
        String key = getKey();
        System.out.println("Userinput: " + userInput);
        String URL = String.format(LIBRIS_API_URL, URLEncoder.encode(userInput, StandardCharsets.UTF_8), key);
        String correctedWord = "";
        System.out.println("Genererad URL: " + URL);

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
                    //terms innehåller en list av hashmaps
                    var terms = (List<Map<String, Object>>) suggestion.get("terms");
                    
                    if (terms != null && !terms.isEmpty()) {
                     
                        int count = 0;
                        for (Map<String, Object> term : terms){
                            if(term.containsKey("value")){
                                count++;
                            }
                        }
                //Detta skulle indikera att det sannolikt har gått fel, exempelvis att ordet "glömde" genererar "GL C3 MDE" vilket är 3 values.
                // I detta fall, behåller vi användarens ursprungliga ord.

                        if(count > 1){
                            correctedWord = userInput;
                        }
                        else {
                            correctedWord = (String) terms.get(0).get("value");
                        }
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
