function disableButton(button) {
    const loadingText = document.getElementById('loadingText').textContent;
    button.innerHTML = loadingText;
    button.disabled = true;

    if (button.form.checkValidity()) {
        setTimeout(function() {
            button.form.submit();
        }, 1000);
    } else {
        button.disabled = false;
    }
}

