package com.spacework.model;

public enum EstadoEspacio {
    ACTIVO("ACTIVO"),
    INACTIVO("INACTIVO"),
    MANTENIMIENTO("MANTENIMIENTO");

    private final String valor;

    EstadoEspacio(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
