package com.galaxy.galaxy_drive.exception.minio;

public class MinioObjectSizeException extends RuntimeException {
    public MinioObjectSizeException(String message) {
        super(message);
    }
}
