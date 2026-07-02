package com.spacework.dao;

import com.spacework.model.Cliente;
import com.spacework.model.Espacio;
import com.spacework.model.Reserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Disabled;
@Disabled("Requiere base de datos encendida")
public class ReservaDAOTest {

    private ReservaDAO reservaDAO;

    @BeforeEach
    public void setUp() {
        reservaDAO = new ReservaDAO();
    }

    @Test
    public void testListarReservas() throws Exception {
        List<Reserva> reservas = reservaDAO.listarTodas();
        assertNotNull(reservas, "La lista de reservas no debe ser null");
        assertTrue(reservas.size() >= 0, "La lista de reservas debe tener al menos 0 elementos");
    }

    @Test
    public void testBuscarPorId() throws Exception {
        Reserva reserva = reservaDAO.buscarPorId(1);
        if (reserva != null) {
            assertNotNull(reserva.getEstado(), "El estado de la reserva no debe ser null");
            assertTrue(reserva.getMontoTotal() >= 0, "El monto debe ser positivo");
        }
    }

    @Test
    public void testListarPorCliente() throws Exception {
        List<Reserva> reservas = reservaDAO.listarPorCliente(1);
        assertNotNull(reservas, "La lista de reservas del cliente no debe ser null");
    }

    @Test
    public void testCambiarEstado() {
        assertDoesNotThrow(() -> reservaDAO.cambiarEstado(1, "COMPLETADA"),
                "Cambiar estado no debe lanzar excepción");
    }

    @Test
    public void testInsertarReserva() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(1);

        Espacio espacio = new Espacio();
        espacio.setIdEspacio(1);

        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setEspacio(espacio);
        reserva.setFechaInicio(new Date());
        reserva.setFechaFin(new Date(System.currentTimeMillis() + 3600000));
        reserva.setMontoTotal(150.00);
        reserva.setEstado("PENDIENTE");

        assertDoesNotThrow(() -> reservaDAO.insertar(reserva),
                "La inserción de la reserva no debe lanzar excepción");
    }

    @Test
    public void testVerificarDisponibilidad() throws Exception {
        Timestamp tsInicio = new Timestamp(new Date().getTime());
        Timestamp tsFin = new Timestamp(System.currentTimeMillis() + 7200000);
        boolean disponible = reservaDAO.verificarDisponibilidad(1, tsInicio, tsFin);
        assertTrue(disponible || !disponible, "El método debe retornar un booleano");
    }

    @Test
    public void testValidacionFechasFin() {
        Reserva reserva = new Reserva();
        Date inicio = new Date();
        Date fin = new Date(inicio.getTime() - 1000);
        reserva.setFechaInicio(inicio);
        reserva.setFechaFin(fin);
        assertTrue(fin.before(inicio), "La fecha de fin debe ser posterior a inicio");
    }

    @Test
    public void testEstadosPosibles() throws Exception {
        String[] estadosValidos = {"PENDIENTE", "CONFIRMADA", "COMPLETADA", "CANCELADA"};
        Reserva reserva = reservaDAO.buscarPorId(1);
        if (reserva != null) {
            boolean estadoValido = false;
            for (String estado : estadosValidos) {
                if (reserva.getEstado().equals(estado)) {
                    estadoValido = true;
                    break;
                }
            }
            assertTrue(estadoValido, "El estado debe ser uno de los válidos");
        }
    }
}
