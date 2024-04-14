package com.galaxy.galaxy_drive.exception.minio;

public class IncorrectNameException extends RuntimeException{
    public IncorrectNameException(String message) {
        super(message);
    }
}
