package com.spacework.strategy;

import com.spacework.model.Pago;
import java.util.Date;

public class PagoTarjeta implements EstrategiaPago {

    @Override
    public Pago procesarPago(Pago pago) throws Exception {
        if (pago.getMontoFinal() <= 0) {
            throw new Exception("Monto de pago inválido para pago con tarjeta");
        }

        // Simulación: 95% de probabilidad de éxito
        if (Math.random() < 0.05) {
            throw new Exception("Tarjeta rechazada por la institución financiera");
        }

        pago.setEstadoPago("COMPLETADO");
        pago.setMetodoPago("TARJETA");
        pago.setFechaPago(new Date());
        pago.setReferencia("TC-" + System.currentTimeMillis());

        return pago;
    }

    @Override
    public String getNombreMetodo() {
        return "TARJETA";
    }
}
