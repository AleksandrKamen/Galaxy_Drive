document.addEventListener("DOMContentLoaded", function () {
    var popup = document.getElementById("popup");
    var closeButton = document.getElementById("close-popup");
    var acceptButton = document.getElementById("accept");

    if (document.cookie.indexOf("popupShown=true") === -1) {
        popup.style.display = "block";
    }
    acceptButton.addEventListener("click", function () {
        popup.style.display = "none";
        document.cookie = "popupShown=true; expires=" + new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toUTCString();
    });

    closeButton.addEventListener("click", function () {
        popup.style.display = "none";
        document.cookie = "popupShown=true; expires=" + new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toUTCString();
    });
});