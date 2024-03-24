var fileUpload = document.getElementById('upload-folder')
fileUpload.addEventListener('change', function () {
    if (this.value) {
        document.getElementById("upload-form-folder").submit();
    }
});