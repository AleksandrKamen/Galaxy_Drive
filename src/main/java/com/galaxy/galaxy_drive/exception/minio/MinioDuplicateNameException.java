package com.galaxy.galaxy_drive.exception.minio;

public class MinioDuplicateNameException extends RuntimeException {
    public MinioDuplicateNameException(String message) {
        super(message);
    }
}
