package com.spacework.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }

    public ResourceNotFoundException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
