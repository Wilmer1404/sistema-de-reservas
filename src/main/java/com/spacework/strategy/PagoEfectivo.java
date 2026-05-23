package com.spacework.strategy;

import com.spacework.model.Pago;
import java.util.Date;

public class PagoEfectivo implements EstrategiaPago {

    @Override
    public Pago procesarPago(Pago pago) throws Exception {
        // Validar monto
        if (pago.getMontoFinal() <= 0) {
            throw new Exception("Monto de pago inválido para pago en efectivo");
        }

        pago.setEstadoPago("COMPLETADO");
        pago.setMetodoPago("EFECTIVO");
        pago.setFechaPago(new Date());
        pago.setReferencia("EF-" + System.currentTimeMillis());

        return pago;
    }

    @Override
    public String getNombreMetodo() {
        return "EFECTIVO";
    }
}
