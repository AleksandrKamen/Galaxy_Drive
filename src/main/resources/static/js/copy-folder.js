document.addEventListener('DOMContentLoaded', function () {
    var copyBtns = document.querySelectorAll('.copy-folder-btn');
    var copyModal = document.getElementById('copyFolderModal');

    copyBtns.forEach(function (btn) {
        btn.addEventListener('click', function () {
            var fullName = btn.getAttribute('data-folder-name');
            var onlypath = fullName.substring(0, fullName.length - 1);
            var lastIndex = onlypath.lastIndexOf('/');
            var name = onlypath.substring(lastIndex + 1);


            copyModal.querySelector('[name="currentName"]').value = fullName;
            copyModal.querySelector('[name="copyFolderName"]').value = name;
        });
    });
});