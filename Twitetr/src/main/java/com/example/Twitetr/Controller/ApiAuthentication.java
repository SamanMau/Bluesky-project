package com.example.Twitetr.Controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Io;

import io.github.cdimascio.dotenv.Dotenv;

public class ApiAuthentication {

    public ApiAuthentication(){

    }

    public boolean manageJWT(String text){
        String name = getName();
        String password = getPassword();

        HashMap<String, String> info = createSession(name, password);

        if(info != null){
            String accessJwt = info.get("accessJwt");
            String sessionDid = info.get("sessionDid");

            boolean success = createPost(accessJwt, sessionDid, text);

            if(success){
                return true;
            }
            
        }

        return false;
    }

    public String getPassword(){
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .filename(".env")
                .load();

        String password = dotenv.get("BLUESKY_PASSWORD");
        return password;
    }

    public String getName(){
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .filename(".env")
                .load();

        String userName = dotenv.get("BLUESKY_USERNAME");
        return userName;
    }

    /*Metoden autentiserar användaren via Blueskys API och returnerar
    autentiserings koden samt en identifierare för användarens session.
    */
    public HashMap<String, String> createSession(String username, String password) {
        try {
            
            //ändpunkten där inloggning hanteras.
            URL url = new URL("https://bsky.social/xrpc/com.atproto.server.createSession");
        
            //skapar HTTP anslutning till Blueskys API.
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("POST");

            //berättar om att data skickas i json format.
            httpConnection.setRequestProperty("Content-Type", "application/json");
            
            //möjliggör att skicka data i HTTP förfrågans body.
            httpConnection.setDoOutput(true);

        // JSON-body med användaruppgifter
        String jsonInput = "{\n" +
                "  \"identifier\": \"" + username + "\",\n" +
                "  \"password\": \"" + password + "\"\n" +
                "}";

        /*
        öppnar en ström för att skicka data till servern. Den omvandlar
        json sträng till en byte array för att skicka data. Strömmar hanterar
        enbart byte data.
        */       
         try{
            var outstream = httpConnection.getOutputStream();
            OutputStream os = outstream;
            os.write(jsonInput.getBytes(StandardCharsets.UTF_8));

         } catch(Exception e){
            e.printStackTrace();
         }  

        // Läs API-svaret och hämtar status kod.
        int responseCode = httpConnection.getResponseCode();
        InputStream streamResponse;

        if(responseCode == 200){
            streamResponse = httpConnection.getInputStream();
        } else{
            streamResponse = httpConnection.getErrorStream();
        }

        //Läser av svaret från servern. Vi lagrar jwt och did i variabeln "response"
        Scanner input = new Scanner(streamResponse, "utf-8");
        StringBuilder response = new StringBuilder();
        while (input.hasNext()) {
            String line = input.nextLine();
            response.append(line);
        }
        input.close();
        
        // om autentisering var framgångsrik, hämta accessJwt och sessionDid
        if (responseCode == 200) {
            String serverResponse = response.toString();


            // Hämta accessJwt
            //hoppar fram 13 poistioner för att hitta det första tecknet i värdet.
            int start = serverResponse.indexOf("\"accessJwt\":\"") + 13;
            
            /*start hittar bara början av värdet, men inte var den slutar.
              end letar efter nästa citattecken och markerar slutet av värdet
              för accessJwt.
            
            */
            int end = serverResponse.indexOf("\"", start);

            //extraherar texten mellan start och end.
            String accessJwt = serverResponse.substring(start, end);

            // Samma sak görs för sessionDid.
            int startDid = serverResponse.indexOf("\"did\":\"") + 7;
            int endDid = serverResponse.indexOf("\"", startDid);
            String sessionDid = serverResponse.substring(startDid, endDid);

            // Lägg till båda i en Map
            HashMap<String, String> map = new HashMap<>();
            map.put("accessJwt", accessJwt);
            map.put("sessionDid", sessionDid);

            return map;
        } 
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}


    //skickar en post förfrågan till API:t
    public boolean createPost(String accessJwt, String sessionDid, String text) {
        try {
   
           URL url = new URL("https://bsky.social/xrpc/com.atproto.repo.createRecord");
           HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
           httpConnection.setRequestMethod("POST");
           httpConnection.setRequestProperty("Content-Type", "application/json");
           httpConnection.setRequestProperty("Authorization", "Bearer " + accessJwt);
           httpConnection.setDoOutput(true);
   
           String jsonInput = getJsonInput(text, sessionDid);
   
           try{
               var outStream = httpConnection.getOutputStream();
               OutputStream os = outStream;
               os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
           } catch(IOException e){
               e.printStackTrace();
           }
   
           // läser API-svaret och hämtar statuskod.
           int responseCode = httpConnection.getResponseCode();
           InputStream streamResponse;
   
           if(responseCode == 200){
               streamResponse = httpConnection.getInputStream();
               return true;
           } else{
               streamResponse = httpConnection.getErrorStream();
               return false;
           }
   
   
       } catch (Exception e) {
           e.printStackTrace();
       }
   
       return false;
   }
   
       public String getJsonInput(String text, String sessionDid){
                   // Skapa tidsstämpel och JSON-body
           String createdAt = Instant.now().toString();
           
           String jsonInput = "{\n" +
                           "  \"repo\": \"" + sessionDid + "\",\n" +
                           "  \"collection\": \"app.bsky.feed.post\",\n" +
                           "  \"record\": {\n" +
                           "    \"$type\": \"app.bsky.feed.post\",\n" +
                           "    \"text\": \"" + text + "\",\n" +
                           "    \"createdAt\": \"" + createdAt + "\"\n" +
                           "  }\n" +
                           "}";
   
           return jsonInput;                
       }

        
}
