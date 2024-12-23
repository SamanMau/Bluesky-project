package com.example.Twitetr.Controller;

import com.example.Twitetr.Entity.Thread;
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
@RequestMapping("/api/threads")
public class Controller_Thread {
    private ArrayList<Thread> threadList = new ArrayList<>();
    @Autowired
    private LibrisManager libris;

    // en metod för att veta vilka otillåtna tecken som finns.
    public boolean containsInvalidCharacters(String thread){
        String invalidCharsRegex = "[\\⛧𖤐⛥♱𐕣⁶⁶⁶⁶𖤐⁶♰𓃶🜏𖤐𐕣⁶⁶⁶☠︎︎🗡⛧☦卐卍\"]";
        Pattern pattern = Pattern.compile(invalidCharsRegex);
        
        return pattern.matcher(thread).find();
    }

    @PostMapping("/post-thread")
    public ResponseEntity<String> postThread(@RequestBody Map<String, String> userInput){
        String thread = userInput.get("thread");

        if(checkIfEmpty(thread)){
            return ResponseEntity.badRequest().body("The thread does not exist. Please Try again");
        }

        if(threadAboveLimit(thread)){
            return ResponseEntity.badRequest().body("The thread has more than 500 characters.");
        }

        if(containsInvalidCharacters(thread)){
            System.out.println("Sant");
            return ResponseEntity.badRequest().body("Error: The thread has forbidden charachters.");
        }

        boolean success = sendToThreadAPI(thread);

        if(success){
            return ResponseEntity.ok("The thread has been sent.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Cannot publish thread.");
        }

    }

    //skickar threads till threads API
    public boolean sendToThreadAPI(String thread){
        try {
            //mock - kod
            System.out.println("The followign thread is being sent to Threads API: " + thread);
            return true;
        } catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    // ResponseEntity is a clas used to show HTTP requests, such as status code.
    @PostMapping
    public ResponseEntity<String> addThread(@RequestBody Thread thread) {
        if(thread.getThreadInformation() == null){
            return ResponseEntity.badRequest().body("The thread does not exist. Please Try again");
        }
        else if(thread.getThreadInformation().isEmpty()){
            return ResponseEntity.badRequest().body("The thread is empty and therefore cannot be sent. Please try again.");
        }

        else {
            threadList.add(thread);
            return ResponseEntity.ok("Thread successfully received: " + thread.getThreadInformation());
        }
    }

    @PostMapping("/manage-thread")
    public ResponseEntity<HashMap<String, Object>> manageThread(@RequestBody Map<String, String> userInput) {
        HashMap<String, Object> spellingControl = new HashMap<>();
        String userThread = userInput.get("thread");
        String specified_language = userInput.get("language"); ///måste ändras sen, där användaren får välja eget språk.
    
        boolean empty_thread = checkIfEmpty(userThread);
        boolean no_language_specified = checkIfEmpty(specified_language);
    
        if (empty_thread || no_language_specified) {
            spellingControl.put("invalid", "The thread is either empty or no language has been specified");
            return ResponseEntity.badRequest().body(spellingControl);
        }
    
        if (containsInvalidCharacters(userThread)) {
            spellingControl.put("invalid", "Thread contains invalid characters.");
            return ResponseEntity.badRequest().body(spellingControl);
        }

        System.out.println("CONTROLLER: manageThread is calling checkSpelling with: " + userThread);
        HashMap<String, Object> librisResponse = libris.checkSpelling(userThread, specified_language);

        if (librisResponse.containsKey("invalid")) {
            return ResponseEntity.badRequest().body(librisResponse);
        }

        Map<String, String> spellingCorrection = suggestedGrammar(librisResponse);
    
        String correctedThread = String.join(" ", spellingCorrection.values());

        spellingControl.put("before", userThread);
        spellingControl.put("after", correctedThread);
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

    public boolean threadAboveLimit(String thread){
        if(thread.length() > 500){
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
