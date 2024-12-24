package com.example.Twitetr.Controller;

import com.example.Twitetr.Entity.BlueSkyText;
import com.example.Twitetr.Service.LibrisManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@RestController
@CrossOrigin
@RequestMapping("/api/text")
public class BlueSky_Controller {
    private ArrayList<BlueSkyText> textList = new ArrayList<>();
    @Autowired
    private LibrisManager libris;

    // en metod för att veta vilka otillåtna tecken som finns.
    public boolean containsInvalidCharacters(String text){
        String invalidCharsRegex = "[\\⛧𖤐⛥♱𐕣⁶⁶⁶⁶𖤐⁶♰𓃶🜏𖤐𐕣⁶⁶⁶☠︎︎🗡⛧☦卐卍\"]";
        Pattern pattern = Pattern.compile(invalidCharsRegex);
        
        return pattern.matcher(text).find();
    }

    @PostMapping("/post-text")
    public ResponseEntity<String> postText(@RequestBody Map<String, String> userInput){
        String text = userInput.get("userText");

        if(checkIfEmpty(text)){
            return ResponseEntity.badRequest().body("The text does not exist. Please Try again");
        }

        if(textAboveLimit(text)){
            return ResponseEntity.badRequest().body("The text has more than 500 characters.");
        }

        if(containsInvalidCharacters(text)){
            System.out.println("Sant");
            return ResponseEntity.badRequest().body("Error: The text has forbidden charachters.");
        }

        boolean success = sendToBlueSkyAPI(text);

        if(success){
            return ResponseEntity.ok("The text has been sent.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Cannot publish text.");
        }

    }

    //skickar text till Bluesky API
    public boolean sendToBlueSkyAPI(String text){
        try {
            //mock - kod
            System.out.println("The following text is being sent to BlueSky API: " + text);
            return true;
        } catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    // ResponseEntity is a clas used to show HTTP requests, such as status code.
    @PostMapping
    public ResponseEntity<String> addText(@RequestBody BlueSkyText text) {
        if(text.getTextInformation() == null){
            return ResponseEntity.badRequest().body("The text does not exist. Please Try again");
        }
        else if(text.getTextInformation().isEmpty()){
            return ResponseEntity.badRequest().body("The text is empty and therefore cannot be sent. Please try again.");
        }

        else {
            textList.add(text);
            return ResponseEntity.ok("Text successfully received: " + text.getTextInformation());
        }
    }

    @PostMapping("/manage-text")
    public ResponseEntity<HashMap<String, Object>> manageText(@RequestBody Map<String, String> userInput) {
        HashMap<String, Object> spellingControl = new HashMap<>();
        String userText = userInput.get("userText");
        String specified_language = userInput.get("language"); ///måste ändras sen, där användaren får välja eget språk.
    
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

        System.out.println("CONTROLLER: manageText is calling checkSpelling with: " + userText);
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

    public boolean checkIfEmpty(String input){
        if(input == null || input.trim().isEmpty()){
            return true;
        } else {
            return false;
        }
    }

    public boolean textAboveLimit(String text){
        if(text.length() > 500){
            return true;
        } else {
            return false;
        }
    }

    public Map<String, String> suggestedGrammar(HashMap<String, Object> librisResponse){
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
