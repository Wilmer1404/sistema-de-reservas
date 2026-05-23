package com.spacework.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String mensaje) {
        super(mensaje);
    }

    public UnauthorizedException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
