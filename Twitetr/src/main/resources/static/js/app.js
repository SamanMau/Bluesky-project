document.addEventListener("DOMContentLoaded", function () {
    const loginForm = document.getElementById("loginForm");

    if (loginForm) {
        loginForm.addEventListener("submit", sendLoginInfo);
        return; // Stoppa exekvering av resten av koden om vi är på login-sidan
    }
    
    
    const editor = document.getElementById("editor");
    const charCounter = document.getElementById("char-counter");
    const checkSpellingButton = document.querySelector(".check-spelling");
    const replaceButton = document.getElementById("replace-text");
    const submitButton = document.getElementById("submit-button");
    const suggestionsContainer = document.querySelector(".suggestions");

    // Hide "Replace With New Text" button by default
    replaceButton.style.display = "none";

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

        fetch("http://127.0.0.1:8080/api/text/manage-text", {
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

    function sendLoginInfo(event) {
        event.preventDefault();
    
        const username = document.getElementById("logInUsername").value;
        const password = document.getElementById("logInPassword").value;
    
        fetch("http://127.0.0.1:8080/api/text/login-info", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                userName: username,
                password: password
            })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                console.log("Login successful:", data);
                alert("✅ Inloggning lyckades!");
            } else {
                throw new Error(data.message || "Inloggning misslyckades");
            }
        })
        .catch(error => {
            console.error("Error:", error);
            alert(`Kunde inte logga in: ${error.message}`);
        });
    }
    
      
    // Submit Post
    submitButton.addEventListener("click", async (e) => {
        e.preventDefault();
        const text = editor.value.trim();

        if (!validateTextInput(text, "submitting")) return;

        try {
            const response = await fetch("http://127.0.0.1:8080/api/text/post-text", {
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
