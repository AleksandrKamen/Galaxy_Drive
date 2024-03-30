function copyObject(){
    var copyBtns = document.querySelectorAll('.copy-btn');
    var copyModal = document.getElementById('copyModal');


    copyBtns.forEach(function (btn) {
        btn.addEventListener('click', function () {
            var fullName = btn.getAttribute('data-object-name');
            var type = btn.getAttribute('data-object-type');
            var copyName = '';

            if (type === 'file') {
                var indexOf = fullName.indexOf('.');
                var lastIndexOf = fullName.lastIndexOf("/") + 1;
                copyName = fullName.substring(indexOf, lastIndexOf);
            } else if (type === 'folder') {
                var lastIndex = fullName.lastIndexOf('/');
                copyName = fullName.substring(lastIndex + 1);
            }

            copyModal.querySelector('[name="type"]').value = type;
            copyModal.querySelector('[name="currentName"]').value = fullName;
            copyModal.querySelector('[name="copyName"]').value = copyName;
        });
    });
}
document.addEventListener('DOMContentLoaded', function () {
    copyObject();
});


