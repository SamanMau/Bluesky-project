package com.example.Twitetr.Controller;

import com.example.Twitetr.Entity.BlueSkyText;
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
@CrossOrigin
@RequestMapping("/api/text")
public class BlueSky_Controller {

    @Autowired
    private LibrisManager libris;

    // Check for forbidden characters
    public boolean containsInvalidCharacters(String text) {
        String invalidCharsRegex = "[\\⛧𖤐⛥♱𐕣⁶⁶⁶⁶𖤐⁶♰𓃶🜏𖤐𐕣⁶⁶⁶☠︎︎🗡⛧☦卐卍\"]";
        Pattern pattern = Pattern.compile(invalidCharsRegex);
        return pattern.matcher(text).find();
    }

    // Check if the text is empty
    public boolean checkIfEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    // Check if the text exceeds a specific limit
    public boolean textAboveLimit(String text) {
        return text.length() > 500;
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

        // Example API call to send validated text to a service
        boolean success = sendToBlueSkyAPI(text);

        if (success) {
            return ResponseEntity.ok("The text has been successfully sent.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: Unable to process the text.");
        }
    }

    private boolean sendToBlueSkyAPI(String text) {
        // Simulate API call success
        return true;
    }

    @PostMapping("/manage-text")
    public ResponseEntity<HashMap<String, Object>> manageText(@RequestBody HashMap<String, String> userInput) {
        HashMap<String, Object> spellingControl = new HashMap<>();
        String userText = userInput.get("userText");
        String specified_language = userInput.get("language");

        boolean empty_text = checkIfEmpty(userText);
        boolean no_language_specified = checkIfEmpty(specified_language);
    
        if (empty_text || no_language_specified) {
            spellingControl.put("invalid", "The text is either empty or no language has been specified");
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
