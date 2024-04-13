package com.galaxy.galaxy_drive.exception.minio;

public class MemoryLlimitException extends RuntimeException{
    public MemoryLlimitException(String message) {
        super(message);
    }
}
