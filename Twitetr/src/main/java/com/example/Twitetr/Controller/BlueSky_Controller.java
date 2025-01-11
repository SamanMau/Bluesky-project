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
    public ResponseEntity<HashMap<String, Object>> postText(@RequestBody Map<String, String> userInput) {
        String text = userInput.get("userText");
        HashMap<String, Object> response = new HashMap<>();
        try {
            
            
            ApiAuthentication apiAuthentication = new ApiAuthentication();

            boolean success = apiAuthentication.manageJWT(text);

            createJSONFile(text);
            
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


    private boolean publishToBlueSky(String text) {
        System.out.println("Publishing text to BlueSky: " + text);
        return true; 
    }

    private void createJSONFile(String text) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File("userText.json");


        Map<String, String> data = new HashMap<>();
        data.put("userText", text);


        objectMapper.writeValue(file, data);
        System.out.println("JSON file created successfully with text: " + text);
    }


    @PostMapping("/manage-text")
    public ResponseEntity<HashMap<String, String>> manageText(@RequestBody HashMap<String, String> userInput) {
        System.out.println("JAG ÄR I MANAGE TEXT");

        HashMap<String, String> response = new HashMap<>();
        String userText = userInput.get("userText");
        String specified_language = userInput.get("language");
        System.out.println("Received userText: " + userInput.get("userText"));
        
        if (textAboveLimit(userText)) {
            response.put("invalid", "The text exceeds 300 characters.");
            return ResponseEntity.badRequest().body(response);
         }
          if (containsInvalidCharacters(userText)) {
            response.put("invalid", "Error: The text contains forbidden characters.");
            return ResponseEntity.badRequest().body(response);
        } if(checkIfEmpty(userText)){
            response.put("invalid", "The text is empty");
            return ResponseEntity.badRequest().body(response);

        }

        String[] words = userText.split(" ");
        StringBuilder correctedText = new StringBuilder();
        boolean hasCorrections = false;

        for (String word : words) {
            HashMap<String, String> wordResponse = libris.checkSpelling(word.trim());
            String correctedWord = wordResponse.get("after");
            if (correctedWord == null || correctedWord.equals(word)) {
                correctedWord = word; 
            } else {
                hasCorrections = true; // At least one word was corrected
            }
            correctedText.append(correctedWord).append(" ");
        }

        response.put("originalText", userText);
        if (hasCorrections) {
            response.put("correctedText", correctedText.toString().trim());
        } else {
            response.put("message", "No corrections found. The text might already be correct.");
        }

        return ResponseEntity.ok(response);
    
    }


}
