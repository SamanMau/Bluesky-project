# Twitetr-project
Projektnamn
Tweet Manager

Ide
Projektet, "Twitetr", är en webbtjänst där användare kan skriva tweets och få dem stavningskontrollerade innan de publiceras. Tweetsen analyseras med LIBRIS stavnings-API, och användaren får stavningsförslag för att korrigera eventuella fel. Efter godkännande kan tweets publiceras och spridas.

Lista över tjänster med webb-APIer
Twitter API: Används för att publicera och hantera tweets.
LIBRIS stavnings-API: Används för att kontrollera stavning och generera stavningsförslag.

Teknikstack
Frontend:
Språk: HTML, CSS, JavaScript.
Beskrivning: Frontend skickar tweettext via ett HTTP-anrop (t.ex. POST). Bygg ett användarvänligt gränssnitt för tweet-skrivning och felhantering.

Backend:
Språk: Java.
Beskrivning: Hantera API-anrop, logik och datautbyte med frontend. Backend bearbetar texten (stavningskontroll eller publicering) och skickar ett JSON-svar tillbaka.

Verktyg
REST API-implementering med stöd för JSON kommer att användas för backend. Den kommer att användas för att integrera med LIBRIS stavnings-API och Twitter API. Dessa API:er kommunicerar vanligtvis via JSON.

För frontend kommer vi kanske använda Chrome DevTools för felsökning och testning av gränssnittet. 

Roller
När det gäller uppdelning av roller, så är det tänkt att de som är erfarna av att jobba med java, kommer att göra cirka 75% av backend. Medan eleverna i IA gör 25% av backend. När det gäller frontend, kommer IA eleverna göra 75% av frontend, medan systemutvecklarna gör 25% av frontend. 

Ramverk
Spring

Instruktioner i readme
Det ska finnas bra och mycket tydliga instruktioner i vår readme om hur vi kör vårt program. Be några andra kursare att testa instruktionerna, därifrån kan vi se om våra instruktioner är tydliga eller inte. 
