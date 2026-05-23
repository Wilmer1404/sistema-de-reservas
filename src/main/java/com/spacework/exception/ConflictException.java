package com.spacework.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String mensaje) {
        super(mensaje);
    }

    public ConflictException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
