function checkFileSize(input) {
    const messageMaxSize = document.getElementById('messageMaxSize').textContent;

    if (input.files.length > 0) {
        for (var i = 0; i < input.files.length; i++) {
            if (input.files[i].size > 104857600) {
                alert(messageMaxSize);
                return false;
            }
        }
    }
    document.getElementById('upload-form-file').submit();
}