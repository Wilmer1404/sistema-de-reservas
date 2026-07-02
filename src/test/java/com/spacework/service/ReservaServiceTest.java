package com.spacework.service;

import com.spacework.dao.*;
import com.spacework.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservaServiceTest {

    @Mock
    private ReservaDAO reservaDAO;
    @Mock
    private EspacioDAO espacioDAO;
    @Mock
    private ClienteDAO clienteDAO;
    @Mock
    private HorarioBloqueadoDAO horarioBloqueadoDAO;
    @Mock
    private PagoDAO pagoDAO;

    @InjectMocks
    private ReservaService reservaService;

    private Cliente clienteMock;
    private Espacio espacioMock;

    @BeforeEach
    public void setUp() {
        clienteMock = new Cliente();
        clienteMock.setIdCliente(1);
        clienteMock.setNombre("Test Cliente");

        espacioMock = new Espacio();
        espacioMock.setIdEspacio(1);
        espacioMock.setPrecioPorHora(50.0);
    }

    @Test
    public void testCrearReservaFallaPorEspacioNoEncontrado() throws Exception {
        when(espacioDAO.buscarPorId(99)).thenReturn(null);
        when(clienteDAO.listar()).thenReturn(Arrays.asList(clienteMock));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.crear(1, 99, "2026-07-02T10:00:00", "2026-07-02T12:00:00");
        });

        assertEquals("Cliente o espacio no encontrado", exception.getMessage());
    }

    @Test
    public void testCrearReservaFallaPorHorarioBloqueado() throws Exception {
        when(espacioDAO.buscarPorId(1)).thenReturn(espacioMock);
        when(clienteDAO.listar()).thenReturn(Arrays.asList(clienteMock));
        when(horarioBloqueadoDAO.verificarDisponibilidadHorarios(anyInt(), any(Timestamp.class), any(Timestamp.class)))
            .thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reservaService.crear(1, 1, "2026-07-02T10:00:00", "2026-07-02T12:00:00");
        });

        assertTrue(exception.getMessage().contains("horario bloqueado"));
    }

    @Test
    public void testCrearReservaFallaPorReservaCruzada() throws Exception {
        when(espacioDAO.buscarPorId(1)).thenReturn(espacioMock);
        when(clienteDAO.listar()).thenReturn(Arrays.asList(clienteMock));
        when(horarioBloqueadoDAO.verificarDisponibilidadHorarios(anyInt(), any(Timestamp.class), any(Timestamp.class)))
            .thenReturn(true);
        when(reservaDAO.verificarDisponibilidad(anyInt(), any(Timestamp.class), any(Timestamp.class)))
            .thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reservaService.crear(1, 1, "2026-07-02T10:00:00", "2026-07-02T12:00:00");
        });

        assertTrue(exception.getMessage().contains("ya tiene una reserva"));
    }
}
