package com.spacework.model;

public enum EstadoDescuento {
    ACTIVO("ACTIVO"),
    AGOTADO("AGOTADO"),
    EXPIRADO("EXPIRADO"),
    INACTIVO("INACTIVO");

    private final String valor;

    EstadoDescuento(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
