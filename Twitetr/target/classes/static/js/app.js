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

const charCounter = document.getElementById('char-counter');
const submitButton = document.getElementById('submit-button');
const loader = document.getElementById('loader');
const previewContainer = document.getElementById('preview-container');
const tweetPreview = document.getElementById('tweet-preview');

// Update character counter and enable/disable submit button
quill.on('text-change', () => {
    const text = quill.getText().trim();
    charCounter.textContent = `${text.length} / 280`;
    submitButton.disabled = text.length === 0 || text.length > 280;

    // Update preview
    tweetPreview.innerHTML = quill.root.innerHTML;
    previewContainer.classList.remove('hidden');
});

submitButton.addEventListener('click', async (e) => {
    e.preventDefault(); // Förhindra sidladdning

    const tweetContent = quill.getContents(); // Hämta rich text (Delta-format)
    const plainText = quill.getText().trim(); // Hämta ren text

    if (plainText.length === 0 || plainText.length > 280) {
        alert('Tweet must be between 1 and 280 characters.');
        return;
    }

    try {
        // Skicka tweet som JSON till backend
        const response = await fetch('api/tweets/test-manage-tweet', {
            method: 'POST', // POST-begäran för att skicka data
            headers: { 'Content-Type': 'application/json' }, // Skickar JSON-format
            body: JSON.stringify({ content: plainText }), // JSON-struktur för tweet
        });

        if (response.ok) {
            const result = await response.json(); // Hämta JSON-svar från backend
            alert(`Tweet saved: ${result.message}`); // Bekräfta sparad tweet
            quill.setText(''); // Rensa editorn
            charCounter.textContent = '0 / 280'; // Återställ räknaren
        } else {
            alert('Failed to post the tweet. Try again!');
        }
    } catch (error) {
        console.error('Error posting tweet:', error);
        alert('Something went wrong. Please try again!');
    }
});
