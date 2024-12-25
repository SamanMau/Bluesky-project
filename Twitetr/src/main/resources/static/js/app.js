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

// Global variable for selected language
let selectedLanguage = 'en'; // Default is English

// Element references
const charCounter = document.getElementById('char-counter');
const submitButton = document.getElementById('submit-button');
const loader = document.getElementById('loader');
const suggestionsContainer = document.querySelector('.suggestions');

// Update character counter and enable/disable submit button
quill.on('text-change', () => {
    const text = quill.getText().trim();
    charCounter.textContent = `${text.length} / 500`;
    submitButton.disabled = text.length === 0 || text === '\n' || text.length > 500;
});

// Helper function to check if text is valid
function isTextValid(text) {
    return text.trim().length > 0;
}

// Handle Language Change
document.querySelectorAll('.language-select button').forEach(button => {
    button.addEventListener('click', (event) => {
        selectedLanguage = event.target.textContent.trim().toLowerCase();
        alert(`Language changed to: ${selectedLanguage === 'en' ? 'English' : 'Swedish'}`);
    });
});

// Validate text input before sending to backend
function validateTextInput(text) {
    console.log("Validating text input:", text); // Log text input for debugging
    if (!text || text.trim().length === 0) {
        alert('Please write something before checking spelling.');
        return false;
    }

    if (text.length > 500) {
        alert('The text exceeds the limit of 500 characters.');
        return false;
    }

    return true;
}

// Handle Check Spelling
document.querySelector('.check-spelling').addEventListener('click', () => {
    const text = quill.getText().replace(/\n/g, '').trim(); // Remove newlines and trim whitespace
    
    console.log("Check spelling button clicked");
    console.log("Text to send: ", text);

    if (!validateTextInput(text)) {
        return;
    }

    fetch('/api/text/manage-text', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({userText: text, language: selectedLanguage || 'sv' }), //tillfälligt, att default är svenska //saman
    })
        .then(response => {
            console.log("Fetch response status:", response.status); // Log response status
            
            if(!response.ok){
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            
            return response.json();

        }).then(data => {
            console.log("Response from backend: ", data);
            if (data.before && data.after) {
                alert('Spelling check complete!');
                suggestionsContainer.innerHTML = `
                    <h3>Before:</h3>
                    <p>${data.before}</p>
                    <h3>After:</h3>
                    <p>${data.after}</p>
                `;
            } else if (data.invalid) {
                alert(data.invalid);
            }
        })
        .catch(error => {
            console.error('Error while checking spelling:', error);
            alert('An error occurred while checking spelling.');
        });
});

// Handle Submit Button
submitButton.addEventListener('click', () => {
    const text = quill.getText();

    console.log("Text to publish:", text); // Log text to be published

    if (!isTextValid(text)) { // Validate text
        alert('Please write something before submitting.');
        return;
    }

    loader.style.display = 'block';

    fetch('/api/text/post-text', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({userText: text.trim()}),
    })
        .then(response => response.text())
        .then(message => {
            loader.style.display = 'none';
            alert(message);
        })
        .catch(error => {
            loader.style.display = 'none';
            console.error('Error while submitting text:', error);
            alert('An error occurred while submitting your text.');
        });
});
