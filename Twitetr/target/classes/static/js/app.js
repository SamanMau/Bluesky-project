// Initialiserar Quill.js-redigeraren
const quill = new Quill('#editor', {
    theme: 'snow',
    placeholder: 'Write your post here...',
    modules: {
        toolbar: [
            [{ header: [1, 2, false] }],
            ['bold', 'italic', 'underline', 'strike'],
            ['link', 'image'],
            [{ list: 'ordered' }, { list: 'bullet' }],
            ['clean'],
        ],
    },
});

// Referenser till HTML-element
const charCounter = document.getElementById('char-counter');
const submitButton = document.getElementById('submit-button');
const replaceButton = document.getElementById('replace-text');
const suggestionsContainer = document.querySelector('.suggestions');

<<<<<<< HEAD
// Uppdaterar teckenräknaren och aktiverar/inaktiverar sänd-knappen
=======
// Hide "Replace" button by default
replaceButton.style.display = 'none';

// Update character counter and enable/disable submit button
>>>>>>> Backend_karam_ny
quill.on('text-change', () => {
    const text = quill.getText().trim();
    charCounter.textContent = `${text.length} / 300`;
    submitButton.disabled = text.length === 0;
});

<<<<<<< HEAD


// Validerar textinput innan det skickas till backend
function validateTextInput(text) {

=======
// Validate text input before sending to backend
function validateTextInput(text, action) {
>>>>>>> Backend_karam_ny
    if (!text || text.trim().length === 0) {
        if (action === 'check-spelling') {
            alert('⚠ Please write something before checking your spelling.');
        } else {
            alert('⚠ Please write something before submitting your text.');
        }
        return false;
    }

    if (text.length > 300) {
        if (action === 'check-spelling') {
            alert('🚨 Error: Your text exceeds the limit of 300 characters. Please shorten it before checking your spelling.');
        } else {
            alert('🚨 Error: Your text exceeds the limit of 300 characters. Please shorten it before submitting.');
        }
        return false;
    }

    return true;
}

// Hanterar stavningskontroll
document.querySelector('.check-spelling').addEventListener('click', () => {
    const text = quill.getText().replace(/\n/g, '').trim(); 

    if (!validateTextInput(text, 'check-spelling')) return;

    fetch('http://127.0.0.1:8080/api/text/manage-text', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
<<<<<<< HEAD
        // Standardspråket är svenska
=======
        // Default language is Swedish
>>>>>>> Backend_karam_ny
        body: JSON.stringify({ userText: text, language: 'sv' }),
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log("Response from /manage-text: ", data);
            if (data.originalText && data.correctedText) {
                suggestionsContainer.innerHTML = `
                    <h3>Original Text:</h3>
                    <p>${data.originalText}</p>
                    <h3>Corrected Text:</h3>
                    <p class="clickable-suggestion" style="cursor: pointer; color: black; text-decoration: none;">${data.correctedText}</p>
                `;

                const suggestionElement = document.querySelector('.clickable-suggestion');
                suggestionElement.addEventListener('click', () => {
                    quill.setText(data.correctedText); 
                    console.log("Text updated to:", data.correctedText);

                    const updatedText = quill.getText().trim();
                    charCounter.textContent = `${updatedText.length} / 300`;
                    submitButton.disabled = updatedText.length === 0 || updatedText.length > 300;
                });

                // Enable and show "Replace With New Text" button
                replaceButton.style.display = 'block';
                replaceButton.disabled = false;
            } else if (data.message) {
                suggestionsContainer.innerHTML = `<h3>No Corrections Found:</h3><p>${data.message}</p>`;
            } else if (data.invalid) {
                suggestionsContainer.innerHTML = `<h3>Error:</h3><p>${data.invalid}</p>`;
            }
        })
        .catch(error => {
            console.error('Error during fetch:', error);
<<<<<<< HEAD
            alert(`Error: ${error.message}`); 
=======
            alert(`Error: ${error.message}`); // Display backend error in alert
>>>>>>> Backend_karam_ny
        });
});

// Handle Replace Button
replaceButton.addEventListener('click', () => {
    const correctedTextElement = document.querySelector('.clickable-suggestion');

    if (correctedTextElement) {
        const correctedText = correctedTextElement.textContent.trim();

        if (correctedText) {
            quill.setText(correctedText); // Replace text in editor
            console.log("Text replaced with corrected version:", correctedText);

            // Update character counter
            const updatedText = quill.getText().trim();
            charCounter.textContent = `${updatedText.length} / 300`;
            submitButton.disabled = updatedText.length === 0 || updatedText.length > 300;
        } else {
            alert("⚠ No corrected text available.");
        }
    } else {
        alert("⚠ No corrected text available.");
    }
});

// Hanterar sänd-knappen
submitButton.addEventListener('click', async (e) => {
    e.preventDefault(); 

    const text = quill.getText().trim();

<<<<<<< HEAD
    if (!validateTextInput(text)) {
        return; 
=======
    if (text.length === 0) {
        alert('⚠ Please write something before submitting.');
        return; // Stop submission if text is empty
    }

    // Validate text before submission
    if (!validateTextInput(text, 'submit')) {
        return; // Stop submission if text is too long or empty
>>>>>>> Backend_karam_ny
    }

    try {
        const response = await fetch('http://127.0.0.1:8080/api/text/post-text', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ userText: text }),
        });

<<<<<<< HEAD
        // Kontrollera om responsen är OK
=======
>>>>>>> Backend_karam_ny
        if (!response.ok) {
            const errorResponse = await response.json();
            console.error("Publish Error Response:", errorResponse);
            alert(errorResponse.message || "Unknown error occurred.");
            return;
        }

<<<<<<< HEAD
        // Tolka JSON-responsen
        const data = await response.json();
        console.log("Response from /post-text:", data);

        // Hantera framgång eller oväntad respons
=======
        const data = await response.json();
        console.log("Response from /post-text:", data);

>>>>>>> Backend_karam_ny
        if (data.status === "success") {
            alert(`✅ Success: ${data.message}`);
        } else {
            alert(`❌ Error: ${data.message}`);
        }
    } catch (error) {
<<<<<<< HEAD
        // Logga nätverksfel eller oväntade problem
=======
>>>>>>> Backend_karam_ny
        console.error("Error during publish:", error);
        alert(`🚨 A network or server error occurred: ${error.message || "Unknown error"}`);
    }
});
