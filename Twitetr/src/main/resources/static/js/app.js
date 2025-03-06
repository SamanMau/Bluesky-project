document.addEventListener("DOMContentLoaded", function () {
    const loginForm = document.getElementById("loginForm");
    const signUpForm = document.getElementById("signupForm");

    if (loginForm) {
        loginForm.addEventListener("submit", sendLoginInfo);
    }

    if(signUpForm){
        signUpForm.addEventListener("submit", sendSignupInfo);
        return;
    }
    
    
    const signOutButton = document.getElementById("signOutForm");
    const editor = document.getElementById("editor");
    const charCounter = document.getElementById("char-counter");
    const checkSpellingButton = document.querySelector(".check-spelling");
    const replaceButton = document.getElementById("replace-text");
    const submitButton = document.getElementById("submit-button");
    const suggestionsContainer = document.querySelector(".suggestions");

    // Hide "Replace With New Text" button by default
    replaceButton.style.display = "none";

    signOutButton.addEventListener("submit", signOut);

    // Character counter update
    editor.addEventListener("input", function () {
        const textLength = editor.value.trim().length;
        charCounter.textContent = `${textLength} / 300`;

        // Enable or disable submit button
        submitButton.disabled = textLength === 0 || textLength > 300;
    });

    // Validate text input
    function validateTextInput(text, action) {
        if (!text || text.trim().length === 0) {
            alert(`⚠ Please write something before ${action}.`);
            return false;
        }

        if (text.length > 300) {
            alert(`🚨 Your text exceeds the limit of 300 characters. Please shorten it before ${action}.`);
            return false;
        }

        return true;
    }

    // Check Spelling
    checkSpellingButton.addEventListener("click", () => {
        const text = editor.value.trim();

        if (!validateTextInput(text, "checking your spelling")) return;

        fetch("http://127.0.0.1:8080/api/text/text-validation", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ userText: text, language: "sv" }),
        })
            .then(response => response.json())
            .then(data => {
                if (data.originalText && data.correctedText) {
                    suggestionsContainer.innerHTML = `
                        <h3>Original Text:</h3>
                        <p>${data.originalText}</p>
                        <h3>Corrected Text:</h3>
                        <p class="clickable-suggestion" style="cursor: pointer; color: black;">${data.correctedText}</p>
                    `;

                    const suggestionElement = document.querySelector(".clickable-suggestion");
                    suggestionElement.addEventListener("click", () => {
                        editor.value = data.correctedText;
                        charCounter.textContent = `${data.correctedText.length} / 300`;
                        submitButton.disabled = data.correctedText.length === 0 || data.correctedText.length > 300;
                    });

                    // Show "Replace With New Text" button
                    replaceButton.style.display = "block";
                } else {
                    suggestionsContainer.innerHTML = `<h3>No Corrections Found:</h3><p>${data.message || "No spelling errors detected."}</p>`;
                }
            })
            .catch(error => {
                console.error("Error:", error);
                alert("Error processing the request.");
            });
    });

    // Replace Button functionality
    replaceButton.addEventListener("click", () => {
        const correctedTextElement = document.querySelector(".clickable-suggestion");
        if (correctedTextElement) {
            editor.value = correctedTextElement.textContent.trim();
            charCounter.textContent = `${editor.value.length} / 300`;
            submitButton.disabled = editor.value.length === 0 || editor.value.length > 300;
        }
    });

    function signOut(event){
        event.preventDefault();
        window.location.href = "login.html";
    }

    function sendLoginInfo(event) {
        event.preventDefault();
    
        const username = document.getElementById("logInUsername").value;
        const password = document.getElementById("logInPassword").value;
    
        fetch("http://127.0.0.1:8080/api/text/session", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                userName: username,
                password: password
            })
        })
        .then(response => response.text())
        .then(text => {

            // konvertera texten "true" eller "false" till en riktig boolean
            const isSuccess = text.trim() === "true";
    
            if (isSuccess) {
                window.location.href = "index.html";
                alert("💻 Log in successful. Press ok");
            } else {
                alert(`❌ Could not log in. Either the user does not exist or you have entered incorrect login details.`);
            }
        })
        .catch(error => {
            alert(`❌ Could not log in. Either the user does not exist or you have entered incorrect login details.`);
        });
    }

    function sendSignupInfo(event) {
        event.preventDefault();
    
        const username = document.getElementById("signupUsername").value;
        const password = document.getElementById("signupPassword").value;
    
        fetch("http://127.0.0.1:8080/api/text/users", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                userName: username,
                password: password
            })
        })
        .then(response => response.text())
        .then(text => {

            // konvertera texten "true" eller "false" till en riktig boolean
            const isSuccess = text.trim() === "true";
    
            if (isSuccess) {
                window.location.href = "index.html";
                alert("💻 Account created successfully");
            } else {
                alert(`❌ The email is already in use.`);
            }
        })
        .catch(error => {
            alert(`❌ Could not log in. Either the user does not exist or you have entered incorrect login details.`);
        });
    }
    
    
      
    // Submit Post
    submitButton.addEventListener("click", async (e) => {
        e.preventDefault();
        const text = editor.value.trim();

        if (!validateTextInput(text, "submitting")) return;

        try {
            const response = await fetch("http://127.0.0.1:8080/api/text/texts", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ userText: text }),
            });

            const data = await response.json();
            alert(data.status === "success" ? `✅ Success: ${data.message}` : `❌ Error: ${data.message}`);
        } catch (error) {
            console.error("Error:", error);
            alert("🚨 A network or server error occurred.");
        }
    });
});
