var fileUpload = document.getElementById('upload-file')
fileUpload.addEventListener('change', function () {
    if (this.value) {
        document.getElementById("upload-form-file").submit();
    }
});
