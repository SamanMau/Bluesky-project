package com.example.Twitetr.Controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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

        map = mockBlueSkyAPI(text);

        if ("success".equals(map.get("status"))) {
            return ResponseEntity.ok(map);
        } else {
            map.put("error", "Can't process the text with BlueSky API.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(map);
        }
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
    public ResponseEntity<HashMap<String, Object>> manageText(@RequestBody HashMap<String, String> userInput) {
        HashMap<String, Object> spellingControl = new HashMap<>();
        String userText = userInput.get("userText");
        String specified_language = userInput.get("language");
        System.out.println("Received userText: " + userInput.get("userText"));
        System.out.println("Received language: " + userInput.get("language"));
        
        


        if(checkIfEmpty(userText)){
            spellingControl.put("invalid", "The text is empty");
            return ResponseEntity.badRequest().body(spellingControl);
        }

        if(checkIfEmpty(specified_language)){
            spellingControl.put("invalid", "No language has been specified");
            return ResponseEntity.badRequest().body(spellingControl);
        }
    
        if (containsInvalidCharacters(userText)) {
            spellingControl.put("invalid", "Text contains invalid characters.");
            return ResponseEntity.badRequest().body(spellingControl);
        }

        HashMap<String, Object> librisResponse = libris.checkSpelling(userText, specified_language);

        if (librisResponse.containsKey("invalid")) {
            return ResponseEntity.badRequest().body(librisResponse);
        }

        Map<String, String> spellingCorrection = suggestedGrammar(librisResponse);
    
        String correctedText = String.join(" ", spellingCorrection.values());

        spellingControl.put("before", userText);
        spellingControl.put("after", correctedText);
        spellingControl.put("suggestions", spellingCorrection);
    
        return ResponseEntity.ok(spellingControl);

        
    }

    public Map<String, String> suggestedGrammar(Map<String, Object> librisResponse){
        Map<String, String> spellingCorrection = new HashMap<>();

        ArrayList<Map<String, Object>> spellingSuggestions = new ArrayList<>();

        if(librisResponse.get("suggestions") instanceof ArrayList){
            spellingSuggestions = (ArrayList<Map<String, Object>>) librisResponse.get("suggestions");
        }

        for(Map<String, Object> spellings : spellingSuggestions){
            String before = (String) spellings.get("word");
            String after = (String) spellings.get("suggestion");

            if(after == null){
                after = before;
            }

            spellingCorrection.put(before, after);
        }

        return spellingCorrection;
    }

}
