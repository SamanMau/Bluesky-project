document.addEventListener("DOMContentLoaded", () => {
    const quill = new Quill('#editor', {
        theme: 'snow',
        placeholder: 'Write your tweet here...',
        modules: {
            toolbar: [
                ['bold', 'italic', 'underline'],
                [{ 'list': 'ordered' }, { 'list': 'bullet' }],
                ['link']
            ]
        }
    });

    const charCounter = document.getElementById('char-counter');
    const checkSpellingButton = document.getElementById('check-spelling-button');
    const acceptSuggestionsButton = document.getElementById('accept-suggestions-button');
    const editSuggestionsButton = document.getElementById('edit-suggestions-button');
    const publishButton = document.getElementById('publish-button');
    const hiddenContent = document.getElementById('hidden-content');
    const form = document.getElementById('tweetForm');

    quill.on('text-change', () => {
        const text = quill.getText().trim();
        charCounter.textContent = `${text.length} / 280`;
        publishButton.disabled = !(text.length > 0 && text.length <= 280);
    });

    checkSpellingButton.addEventListener('click', () => {
        alert('Checking spelling...');
        acceptSuggestionsButton.classList.remove('hidden');
        editSuggestionsButton.classList.remove('hidden');
    });

    acceptSuggestionsButton.addEventListener('click', () => {
        alert('Suggestions accepted.');
        publishButton.classList.remove('hidden');
    });

    editSuggestionsButton.addEventListener('click', () => {
        alert('Editing suggestions.');
    });

    form.addEventListener('submit', (event) => {
        event.preventDefault();
        const text = quill.getText().trim();
        hiddenContent.value = text;
        alert(`Tweet submitted: ${text}`);
    });

    document.getElementById('language').addEventListener('change', (event) => {
        alert(`Language changed to: ${event.target.value}`);
    });
});
