package com.galaxy.galaxy_drive.exception.minio;

public class FolderNotFoundException extends RuntimeException {
    public FolderNotFoundException(String message) {
        super(message);
    }
}
