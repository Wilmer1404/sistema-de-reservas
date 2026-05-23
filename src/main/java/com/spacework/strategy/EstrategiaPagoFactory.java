package com.spacework.strategy;

import com.spacework.exception.BusinessException;

public class EstrategiaPagoFactory {
    public static EstrategiaPago crear(String metodo) {
        switch (metodo.toUpperCase()) {
            case "EFECTIVO":
                return new PagoEfectivo();
            case "TARJETA":
                return new PagoTarjeta();
            case "TRANSFERENCIA":
                return new PagoTransferencia();
            default:
                throw new BusinessException("Método de pago no soportado: " + metodo);
        }
    }
}
