document.addEventListener('DOMContentLoaded', function () {
    var renameBtns = document.querySelectorAll('.rename-folder-btn');
    var renameModal = document.getElementById('renameFolderModal');
    renameBtns.forEach(function (btn) {
        btn.addEventListener('click', function () {
            var fullName = btn.getAttribute('data-folder-name');
            var onlypath = fullName.substring(0, fullName.length - 1);
            var lastIndex = onlypath.lastIndexOf('/');
            var name = onlypath.substring(lastIndex + 1);


            renameModal.querySelector('[name="currentName"]').value = fullName;
            renameModal.querySelector('[name="newFolderName"]').value = name;
        });
    });
});