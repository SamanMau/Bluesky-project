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

    // The following method checks if the text written by the user contains invalid chracthers.
    public boolean containsInvalidCharacters(String text) {
        String invalidCharsRegex = "[\\𖤐𐕣⁶𖤐𓃶🜏𖤐𐕣☠︎︎🗡⛧☦卐卍\"]";
        Pattern pattern = Pattern.compile(invalidCharsRegex);
        return pattern.matcher(text).find();
    }

    //This method checks if the users input is empty.
    public boolean checkIfEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    // This method checks if the text exceeds 300 charachters. This is the limit in Bluesky.
    public boolean textAboveLimit(String text) {
        return text.length() > 300;
    }


    public HashMap<String, Object> mockBlueSkyAPI(String text) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Text received by BlueSky API");
        response.put("receivedText", text);
        return response;
    }


    // Endpoint to handle text validation and processing
    @PostMapping("/post-text")
    public ResponseEntity<HashMap<String, Object>> postText(@RequestBody Map<String, String> userInput){
        String text = userInput.get("userText");
        HashMap<String, Object> map = new HashMap<>();


        if (checkIfEmpty(text)) {
            map.put("invalid", "Text is empty. Try again");
            return ResponseEntity.badRequest().body(map);
        }

        if (textAboveLimit(text)) {
            map.put("invalid", "The text exceeds 500 characters.");
            return ResponseEntity.badRequest().body(map);
        }

        if (containsInvalidCharacters(text)) {
            map.put("invalid", "Error: The text contains forbidden characters.");
            return ResponseEntity.badRequest().body(map);
        }

        try{
            createJSONFile(text);
        } catch (Exception e){
            map.put("error", "Could not create JSON file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(map);
        }

        ApiAuthentication apiAuthentication = new ApiAuthentication();

        boolean success = apiAuthentication.manageJWT(text);

        if(success){
            map.put("status", "success");
            map.put("message", "Text received by BlueSky API");
            map.put("receivedText", text);
        } else{
            map.put("error", "Can't process the text with BlueSky API.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(map);
        }

        return ResponseEntity.ok(map);
    }

    public void createJSONFile(String text) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File("userText.json");

        // förbereder datan i hashmap format
        Map<String, String> data = new HashMap<>();
        data.put("userText", text);

        // skriver data till JSON filen.
        objectMapper.writeValue(file, data);
    }


    @PostMapping("/manage-text")
    public ResponseEntity<HashMap<String, String>> manageText(@RequestBody HashMap<String, String> userInput) {

        HashMap<String, String> response = new HashMap<>();
        String userText = userInput.get("userText");
        String specified_language = userInput.get("language");
        System.out.println("Received userText: " + userInput.get("userText"));
        System.out.println("Received language: " + userInput.get("language"));
        
        
        if (!validateInput(userText, specified_language, response)) {
            return ResponseEntity.badRequest().body(response);
        }

      String[] words = userText.split(" ");
      StringBuilder correctedText = new StringBuilder();
      HashMap<String, String> corrections = new HashMap<>();

      for (String word : words) {
        HashMap<String, String> wordResponse = libris.checkSpelling(word.trim());

        if (wordResponse.containsKey("invalid")) {
            corrections.put(word, word);
            correctedText.append(word).append(" ");
    
        } else {
            String correctedWord = wordResponse.getOrDefault("suggestions", word);
            corrections.put(word, correctedWord);
            correctedText.append(correctedWord).append("");
        }
      }

        response.put("originalText", userText);
        response.put("correctedText", correctedText.length() > 0 ? correctedText.toString().trim() : "No corrections found.");


        for (Map.Entry<String, String> entry : corrections.entrySet()) {
            response.put(entry.getKey(), entry.getValue());
        }
   
        return ResponseEntity.ok(response);
 
    }

    private boolean validateInput(String userText, String language, HashMap<String, String> response) {
        if (checkIfEmpty(userText)) {
            response.put("invalid", "The text is empty");
            return false;
        }

        if (checkIfEmpty(language)) {
            response.put("invalid", "No language has been specified");
            return false;
        }

        if (!language.equals("en") && !language.equals("sv")) {
            response.put("invalid", "Unsupported language specified. Use 'en' or 'sv'.");
            return false;
        }

        if (containsInvalidCharacters(userText)) {
            response.put("invalid", "The text contains invalid characters.");
            return false;
        }
        return true;
    }

  /*   private String correctedSentence(String userText, HashMap<String, String> corrections) {
        String[] words = userText.split(" "); 
        StringBuilder correctedText = new StringBuilder();

        for (String word: words) {
            HashMap<String, String> wordResponse = libris.checkSpelling(word);

           if (wordResponse.containsKey("invalid")) {
            corrections.put(word, word);
            correctedText.append(word).append(" ");

           } else {
            
            String correctedWord = wordResponse.getOrDefault(word, word);
            corrections.put(word, correctedWord);
            correctedText.append(correctedWord).append(" ");

           }

        }
        return correctedText.toString().trim();
    }  */

}
