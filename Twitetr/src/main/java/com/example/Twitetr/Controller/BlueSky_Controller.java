package com.example.Twitetr.Controller;

import com.example.Twitetr.Service.LibrisManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.HashMap;

@RestController
@CrossOrigin(origins = "http://127.0.0.1:5500")
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

    // Endpoint to handle text validation and processing
    @PostMapping("/post-text")
    public ResponseEntity<String> postText(@RequestBody Map<String, String> userInput) {
        String text = userInput.get("userText");

        if (checkIfEmpty(text)) {
            return ResponseEntity.badRequest().body("The text does not exist. Please try again.");
        }

        if (textAboveLimit(text)) {
            return ResponseEntity.badRequest().body("The text exceeds the limit of 500 characters.");
        }

        if (containsInvalidCharacters(text)) {
            return ResponseEntity.badRequest().body("Error: The text contains forbidden characters.");
        }

        boolean success = sendToBlueSkyAPI(text); //ska tas bort

        if (success) {
            return ResponseEntity.ok("The text has been successfully sent.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: Unable to process the text.");
        }
    }

    public boolean sendToBlueSkyAPI(String text) {
        return true;
    }

    @PostMapping("/manage-text")
    public ResponseEntity<HashMap<String, Object>> manageText(@RequestBody HashMap<String, String> userInput) {
        HashMap<String, Object> spellingControl = new HashMap<>();
        String userText = userInput.get("userText");
        String specified_language = userInput.get("language");


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
