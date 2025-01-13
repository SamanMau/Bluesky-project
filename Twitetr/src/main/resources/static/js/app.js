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

const suggestionsContainer = document.querySelector('.suggestions');

// Uppdaterar teckenräknaren och aktiverar/inaktiverar sänd-knappen
quill.on('text-change', () => {
    const text = quill.getText().trim();
    charCounter.textContent = `${text.length} / 300`;
    submitButton.disabled = text.length === 0 || text === '\n' || text.length > 300;
});



// Validerar textinput innan det skickas till backend
function validateTextInput(text) {

    if (!text || text.trim().length === 0) {
        alert('Please write something before checking spelling.');
        return false;
    }

    if (text.length > 300) {
        alert('The text exceeds the limit of 300 characters.');
        return false;
    }

    return true;
}

// Hanterar stavningskontroll
document.querySelector('.check-spelling').addEventListener('click', () => {
    const text = quill.getText().replace(/\n/g, '').trim(); // Remove newlines and trim whitespace

    if (!validateTextInput(text)) return;

    

    fetch('http://127.0.0.1:8080/api/text/manage-text', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        // Standardspråket är svenska
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
                    quill.setText(data.correctedText); // Update editor with corrected text
                    console.log("Text updated to:", data.correctedText);
                    
                    const updatedText = quill. getText().trim();
                    charCounter.textContent = `${updatedText.length} / 300`;
                    submitButton.disabled = updatedText.length === 0 || updatedText.length > 300;
                });
            } else if (data.message) {
                suggestionsContainer.innerHTML = 
                `<h3>No Corrections Found:</h3><p>${data.message}</p>`;
            } else if (data.invalid ) {
                suggestionsContainer.innerHTML = `
                    <h3>Error:</h3><p>${data.invalid}</p>`;
            }
        })
        .catch(error => {
            console.error('Error during fetch:', error);
            alert(`Error: ${error.message}`); 
        });
});


// Hanterar sänd-knappen
submitButton.addEventListener('click', async (e) => {
    e.preventDefault(); 

    const text = quill.getText().trim();
    

    if (!validateTextInput(text)) {
        return; 
    }

    try {
        const response = await fetch('http://127.0.0.1:8080/api/text/post-text', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ userText: text }),
        });

        // Kontrollera om responsen är OK
        if (!response.ok) {
            const errorResponse = await response.json();
            console.error("Publish Error Response:", errorResponse);
            alert(errorResponse.message || "Unknown error occurred.");
            return;
        }

        // Tolka JSON-responsen
        const data = await response.json();
        console.log("Response from /post-text:", data);

        // Hantera framgång eller oväntad respons
        if (data.status === "success") {
            alert(`Success: ${data.message}`);
        } else {
            alert(`Error: ${data.message}`);
        }
    } catch (error) {
        // Logga nätverksfel eller oväntade problem
        console.error("Error during publish:", error);
        alert(`A network or server error occurred: ${error.message || "Unknown error"}`);
    }
});
