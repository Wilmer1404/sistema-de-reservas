package com.spacework.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
@Disabled("Requiere base de datos encendida")
public class ReporteServiceTest {

    private ReporteService reporteService;

    @BeforeEach
    public void setUp() {
        reporteService = new ReporteService();
    }

    @Test
    public void testIngresosMensuales() throws Exception {
        Map<String, Object> resultado = reporteService.ingresosMensuales(2026);
        assertNotNull(resultado, "El resultado no debe ser null");
        assertTrue(resultado.containsKey("datos"), "Debe contener la clave 'datos'");
        assertTrue(resultado.containsKey("total"), "Debe contener la clave 'total'");
        assertNotNull(resultado.get("datos"), "Los datos no deben ser null");
    }

    @Test
    public void testReservasPorEstado() throws Exception {
        Map<String, Object> resultado = reporteService.reservasPorEstado();
        assertNotNull(resultado, "El resultado no debe ser null");
        assertTrue(resultado.containsKey("datos"), "Debe contener la clave 'datos'");
        assertTrue(resultado.containsKey("totalReservas"), "Debe contener la clave 'totalReservas'");
    }

    @Test
    public void testOcupacionPorEspacio() throws Exception {
        Map<String, Object> resultado = reporteService.ocupacionPorEspacio();
        assertNotNull(resultado, "El resultado no debe ser null");
        assertTrue(resultado.containsKey("datos"), "Debe contener la clave 'datos'");
        assertTrue(resultado.containsKey("totalEspacios"), "Debe contener la clave 'totalEspacios'");
    }

    @Test
    public void testIngresosMensualesAñoDiferente() throws Exception {
        Map<String, Object> resultado = reporteService.ingresosMensuales(2025);
        assertNotNull(resultado.get("datos"), "Los datos del 2025 no deben ser null");
    }
}
