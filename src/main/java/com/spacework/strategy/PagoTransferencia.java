package com.spacework.strategy;

import com.spacework.model.Pago;
import java.util.Date;

public class PagoTransferencia implements EstrategiaPago {

    @Override
    public Pago procesarPago(Pago pago) throws Exception {
        if (pago.getMontoFinal() <= 0) {
            throw new Exception("Monto de pago inválido para transferencia");
        }

        pago.setEstadoPago("COMPLETADO");
        pago.setMetodoPago("TRANSFERENCIA");
        pago.setFechaPago(new Date());
        pago.setReferencia("TF-" + System.currentTimeMillis());

        return pago;
    }

    @Override
    public String getNombreMetodo() {
        return "TRANSFERENCIA";
    }
}
