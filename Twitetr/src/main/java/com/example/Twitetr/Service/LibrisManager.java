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

    /**
     * Skickar en förfrågan till LIBRIS API för att kontrollera stavningen av ett användarinmatat ord
     * och returnerar ett resultat i form av en HashMap.
     *
     * @param userInput          Användarens inmatade text som ska kontrolleras för stavfel.
     * @param specified_language Den språkvariant som användaren specificerat för stavningskontrollen.
     * @return En HashMap som innehåller resultatet av stavningskontrollen.
     */
    public HashMap<String, String> checkSpelling(String userInput) {
        RestTemplate restTemplate = new RestTemplate();
        HashMap<String, String> responseMap = new HashMap<>();
        String key = getKey();
        String URL = String.format(LIBRIS_API_URL, URLEncoder.encode(userInput, StandardCharsets.UTF_8), key);
        String correctedWord = "";
    
        System.out.println("LIBRIS API URL: " + URL);
        System.out.println("Användarinmatning: " + userInput);
    
        try {
            // Skicka GET-förfrågan till API:t
            ResponseEntity<String> response = restTemplate.getForEntity(URL, String.class);
            String result = response.getBody();
    
            System.out.println("LIBRIS API svar: " + result);
    
            if (result != null) {
                // Extrahera det korrigerade ordet från XML-svaret
                correctedWord = extractCorrectedWordFromXML(result);
                if (correctedWord != null) {
                    String message = "Här är det korrigerade ordet: " + correctedWord;
                    System.out.println(message); // Skriv ut meddelandet till terminalen
                } else {
                    System.out.println("Inga korrigeringar hittades för inmatningen."); // Skriv ut meddelande om ingen korrigering hittades
                }
            } else {
                System.out.println("LIBRIS returnerade ett tomt svar."); // Skriv ut felmeddelande om inget svar mottogs
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("LIBRIS API-fel: " + e.getMessage()); // Skriv ut felmeddelande vid API-fel
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

        String key = dotenv.get("LIBRIS_API_NYCKEL");
        return key;
    }

    /**
     * Extraherar det korrigerade ordet från XML-svaret från LIBRIS API.
     * 
     * @param xmlResponse XML-svaret som returneras från LIBRIS API.
     * @return Det korrigerade ordet om det finns, annars null.
     */
    public String extractCorrectedWordFromXML(String xmlResponse) {
        try {
            // Parsar XML-svaret med hjälp av DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            var builder = factory.newDocumentBuilder();
            var document = builder.parse(new java.io.ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8)));
    
            // Hämta alla <term>-element från XML-dokumentet
            var termNodes = document.getElementsByTagName("term");
            for (int i = 0; i < termNodes.getLength(); i++) {
                var termElement = (org.w3c.dom.Element) termNodes.item(i);
                
                // Kontrollera om attributet 'changed' är 'true'
                String changedAttr = termElement.getAttribute("changed");
                if ("true".equals(changedAttr)) {
                    return termElement.getTextContent(); // Returnera det korrigerade ordet
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Returnera null om inget korrigerat ord hittas
    }   
}
