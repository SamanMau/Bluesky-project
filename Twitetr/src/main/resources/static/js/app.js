// const quill = new Quill('#editor', {
//     theme: 'snow',
//     placeholder: 'Write your post here...',
//     modules: {
//         toolbar: [
//             [{ header: [1, 2, false] }],
//             ['bold', 'italic', 'underline', 'strike'],
//             ['link', 'image'],
//             [{ list: 'ordered' }, { list: 'bullet' }],
//             ['clean'],
//         ],
//     },
// });

// const charCounter = document.getElementById('char-counter');
// const submitButton = document.getElementById('submit-button');
// const loader = document.getElementById('loader');
// const previewContainer = document.getElementById('preview-container');
// const tweetPreview = document.getElementById('tweet-preview');

// // Update character counter and enable/disable submit button
// quill.on('text-change', () => {
//     const text = quill.getText().trim();
//     charCounter.textContent = `${text.length} / 280`;
//     submitButton.disabled = text.length === 0 || text.length > 280;

//     // Update preview
//     tweetPreview.innerHTML = quill.root.innerHTML;
//     previewContainer.classList.remove('hidden');
// });

// submitButton.addEventListener('click', async (e) => {
//     e.preventDefault(); // Förhindra sidladdning

//     const tweetContent = quill.getContents(); // Hämta rich text (Delta-format)
//     const plainText = quill.getText().trim(); // Hämta ren text

//     if (plainText.length === 0 || plainText.length > 280) {
//         alert('Tweet must be between 1 and 280 characters.');
//         return;
//     }

//     try {
//         // Skicka tweet som JSON till backend
//         const response = await fetch('http://localhost:8080/api/threads/manage-thread', {
//             method: 'POST', // POST-begäran för att skicka data
//             headers: { 'Content-Type': 'application/json' }, // Skickar JSON-format
//             body: JSON.stringify({
//                 tweet: plainText, // Endast tweet behövs
//                 language: 'sv', //bara tillfällig
//             }),
//         });

//         if (response.ok) {
//             const result = await response.json();
//             alert(`Tweet before: ${result.before}\nTweet after: ${result.after}`);
//         } else {
//             const errorMessage = await response.text(); // Få backendens felmeddelande
//             alert(`Failed to post the tweet. Reason: ${errorMessage}`);
//         }
//     } catch (error) {
//         const errorMessage = await response.text(); // Hämta felmeddelande
//         alert(`Failed to post the tweet. Reason: ${errorMessage}`);
//     }
// });

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

// Handle Check Spelling
document.querySelector('.check-spelling').addEventListener('click', () => {
    const text = quill.getText().trim();

    if (!isTextValid(text)) { // Validate text
        alert('Please write something before checking spelling.');
        return;
    }

    loader.style.display = 'block';

    fetch('/api/text/manage-text', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({userText: text, language: selectedLanguage || 'sv' }), //tillfälligt, att default är svenska //saman
    })
        .then(response => response.json())
        .then(data => {
            loader.style.display = 'none';
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
            loader.style.display = 'none';
            console.error('Error while checking spelling:', error);
            alert('An error occurred while checking spelling.');
        });
});

// Handle Submit Button
submitButton.addEventListener('click', () => {
    const text = quill.getText();

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
