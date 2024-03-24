document.addEventListener('DOMContentLoaded', function () {
var copyBtns = document.querySelectorAll('.copy-file-btn');
var copyModal = document.getElementById('copyFileModal');

copyBtns.forEach(function(btn) {
    btn.addEventListener('click', function() {
        var fullName = btn.getAttribute('data-file-name');
        var indexOf = fullName.indexOf('.');
        var lastIndexOf = fullName.lastIndexOf("/") +1;
        var name = fullName.substring(indexOf,lastIndexOf);


        copyModal.querySelector('[name="currentName"]').value =  fullName;
        copyModal.querySelector('[name="copyFileName"]').value = name;
    });
});
});