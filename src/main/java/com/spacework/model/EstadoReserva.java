package com.spacework.model;

public enum EstadoReserva {
    PENDIENTE("PENDIENTE"),
    CONFIRMADA("CONFIRMADA"),
    COMPLETADA("COMPLETADA"),
    CANCELADA("CANCELADA");

    private final String valor;

    EstadoReserva(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static EstadoReserva fromValor(String valor) {
        for (EstadoReserva e : EstadoReserva.values()) {
            if (e.valor.equals(valor)) return e;
        }
        throw new IllegalArgumentException("Estado inválido: " + valor);
    }
}
