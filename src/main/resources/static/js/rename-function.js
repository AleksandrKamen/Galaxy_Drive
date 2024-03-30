function renameObject() {
    var renameBtns = document.querySelectorAll('.rename-btn');
    var renameModal = document.getElementById('renameModal');

    renameBtns.forEach(function (btn) {
        btn.addEventListener('click', function () {
            var fullName = btn.getAttribute('data-object-name');
            var type = btn.getAttribute('data-object-type');
            var newName = '';

            if (type === 'file') {
                var indexOf = fullName.indexOf('.');
                var lastIndexOf = fullName.lastIndexOf("/") + 1;
                newName = fullName.substring(indexOf, lastIndexOf);
            } else if (type === 'folder') {
                var lastIndex = fullName.lastIndexOf('/');
                newName = fullName.substring(lastIndex + 1);
            }

            renameModal.querySelector('[name="type"]').value = type;
            renameModal.querySelector('[name="currentName"]').value = fullName;
            renameModal.querySelector('[name="newName"]').value = newName;
        });
    });
}
document.addEventListener('DOMContentLoaded', function () {
    renameObject();
});


