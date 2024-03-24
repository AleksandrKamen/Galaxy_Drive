document.addEventListener('DOMContentLoaded', function (){
    var renameBtns = document.querySelectorAll('.rename-file-btn');
    var renameModal = document.getElementById('renameFileModal');

    renameBtns.forEach(function(btn) {
        btn.addEventListener('click', function() {
            var fullName = btn.getAttribute('data-file-name');
            var indexOf = fullName.indexOf('.');
            var lastIndexOf = fullName.lastIndexOf("/") +1;
            var name = fullName.substring(indexOf,lastIndexOf);

            renameModal.querySelector('[name="currentName"]').value = fullName;
            renameModal.querySelector('[name="newFileName"]').value = name;
        });
    });
});
