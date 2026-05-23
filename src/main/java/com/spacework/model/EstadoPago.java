package com.spacework.model;

public enum EstadoPago {
    PENDIENTE("PENDIENTE"),
    COMPLETADO("COMPLETADO"),
    RECHAZADO("RECHAZADO");

    private final String valor;

    EstadoPago(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static EstadoPago fromValor(String valor) {
        for (EstadoPago e : EstadoPago.values()) {
            if (e.valor.equals(valor)) return e;
        }
        throw new IllegalArgumentException("Estado de pago inválido: " + valor);
    }
}
