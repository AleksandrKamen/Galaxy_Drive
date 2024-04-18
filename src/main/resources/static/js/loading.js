function disableButton(button) {
    const loadingText = document.getElementById('loadingText').textContent;
    if (button.form.checkValidity()) {
        button.innerHTML = loadingText;
        button.disabled = true;
        setTimeout(function() {
            button.form.submit();
        }, 1000);
    } else {
        button.disabled = false;
    }
}

