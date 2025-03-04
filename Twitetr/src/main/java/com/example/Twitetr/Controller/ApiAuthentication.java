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
import org.springframework.http.ResponseEntity;

import io.github.cdimascio.dotenv.Dotenv;

/*
 * Denna klass ansvarar för att kommunicera med Blueskys API. Ett objekt av klassen
 * skapas i Bluesky_Controller.
 */
public class ApiAuthentication {
    private BlueSky_Controller controller;

    public ApiAuthentication(BlueSky_Controller controller){
        this.controller = controller;
    }

    /*
     * Metoden hämtar användarens namn och lösenord. Därefter anropar den
     * metoden "createSession()" och skickar in namnet och lösenordet som argument.
     * Därefter anropas metoden "createPost()" som publicerar texten på Bluesky.
     * Om texten publicerades framgångsrikt, returnernas "true" till Controller klassen.
     */
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

    /*
     * Hämtar lösenordet från env filen.
     */
    public String getPassword(){
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .filename(".env")
                .load();

        String password = dotenv.get("BLUESKY_PASSWORD");
        return password;
    }

    /*
     * Hämtar användarens namn från env filen.
     */
    public String getName(){
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .filename(".env")
                .load();

        String userName = dotenv.get("BLUESKY_USERNAME");
        return userName;
    }

    /*
     * Metoden autentiserar användaren via Blueskys API och returnerar
     * autentiserings koden samt en identifierare för användarens session.
     */
    public HashMap<String, String> createSession(String username, String password) {
        try {
            
            //ändpunkten där autentisering hanteras.
            URL url = new URL("https://bsky.social/xrpc/com.atproto.server.createSession");
        
            //skapar HTTP POST anslutning till Blueskys API med hjälp av URL:en.
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("POST");

            //berättar om att data skickas i json format.
            httpConnection.setRequestProperty("Content-Type", "application/json");
            
            httpConnection.setDoOutput(true);

        // JSON-sträng med användarens namn och lösenord.
        String jsonInput = "{\n" +
                "  \"identifier\": \"" + username + "\",\n" +
                "  \"password\": \"" + password + "\"\n" +
                "}";

        /*
        öppnar en ström för att skicka data (json strängen) till servern. Den omvandlar
        json sträng till en byte array för att skicka data.
        */       
         try{
            var outstream = httpConnection.getOutputStream();
            OutputStream os = outstream;
            os.write(jsonInput.getBytes(StandardCharsets.UTF_8));

         } catch(Exception e){
            e.printStackTrace();
         }  

        // läser API-svaret och hämtar status kod. Den returnerar accessJwt och sessionDid
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

        if (responseCode == 200) {
            String serverResponse = response.toString();


            /*
             * Hämta accessJwt, den hoppar fram 13 poistioner för att 
             * hitta det första tecknet i värdet. Start hämtar början av värdet.
             */
            int startJWT = serverResponse.indexOf("\"accessJwt\":\"") + 13;
            
            //Hämtar slutet av värdet.
            int endJwt = serverResponse.indexOf("\"", startJWT);

            //extraherar texten mellan startJWT och endJwt.
            String accessJwt = serverResponse.substring(startJWT, endJwt);

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

    /*
     * Metoden skickar en post förfrågan till Blueskys API och publicerar texten.
     * Om texten publicerades framgångsrikt, returneras det booleska värdet "true".
     */
    public boolean createPost(String accessJwt, String sessionDid, String text) {
        try {
   
           /*
            * En URL för att skapa ett inlägg på Bluesky. JWT - token skickas för att
            autentisera en HTTP-POST begäran.
            */
           URL url = new URL("https://bsky.social/xrpc/com.atproto.repo.createRecord");
           HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
           httpConnection.setRequestMethod("POST");
           httpConnection.setRequestProperty("Content-Type", "application/json");
           httpConnection.setRequestProperty("Authorization", "Bearer " + accessJwt);
           httpConnection.setDoOutput(true);

        // Kontrollera om texten är för lång
        if (controller.textAboveLimit(text)) {
            return false;
         }
        // Kontrollera om texten innehåller förbjudna tecken
          if (controller.containsInvalidCharacters(text)) {
            return false;

        }

        // Kontrollera om texten är tom
        if(controller.checkIfEmpty(text)){
            return false;
        }
   
          text = text.replace("\"", "\\\"");

           String jsonInput = getJsonInput(text, sessionDid);
   
           //Öpnar en utström och skickar JSON variabeln.
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

                if(responseCode == 201){
                    System.out.println("201");
                }

                if(responseCode == 400){
                    System.out.println("400");
                }

                if(responseCode == 401){
                    System.out.println("401");
                }
                
                if(responseCode == 403){
                    System.out.println("403");
                }

                if(responseCode == 404){
                    System.out.println("404");
                }

                if(responseCode == 500){
                    System.out.println("500");
                }


               return false;
           }
   
   
       } catch (Exception e) {
           e.printStackTrace();
       }
   
       return false;
   }
   
       /*
        * En JSON variabel skapas som innehåller sessionDID, själva texten samt
        tiden som JSON variabeln skapades. JSON strukturen följer reglerna för hur
        Blueskys API förväntar sig att den ska vara.
        */
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
