package com.example.Twitetr.Controller;

//URL FÖR ATT TESTA BACKEND: http://localhost:8080/api/tweets
//http://localhost:8080/api/tweets

import com.example.Twitetr.Entity.Tweet;
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
@RequestMapping("/api/tweets")
public class Controller_Twitter {
    private ArrayList<Tweet> tweetList = new ArrayList<>();
    @Autowired
    private LibrisManager libris;

    @GetMapping //Hämtar alla Tweets
    public String getAllTweets() { //testmetod.
        return "Hej hej!";
    }

    /*
    public ArrayList<Tweet> getAllTweets() {
        return tweetList;
    }
     */

    // en metod för att veta vilka otillåtna tecken som finns.
    public boolean containsInvalidCharacters(String tweet){
        String invalidCharsRegex = "[\\⛧𖤐⛥♱𐕣⁶⁶⁶⁶𖤐⁶♰𓃶🜏𖤐𐕣⁶⁶⁶☠︎︎🗡⛧☦卐卍\"]";
        Pattern pattern = Pattern.compile(invalidCharsRegex);
        
        return pattern.matcher(tweet).find();
    }

    @PostMapping("/post-tweet")
    public ResponseEntity<String> postTweet(@RequestBody Map<String, String> userInput){
        String tweet = userInput.get("tweet");

        if(checkIfEmpty(tweet)){
            return ResponseEntity.badRequest().body("The tweet does not exist. Please Try again");
        }

        if(tweetAboveLimit(tweet)){
            return ResponseEntity.badRequest().body("The tweet has more than 280 characters.");
        }

        if(containsInvalidCharacters(tweet)){
            System.out.println("Sant");
            return ResponseEntity.badRequest().body("Error: Tweeten innehåller otillåtna tecken.");
        }

        boolean success = sendToTwitterAPI(tweet);

        if(success){
            return ResponseEntity.ok("The tweet has been sent.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Kunde inte publicera tweeten.");
        }

    }

    //skickar tweet till twitters API
    public boolean sendToTwitterAPI(String tweet){
        try {
            //mock - kod
            System.out.println("Följande tweet skickas till twitters API: " + tweet);
            return true;
        } catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    // ResponseEntity is a clas used to show HTTP requests, such as status code.
    @PostMapping
    public ResponseEntity<String> addTweet(@RequestBody Tweet tweet) {
        if(tweet.getTweetInformation() == null){
            return ResponseEntity.badRequest().body("The tweet does not exist. Please Try again");
        }
        else if(tweet.getTweetInformation().isEmpty()){
            return ResponseEntity.badRequest().body("The tweet is empty and therefore cannot be sent. Please try again.");
        }

        else {
            tweetList.add(tweet);
            return ResponseEntity.ok("Tweet successfully received: " + tweet.getTweetInformation());
        }
    }

    @PostMapping("/manage-tweet")
    public ResponseEntity<HashMap<String, Object>> manageTweet(@RequestBody Map<String, String> userInput) {
        HashMap<String, Object> spellingControl = new HashMap<>();
        String userTweet = userInput.get("tweet");
        String specified_language = "sv"; ///måste ändras sen, där användaren får välja eget språk.
    
        boolean empty_tweet = checkIfEmpty(userTweet);
        boolean no_language_specified = checkIfEmpty(specified_language);
    
        if (empty_tweet || no_language_specified) {
            spellingControl.put("invalid", "The tweet is either empty or no language has been specified");
            return ResponseEntity.badRequest().body(spellingControl);
        }
    
        if (containsInvalidCharacters(userTweet)) {
            spellingControl.put("invalid", "Tweet contains invalid characters.");
            return ResponseEntity.badRequest().body(spellingControl);
        }

        HashMap<String, Object> librisResponse = libris.checkSpelling(userTweet, specified_language);

        if(librisResponse.containsKey("invalid")){
            String key = "invalid";
            Object value = librisResponse.get("invalid");

            spellingControl.put(key, value);
            return ResponseEntity.badRequest().body(spellingControl);
        }

        Map<String, String> spellingCorrection = suggestedGrammar(librisResponse);
    
        String correctedTweet = String.join(" ", spellingCorrection.values());

        spellingControl.put("before", userTweet);
        spellingControl.put("after", correctedTweet);
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

    public boolean tweetAboveLimit(String tweet){
        if(tweet.length() > 280){
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

    /* 
    public static void main(String[] args) {
        Controller_Twitter controller = new Controller_Twitter();
        String inputTweet = "Twitetr är fantastisk!";
        String correctedTweet = controller.suggestedGrammar(inputTweet, "svenska");
        System.out.println(correctedTweet);
    }
        */

}
