package com.example.Twitetr.Entity;

public class Tweet {
    private String tweet_information;

    public Tweet(){

    }

    public String getTweetInformation(){
        return tweet_information;
    }

    public void setTweet_information(String tweet_info){
        this.tweet_information = tweet_info;
    }
}
