package com.example.Twitetr.Controller;

//URL FÖR ATT TESTA BACKEND: http://localhost:8080/api/tweets

import com.example.Twitetr.Entity.Tweet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@CrossOrigin
@RequestMapping("/api/tweets")
public class Controller_Twitter {
    private ArrayList<Tweet> tweetList = new ArrayList<>();

    @GetMapping //Hämtar alla Tweets
    public String getAllTweets() { //testmetod.
        return "Hej hej!";
    }

    /*
    public ArrayList<Tweet> getAllTweets() {
        return tweetList;
    }
     */


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

}
