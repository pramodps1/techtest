package com.db.dataplatform.techtest.client.exception;

public class ServiceUnavailableException extends RuntimeException{
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
