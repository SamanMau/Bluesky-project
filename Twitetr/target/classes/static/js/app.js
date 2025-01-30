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
const replaceButton = document.getElementById('replace-text');
const suggestionsContainer = document.querySelector('.suggestions');

// Hide "Replace" button by default
replaceButton.style.display = 'none';

// Update character counter and enable/disable submit button
quill.on('text-change', () => {
    const text = quill.getText().trim();
    charCounter.textContent = `${text.length} / 300`;
    submitButton.disabled = text.length === 0;
});

// Validate text input before sending to backend
function validateTextInput(text, action) {
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

// Handle Check Spelling
document.querySelector('.check-spelling').addEventListener('click', () => {
    const text = quill.getText().replace(/\n/g, '').trim(); // Remove newlines and trim whitespace

    if (!validateTextInput(text, 'check-spelling')) return;

    fetch('http://127.0.0.1:8080/api/text/manage-text', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        // Default language is Swedish
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
            alert(`Error: ${error.message}`); // Display backend error in alert
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

// Handle Submit Button
submitButton.addEventListener('click', async (e) => {
    e.preventDefault(); // Prevent page reload

    const text = quill.getText().trim();

    if (text.length === 0) {
        alert('⚠ Please write something before submitting.');
        return; // Stop submission if text is empty
    }

    // Validate text before submission
    if (!validateTextInput(text, 'submit')) {
        return; // Stop submission if text is too long or empty
    }

    try {
        const response = await fetch('http://127.0.0.1:8080/api/text/post-text', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ userText: text }),
        });

        if (!response.ok) {
            const errorResponse = await response.json();
            console.error("Publish Error Response:", errorResponse);
            alert(errorResponse.message || "Unknown error occurred.");
            return;
        }

        const data = await response.json();
        console.log("Response from /post-text:", data);

        if (data.status === "success") {
            alert(`✅ Success: ${data.message}`);
        } else {
            alert(`❌ Error: ${data.message}`);
        }
    } catch (error) {
        console.error("Error during publish:", error);
        alert(`🚨 A network or server error occurred: ${error.message || "Unknown error"}`);
    }
});
