document.addEventListener('DOMContentLoaded', function () {
    const togglePasswordButtons = document.querySelectorAll('.toggle-password');
    const showBtnText = document.getElementById('showBtnText').textContent;
    const hideBtnText = document.getElementById('hideBtnText').textContent;

    togglePasswordButtons.forEach(function (button) {
        button.addEventListener('click', function () {
            const passwordInput = button.previousElementSibling;
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            button.textContent = type === 'password' ? showBtnText : hideBtnText;
        });
    });
});
