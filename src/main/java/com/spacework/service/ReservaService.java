package com.spacework.service;

import com.spacework.dao.*;
import com.spacework.model.*;
import com.spacework.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservaService {
    @Autowired
    private ReservaDAO reservaDAO;

    @Autowired
    private EspacioDAO espacioDAO;

    @Autowired
    private ClienteDAO clienteDAO;

    @Autowired
    private PagoDAO pagoDAO;

    @Autowired
    private HorarioBloqueadoDAO horarioBloqueadoDAO;

    public Reserva crearReserva(Reserva reserva) {
        // Validar cliente existe
        if (reserva.getCliente() == null || reserva.getCliente().getIdCliente() == 0) {
            throw new BusinessException("Cliente inválido");
        }

        // Validar espacio existe
        Espacio espacio = reserva.getEspacio();
        if (espacio == null || espacio.getIdEspacio() == 0) {
            throw new BusinessException("Espacio inválido");
        }

        if (!"ACTIVO".equals(espacio.getEstado())) {
            throw new ConflictException("El espacio no está disponible");
        }

        // Validar fechas
        if (reserva.getFechaFin().before(reserva.getFechaInicio())) {
            throw new BusinessException("La fecha de fin debe ser posterior a la de inicio");
        }

        reserva.setEstado("PENDIENTE");
        // TODO: Guardar reserva en BD
        // reservaDAO.crear(reserva);

        // Crear pago asociado
        // TODO: Crear pago en BD
        Pago pago = new Pago();
        pago.setIdReserva(reserva.getIdReserva());
        pago.setMonto(reserva.getMontoTotal());
        pago.setEstadoPago("PENDIENTE");

        return reserva;
    }

    public void cancelarReserva(Long idReserva) {
        // TODO: Cancelar reserva en BD
        Reserva reserva = new Reserva();
        reserva.setIdReserva(idReserva.intValue());
        reserva.setEstado("CANCELADA");
    }

    public void completarReserva(Long idReserva) {
        // TODO: Completar reserva en BD
        Reserva reserva = new Reserva();
        reserva.setIdReserva(idReserva.intValue());
        reserva.setEstado("COMPLETADA");
    }
}
