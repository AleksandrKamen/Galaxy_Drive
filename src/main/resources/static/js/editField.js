function editField(elementId) {
    var currentText = document.getElementById(elementId).innerText;
    var newInput = document.createElement('input');
    newInput.setAttribute('type', 'text');
    newInput.setAttribute('value', currentText);
    newInput.setAttribute('maxlength', 20);

    var spanElement = document.getElementById(elementId);
    spanElement.parentNode.replaceChild(newInput, spanElement);

    var closeButton = document.getElementById('close-btn');
    var currnetNameInput = document.getElementById('currnetNameInput');
    var lastNameInput = document.getElementById('lastNameInput');

    newInput.addEventListener('blur', function () {
        var newText = newInput.value;
        var newSpan = document.createElement('span');
        newSpan.innerText = newText;
        newSpan.id = elementId;
        newInput.parentNode.replaceChild(newSpan, newInput);

        if (elementId === 'currnetName') {
            currnetNameInput.value = newText;
        } else if (elementId === 'lastName') {
            lastNameInput.value = newText;
        }
    });

    closeButton.addEventListener('click', function () {
        var spanElement = document.getElementById(elementId);
        var newSpan = document.createElement('span');
        newSpan.innerText = currentText;
        newSpan.id = elementId;
        spanElement.parentNode.replaceChild(newSpan, spanElement);
    });

    newInput.focus();
}