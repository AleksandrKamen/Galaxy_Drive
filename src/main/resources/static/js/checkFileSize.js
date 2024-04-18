function checkFileSize(input) {
    const maxTotalSize = 100 * 1024 * 1024;
    if (input.files.length > 0) {
        let totalSize = 0;
        for (let i = 0; i < input.files.length; i++) {
            totalSize += input.files[i].size;
        }
        if (totalSize > maxTotalSize) {
            alert(document.getElementById('messageMaxSize').textContent);
            return false;
        }
    }
    input.parentNode.submit();
}
