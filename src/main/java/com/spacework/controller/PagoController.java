package com.spacework.controller;

import com.spacework.model.Pago;
import com.spacework.model.TokenEvaluacion;
import com.spacework.model.Evaluacion;
import com.spacework.model.Reserva;
import com.spacework.model.Cliente;
import com.spacework.model.Espacio;
import com.spacework.dao.PagoDAO;
import com.spacework.dao.TokenEvaluacionDAO;
import com.spacework.dao.EvaluacionDAO;
import com.spacework.dao.ReservaDAO;
import com.spacework.dao.ClienteDAO;
import com.spacework.dao.EspacioDAO;
import com.spacework.util.MailService;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Controlador para gestión de Pagos
 * Maneja la lógica de negocio relacionada con pagos
 */
public class PagoController {
    
    private PagoDAO pagoDAO;
    private TokenEvaluacionDAO tokenEvaluacionDAO;
    private EvaluacionDAO evaluacionDAO;
    private ReservaDAO reservaDAO;
    private ClienteDAO clienteDAO;
    private EspacioDAO espacioDAO;

    public PagoController() {
        this.pagoDAO = new PagoDAO();
        this.tokenEvaluacionDAO = new TokenEvaluacionDAO();
        this.evaluacionDAO = new EvaluacionDAO();
        this.reservaDAO = new ReservaDAO();
        this.clienteDAO = new ClienteDAO();
        this.espacioDAO = new EspacioDAO();
    }

    /**
     * Registra un nuevo pago
     */
    public boolean registrarPago(int idReserva, double monto, String metodoPago) {
        if (monto <= 0) {
            System.out.println("Error: El monto debe ser mayor a 0");
            return false;
        }
        
        if (!metodoPago.equals("TARJETA") && !metodoPago.equals("TRANSFERENCIA") && !metodoPago.equals("EFECTIVO")) {
            System.out.println("Error: Método de pago inválido");
            return false;
        }
        
        Pago pago = new Pago();
        pago.setIdReserva(idReserva);
        pago.setMonto(monto);
        pago.setMetodoPago(metodoPago);
        pago.setEstadoPago("PENDIENTE");
        pago.setFechaCreacion(new Date());
        
        return pagoDAO.insertar(pago);
    }

    /**
     * Obtiene todos los pagos
     */
    public List<Pago> obtenerTodos() {
        return pagoDAO.listar();
    }

    /**
     * Obtiene los pagos de una reserva específica
     */
    public List<Pago> obtenerPorReserva(int idReserva) {
        return pagoDAO.listarPorReserva(idReserva);
    }

    /**
     * Busca un pago por ID
     */
    public Pago obtenerPorId(int idPago) {
        return pagoDAO.buscarPorId(idPago);
    }

    /**
     * Marca un pago como completado y genera token de evaluación
     * Automáticamente crea una evaluación pendiente para el cliente
     * Envía correo al cliente con enlace para evaluar
     */
    public boolean completarPago(int idPago) {
        try {
            Pago pago = pagoDAO.buscarPorId(idPago);
            if (pago == null) {
                System.out.println("Error: Pago no encontrado");
                return false;
            }

            // Obtener la reserva asociada al pago
            Reserva reserva = reservaDAO.buscarPorId(pago.getIdReserva());
            if (reserva == null) {
                System.out.println("Error: Reserva no encontrada");
                return false;
            }

            // Marcar pago como completado
            pago.setEstadoPago("COMPLETADO");
            pago.setFechaPago(new Date());
            
            if (!pagoDAO.actualizar(pago)) {
                System.out.println("Error: No se pudo actualizar el pago");
                return false;
            }

            // ===== CREAR EVALUACION AUTOMATICAMENTE =====
            Evaluacion evaluacion = new Evaluacion();
            evaluacion.setIdReserva(pago.getIdReserva());
            evaluacion.setIdCliente(reserva.getCliente().getIdCliente());
            evaluacion.setCalificacion(0);  // Pendiente de calificar
            evaluacion.setComentario(null); // Pendiente de comentario
            
            if (!evaluacionDAO.insertar(evaluacion)) {
                System.err.println("Advertencia: No se pudo crear evaluación automática para pago #" + idPago);
                // Continuamos de todas formas (no es crítico)
            } else {
                System.out.println("[PagoController] Evaluación automática creada para reserva #" + pago.getIdReserva());
            }

            // Generar token de evaluación (válido por 30 días)
            String token = UUID.randomUUID().toString();
            java.util.Date ahora = new java.util.Date();
            java.util.Date expiracion = new java.util.Date(ahora.getTime() + (30L * 24 * 60 * 60 * 1000)); // 30 días

            TokenEvaluacion tokenEval = new TokenEvaluacion();
            tokenEval.setIdPago(idPago);
            tokenEval.setToken(token);
            // Email genérico para testing (en producción, obtendría del cliente)
            tokenEval.setEmailCliente("cliente@example.com");
            tokenEval.setFechaCreacion(ahora);
            tokenEval.setFechaExpiracion(expiracion);
            tokenEval.setUtilizado(0);

            if (!tokenEvaluacionDAO.crearToken(tokenEval)) {
                System.out.println("Error: No se pudo crear token de evaluación");
                return false;
            }

            // Log del token generado (en testing sin mail)
            System.out.println("[PagoController] Pago #" + idPago + " completado");
            System.out.println("[PagoController] Token de evaluación generado: " + token);
            System.out.println("[PagoController] Válido hasta: " + 
                    new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(expiracion));

            // Enviar correo (usará console si JavaMail no está disponible)
            boolean emailEnviado = MailService.enviarTokenEvaluacion(
                    "cliente@example.com",
                    "Cliente",
                    "Espacio",
                    new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()),
                    token
            );

            System.out.println("[PagoController] Notificación enviada: " + 
                    (emailEnviado ? "Exitosa" : "Fallida"));
            
            return true;

        } catch (Exception e) {
            System.err.println("[PagoController] Error al completar pago: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Rechaza un pago
     */
    public boolean rechazarPago(int idPago) {
        return pagoDAO.cambiarEstado(idPago, "RECHAZADO");
    }

    /**
     * Reembolsa un pago
     */
    public boolean reembolsarPago(int idPago) {
        return pagoDAO.cambiarEstado(idPago, "REEMBOLSADO");
    }

    /**
     * Obtiene el resumen de ingresos en un período
     */
    public double obtenerIngresosPeriodo(Date fechaInicio, Date fechaFin) {
        // Implementar agregación de montos completados
        // Por ahora retorna placeholder
        return 0.0;
    }
}
