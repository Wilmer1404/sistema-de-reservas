package com.spacework.service;

import com.spacework.dao.*;
import com.spacework.model.*;
import com.spacework.exception.*;
import com.spacework.strategy.EstrategiaPago;
import com.spacework.strategy.EstrategiaPagoFactory;
import com.spacework.util.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class PagoService {

    @Autowired
    private PagoDAO pagoDAO;

    @Autowired
    private ReservaDAO reservaDAO;

    @Autowired
    private DescuentoDAO descuentoDAO;

    @Autowired
    private TokenEvaluacionDAO tokenEvaluacionDAO;

    @Autowired
    private ClienteDAO clienteDAO;

    @Autowired
    private EmailService emailService;

    public Pago procesarPago(Long idPago, String metodoPago, String codigoDescuento) {
        Pago pago = new Pago();
        pago.setIdPago(idPago.intValue());
        
        if (codigoDescuento != null && !codigoDescuento.isEmpty()) {
            aplicarDescuento(pago, codigoDescuento);
        }

        // Calcular IGV sobre monto descontado
        double montoConDescuento = pago.getMonto() - pago.getDescuentoAplicado();
        double igv = montoConDescuento * 0.18;
        pago.setIgv(igv);
        pago.setMontoFinal(montoConDescuento + igv);

        // Procesar pago mediante estrategia
        EstrategiaPago estrategia = EstrategiaPagoFactory.crear(metodoPago);
        try {
            pago = estrategia.procesarPago(pago);
        } catch (Exception e) {
            throw new BusinessException("Error procesando pago: " + e.getMessage());
        }

        return pago;
    }

    private void aplicarDescuento(Pago pago, String codigoDescuento) {
        // TODO: Validar y aplicar descuento desde BD
        // Descuento descuento = descuentoDAO.obtenerPorCodigo(codigoDescuento);
        // if (descuento.esValido(...)) { ... }
    }

    private void generarTokenEvaluacion(Pago pago, Reserva reserva) {
        // TODO: Generar token de evaluación y guardarlo en BD
        String token = TokenGenerator.generarToken();
        // Token válido 7 días después de la fecha fin de la reserva
        // Calendar cal = Calendar.getInstance();
        // cal.setTime(reserva.getFechaFin());
        // cal.add(Calendar.DAY_OF_MONTH, 7);
        // Date fechaExpiracion = cal.getTime();
        // TokenEvaluacion tokenEval = new TokenEvaluacion(...);
        // tokenEvaluacionDAO.actualizar(tokenEval);
    }
}
