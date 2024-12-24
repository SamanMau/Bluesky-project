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
        String texString = userInput.get("texString");

        if(checkIfEmpty(texString)){
            return ResponseEntity.badRequest().body("The texString does not exist. Please Try again");
        }

        if(textAboveLimit(texString)){
            return ResponseEntity.badRequest().body("The texString has more than 500 characters.");
        }

        if(containsInvalidCharacters(texString)){
            System.out.println("Sant");
            return ResponseEntity.badRequest().body("Error: The texString has forbidden charachters.");
        }

        boolean success = sendToBlueSkyAPI(texString);

        if(success){
            return ResponseEntity.ok("The texString has been sent.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Cannot publish texString.");
        }

    }

    //skickar text till Bluesky API
    public boolean sendToBlueSkyAPI(String texString){
        try {
            //mock - kod
            System.out.println("The following texString is being sent to BlueSky API: " + texString);
            return true;
        } catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    // ResponseEntity is a clas used to show HTTP requests, such as status code.
    @PostMapping
    public ResponseEntity<String> addText(@RequestBody BlueSkyText texString) {
        if(texString.getTextInformation() == null){
            return ResponseEntity.badRequest().body("The texString does not exist. Please Try again");
        }
        else if(texString.getTextInformation().isEmpty()){
            return ResponseEntity.badRequest().body("The texString is empty and therefore cannot be sent. Please try again.");
        }

        else {
            textList.add(texString);
            return ResponseEntity.ok("Text successfully received: " + texString.getTextInformation());
        }
    }

    @PostMapping("/manage-text")
    public ResponseEntity<HashMap<String, Object>> manageText(@RequestBody Map<String, String> userInput) {
        HashMap<String, Object> spellingControl = new HashMap<>();
        String userText = userInput.get("texString");
        String specified_language = userInput.get("language"); ///måste ändras sen, där användaren får välja eget språk.
    
        boolean empty_text = checkIfEmpty(userText);
        boolean no_language_specified = checkIfEmpty(specified_language);
    
        if (empty_text || no_language_specified) {
            spellingControl.put("invalid", "The texString is either empty or no language has been specified");
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
        if(input == null || input.isEmpty()){
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
