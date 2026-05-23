package com.spacework.model;

public enum MetodoPago {
    EFECTIVO("EFECTIVO"),
    TARJETA("TARJETA"),
    TRANSFERENCIA("TRANSFERENCIA");

    private final String valor;

    MetodoPago(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static MetodoPago fromValor(String valor) {
        for (MetodoPago m : MetodoPago.values()) {
            if (m.valor.equals(valor)) return m;
        }
        throw new IllegalArgumentException("Método de pago inválido: " + valor);
    }
}
