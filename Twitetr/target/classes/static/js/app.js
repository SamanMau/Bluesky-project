// Initialize Quill.js editor
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

// Element references
const charCounter = document.getElementById('char-counter');
const submitButton = document.getElementById('submit-button');

const suggestionsContainer = document.querySelector('.suggestions');

// Update character counter and enable/disable submit button
quill.on('text-change', () => {
    const text = quill.getText().trim();
    charCounter.textContent = `${text.length} / 300`;
    submitButton.disabled = text.length === 0 || text === '\n' || text.length > 300;
});



// Validate text input before sending to backend
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

// Handle Check Spelling
document.querySelector('.check-spelling').addEventListener('click', () => {
    const text = quill.getText().replace(/\n/g, '').trim(); // Remove newlines and trim whitespace

    if (!validateTextInput(text)) return;

    // console.log("Check spelling button clicked");
    // console.log("Text to send: ", text);

    fetch('http://127.0.0.1:8080/api/text/manage-text', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        // default language is Swedish
        body: JSON.stringify({ userText: text, language: 'sv' }),
    })
        .then(response => {
            // console.log("Fetch response:", response);

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
            } else if (data.invalid) {
                suggestionsContainer.innerHTML = `<h3>Error:</h3><p>${data.invalid}</p>`;
            } else {
                suggestionsContainer.innerHTML = `
                    <h3>No Corrections Found:</h3>
                    <p>The text might already be correct.</p>
                `;
            }
        })
        .catch(error => {
            console.error('Error during fetch:', error);
            alert(`Error: ${error.message}`); // Visa backend-meddelandet i alert
        });
});


// Handle Submit Button
submitButton.addEventListener('click', () => {
    const text = quill.getText().trim();
    if (!validateTextInput(text)) return;

    if (!isTextValid(text)) {
        alert('Please write something before submitting.');
        return;
    }

    fetch('http://127.0.0.1:8080/api/text/post-text', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ userText: text }),
    })
        .then(response => {
            if (!response.ok) {
                console.error(`Publish Error: ${response.status} - ${response.statusText}`);
                throw new Error(`Failed to publish: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {

            console.log("Response data for publish: ", data);

            if (data.status === "success") {
                alert('Text successfully published!');
            } else if (data.error) {
                alert(`Failed to publish: ${data.error}`);
            } else {
                alert('Unknown response from server.');
            }
        })
        .catch(error => {
            console.error('Error during publishing:', error);
            alert(`An error occurred. Please try again later.`);
        });
});



// FÖR INTEGRATIONEN FÖR BACKEND
submitButton.addEventListener('click', async (e) => {
    e.preventDefault(); // Förhindra sidladdning

    const plainText = quill.getText().trim();

    // Kontrollera att texten är mellan 1 och 280 tecken
    if (plainText.length === 0 || plainText.length > 300) {
        alert('Tweet must be between 1 and 300 characters.');
        return;
    }

    try {
        // Skicka text till backend och hämta svar
        const response = await fetch('http://127.0.0.1:8080/api/text/manage-text', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ userText: plainText, language: 'sv' })
        });

        if (response.ok) {
            const result = await response.json();

            // Visa resultatet i frontend
            tweetPreview.innerHTML = `
                <p><strong>LIBRIS suggestions:</strong> ${result['LIBRIS suggestions']}</p>
                <p><strong>User original tweet:</strong> ${result['User original tweet']}</p>
            `;
            previewContainer.classList.remove('hidden');
        } else {
            // alert('Failed to process the tweet. Try again!');
            alert('Error while submitting text:', error.message);
        }
    } catch (error) {
        // console.error('Error communicating with backend:', error);
        // alert('Something went wrong. Please try again!');
    }
});