package com.spacework.model;

public enum EstadoUsuario {
    ACTIVO("ACTIVO"),
    INACTIVO("INACTIVO"),
    BLOQUEADO("BLOQUEADO");

    private final String valor;

    EstadoUsuario(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
