    var types = document.getElementsByName('type');
    var files =  document.getElementsByClassName('files')
    var folders =  document.getElementsByClassName('folders')
    Array.from(folders).forEach(folder => {
        folder.style.display = 'none';
    });

    Array.from(types).forEach(button => {
    button.addEventListener('change', function () {
        if (this.value === 'files') {
            Array.from(files).forEach(file => {
                file.style.display = 'table-row';
            });

            Array.from(folders).forEach(folder => {
                folder.style.display = 'none';
            });
        } else if (this.value === 'folders') {
            Array.from(files).forEach(file => {
                file.style.display = 'none';
            });

            Array.from(folders).forEach(folder => {
                folder.style.display = 'table-row';
            });
        }
    });
})

