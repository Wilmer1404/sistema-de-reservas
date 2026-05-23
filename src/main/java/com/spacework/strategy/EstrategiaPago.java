package com.spacework.strategy;

import com.spacework.model.Pago;
import java.math.BigDecimal;

public interface EstrategiaPago {
    Pago procesarPago(Pago pago) throws Exception;
    String getNombreMetodo();
}
