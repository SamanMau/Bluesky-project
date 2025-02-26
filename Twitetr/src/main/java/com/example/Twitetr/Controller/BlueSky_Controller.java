package com.example.Twitetr.Controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.coyote.AbstractProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Twitetr.Service.LibrisManager;
import com.fasterxml.jackson.databind.ObjectMapper; // Required for JSON processing
import io.github.cdimascio.dotenv.Dotenv;

@RestController
@CrossOrigin(origins = "http://127.0.0.1:5500/")
@RequestMapping("/api/text")
public class BlueSky_Controller {

    @Autowired
    private LibrisManager libris;

 /**
     * Kontrollera om texten innehåller ogiltiga tecken.
     * Regex används för att matcha mot förbjudna tecken.
     *
     * @param text Texten att kontrollera.
     * @return true om ogiltiga tecken finns; false annars.
     */
    
     public boolean containsInvalidCharacters(String text) {
        String invalidCharsRegex = "[\\𖤐𐕣⁶𖤐𓃶🜏𖤐𐕣☠︎︎🗡⛧☦卐卍]";
        Pattern pattern = Pattern.compile(invalidCharsRegex);
        return pattern.matcher(text).find();
    }


    /**
     * Kontrollera om texten är tom eller bara innehåller mellanslag.
     * "text.matches()" kontrollerar om texten bara innehåller osynliga tecken som \n och \t
     * @param text Texten att kontrollera.
     * @return true om texten är tom; false annars.
     */

    public boolean checkIfEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

  /**
     * Kontrollera om texten överskrider 300 tecken, som är Blueskys gräns.
     *
     * @param text Texten att kontrollera.
     * @return true om texten är för lång; false annars.
     */

    public boolean textAboveLimit(String text) {
        return text.length() > 300;
    }

     /**
     * Mock-funktion för att simulera svar från Bluesky API.
     *
     * @param text Texten som skickas till API:t.
     * @return En HashMap som innehåller en status och det mottagna meddelandet.
     */

    public HashMap<String, Object> mockBlueSkyAPI(String text) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Text received by BlueSky API");
        response.put("receivedText", text);
        return response;
    }

     /**
     * Endpoint för att skicka text till Bluesky API.
     * Den hanterar inloggning, publicering och felhantering.
     *
     * @param userInput Användarens inmatning som JSON.
     * @return ResponseEntity med status och svar från servern.
     */
    
    @PostMapping("/post-text")
    public ResponseEntity<HashMap<String, Object>> postText(@RequestBody Map<String, String> userInput) {
        String text = userInput.get("userText");
        HashMap<String, Object> response = new HashMap<>();
        
        String replaceText = text.replace("\n", " ");


        try {
            
            // Skapa en ny instans av ApiAuthentication för inloggning
            ApiAuthentication apiAuthentication = new ApiAuthentication(this);

            // Försök att publicera text via Bluesky API
            boolean success = apiAuthentication.manageJWT(replaceText);

            // Skapa en JSON-fil av texten för lagring
            createJSONFile(replaceText);
            
            // Kontrollera om publiceringen lyckades
            if (success) {
                response.put("status", "success");
                response.put("message", "Text successfully published.");
                return ResponseEntity.ok(response); 
            } else {
                response.put("status", "error");
                response.put("message", "Publishing failed due to an unknown error.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Server encountered an error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

     /**
     * Skapar en JSON-fil av den givna texten.
     *
     * @param text Texten att lagra i JSON-filen.
     * @throws IOException Om filen inte kan skapas.
     */

    private void createJSONFile(String text) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File("userText.json");

        // Lagra texten som en nyckel-värde-pair i filen
        Map<String, String> data = new HashMap<>();
        data.put("userText", text);

        // Skriv datan till fil
        objectMapper.writeValue(file, data);
        System.out.println("JSON file created successfully with text: " + text);
    }


     /**
     * Endpoint för att validera och korrigera text.
     * Den kontrollerar längd, förbjudna tecken och tomma texter.
     *
     * @param userInput Användarens text som JSON.
     * @return ResponseEntity med eventuella korrigeringar eller felmeddelanden.
     */

     @PostMapping("/login-info")
     public void loginInfo(@RequestBody HashMap<String, String> userInput){
        String name = userInput.get("userName");
        String password = userInput.get("password");

        Database_Controller controller = new Database_Controller(this);
        boolean exists = controller.checkIfUserExists(name, password);

        System.out.println(name + password);

     }

    @PostMapping("/manage-text")
    public ResponseEntity<HashMap<String, String>> manageText(@RequestBody HashMap<String, String> userInput) {
        HashMap<String, String> response = new HashMap<>();
        String userText = userInput.get("userText");
        
        // Kontrollera om texten är för lång
        if (textAboveLimit(userText)) {
            System.out.println("above limit");
            response.put("invalid", "The text exceeds 300 characters.");
            return ResponseEntity.badRequest().body(response);
         }
        // Kontrollera om texten innehåller förbjudna tecken
          if (containsInvalidCharacters(userText)) {
            System.out.println("invalid characters");
            response.put("invalid", "Error: The text contains forbidden characters.");
            return ResponseEntity.badRequest().body(response);
        }

        // Kontrollera om texten är tom
        if(checkIfEmpty(userText)){
            response.put("invalid", "The text is empty");
            return ResponseEntity.badRequest().body(response);
        }

        // Korrigera stavfel med hjälp av LibrisManager
        String[] words = userText.split(" ");
        StringBuilder correctedText = new StringBuilder();
        boolean hasCorrections = false;

        for (String word : words) {
            // Kontrollera stavning för varje ord
            HashMap<String, String> wordResponse = libris.checkSpelling(word.trim());
            String correctedWord = wordResponse.get("after");
            
            // Om inget förslag finns, använd ursprungsordet
            if (correctedWord == null || correctedWord.equals(word) || correctedWord.isEmpty()) {
                correctedWord = word; 
            }

            if(correctedWord.length() == 1){
                correctedWord = word; 
            }
            
            if(containsNumbers(correctedWord)){
                System.out.println("sant");
                correctedWord = word; 
            }

            String[] wordDivided = correctedWord.split(" ");

            if(wordDivided.length > 1){
                correctedWord = word; 
            }
             
            else {
                hasCorrections = true; // Minst ett ord korrigerades
            }
            correctedText.append(correctedWord).append(" ");
        }

        // Lägg till resultat i svaret
        response.put("originalText", userText);
        if (hasCorrections) {
            response.put("correctedText", correctedText.toString().trim());
        } else {
            response.put("message", "No corrections found. The text might already be correct.");
        }

        return ResponseEntity.ok(response);
    
    }

    public boolean containsNumbers(String word){

        for(int i = 0; i <= 9; i++){
            String number = Integer.toString(i);

            if(word.contains(number)){
                return true;
            }
        }

        return false;
    }


}
