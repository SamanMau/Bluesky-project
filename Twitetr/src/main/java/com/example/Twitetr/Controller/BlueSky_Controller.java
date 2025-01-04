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
            // Create JSON file with text
            createJSONFile(text);

            // Simulate publishing logic (replace this with your actual implementation)
            boolean published = publishToBlueSky(text); // Assume this method publishes the text

            if (published) {
                response.put("status", "success");
                response.put("message", "Text successfully published.");
                return ResponseEntity.ok(response); // Send 200 OK immediately after publishing
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

    // Simulate publishing text to BlueSky API
    private boolean publishToBlueSky(String text) {
        // Add your BlueSky publishing logic here
        // For example, call the API and return true if successful
        System.out.println("Publishing text to BlueSky: " + text);
        return true; // Simulate successful publishing
    }

    // Create a JSON file to store the text
    private void createJSONFile(String text) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File("userText.json");

        // Prepare data as a map
        Map<String, String> data = new HashMap<>();
        data.put("userText", text);

        // Write data to JSON file
        objectMapper.writeValue(file, data);
        System.out.println("JSON file created successfully with text: " + text);
    }

    // @PostMapping("/post-text")
    // public ResponseEntity<HashMap<String, Object>> postText(@RequestBody Map<String, String> userInput){
    //     String text = userInput.get("userText");
    //     System.out.println("JAG ÄR I POST TEXT: Received text - " + text);
    //     HashMap<String, Object> map = new HashMap<>();


    //     // if (checkIfEmpty(text)) {
    //     //     map.put("invalid", "Text is empty. Try again");
    //     //     return ResponseEntity.badRequest().body(map);
    //     // }

    //     // if (textAboveLimit(text)) {
    //     //     map.put("invalid", "The text exceeds 500 characters.");
    //     //     return ResponseEntity.badRequest().body(map);
    //     // }

    //     // if (containsInvalidCharacters(text)) {
    //     //     map.put("invalid", "Error: The text contains forbidden characters.");
    //     //     return ResponseEntity.badRequest().body(map);
    //     // }

    //     try{
    //         createJSONFile(text);
    //     } catch (Exception e){
    //         e.printStackTrace();
    //         map.put("error", "Could not create JSON file: " + e.getMessage());
    //         System.out.println("Post-text failed: JSON file creation error.");
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body(map);
    //     }

    //     ApiAuthentication apiAuthentication = new ApiAuthentication();

    //     boolean success = apiAuthentication.manageJWT(text);

    //     if(success){
    //         System.out.println("Post-text succeeded: Text published successfully.");
    //         map.put("status", "success");
    //         map.put("message", "Text received by BlueSky API");
    //         map.put("receivedText", text);

    //         return ResponseEntity.ok(map); // Status 200
    //     } else{
    //         map.put("error", "Can't process the text with BlueSky API.");
    //         System.out.println("Post-text failed: BlueSky API error.");
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body(map);
    //     }

        
    // }

    // public void createJSONFile(String text) throws IOException {
    //     ObjectMapper objectMapper = new ObjectMapper();
    //     File file = new File("userText.json");

    //     // förbereder datan i hashmap format
    //     Map<String, String> data = new HashMap<>();
    //     data.put("userText", text);

    //     // skriver data till JSON filen.
    //     objectMapper.writeValue(file, data);
    // }


    @PostMapping("/manage-text")
    public ResponseEntity<HashMap<String, String>> manageText(@RequestBody HashMap<String, String> userInput) {
        System.out.println("JAG ÄR I MANAGE TEXT");

        HashMap<String, String> response = new HashMap<>();
        String userText = userInput.get("userText");
        String specified_language = userInput.get("language");
        System.out.println("Received userText: " + userInput.get("userText"));
                
        if (!validateInput(userText, specified_language, response)) {
            return ResponseEntity.badRequest().body(response);
        }

        String[] words = userText.split(" ");
        StringBuilder correctedText = new StringBuilder();
        boolean hasCorrections = false;

        for (String word : words) {
            HashMap<String, String> wordResponse = libris.checkSpelling(word.trim());
            String correctedWord = wordResponse.get("after");
            if (correctedWord == null || correctedWord.equals(word)) {
                correctedWord = word; // No correction needed
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


    // @PostMapping("/manage-text")
    // public ResponseEntity<HashMap<String, String>> manageText(@RequestBody HashMap<String, String> userInput) {
    //     System.out.println("JAG ÄR I MANAGE TEXT");

    //     HashMap<String, String> response = new HashMap<>();
    //     String userText = userInput.get("userText");
    //     String specified_language = userInput.get("language");
    //     System.out.println("Received userText: " + userInput.get("userText"));
                
    //     if (!validateInput(userText, specified_language, response)) {
    //         return ResponseEntity.badRequest().body(response);
    //     }

    //   String[] words = userText.split(" ");
    //   StringBuilder correctedText = new StringBuilder();
    //   HashMap<String, String> corrections = new HashMap<>();

    //   for(String word : words){
    //     HashMap<String, String> wordResponse = libris.checkSpelling(word.trim());
    //     String correctedWord = wordResponse.get("after");
    //   //  response.putAll(wordResponse);

    //     if(correctedWord == null){
    //         correctedWord = wordResponse.get("before");
    //     }

    //     correctedText.append(correctedWord).append(" ");

    //   }

    //     response.put("originalText", userText);
    //     response.put("correctedText", correctedText.toString());

    //     for(String value : response.values()){
    //         System.out.println(value + " ");
    //     }

    //     return ResponseEntity.ok(response);
 
    // }

    // private boolean validateInput(String userText, String language, HashMap<String, String> response) {
    //     if (checkIfEmpty(userText)) {
    //         response.put("invalid", "The text is empty");
    //         return false;
    //     }

    //     if (checkIfEmpty(language)) {
    //         response.put("invalid", "No language has been specified");
    //         return false;
    //     }

    //     if (!language.equals("en") && !language.equals("sv")) {
    //         response.put("invalid", "Unsupported language specified. Use 'en' or 'sv'.");
    //         return false;
    //     }

    //     if (containsInvalidCharacters(userText)) {
    //         response.put("invalid", "The text contains invalid characters.");
    //         return false;
    //     }
    //     return true;
    // }

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
