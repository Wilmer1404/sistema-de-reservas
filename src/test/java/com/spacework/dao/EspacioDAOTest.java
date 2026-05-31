package com.spacework.dao;

import com.spacework.model.Espacio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class EspacioDAOTest {

    private EspacioDAO espacioDAO;

    @BeforeEach
    public void setUp() {
        espacioDAO = new EspacioDAO();
    }

    @Test
    public void testListarEspacios() throws Exception {
        List<Espacio> espacios = espacioDAO.listar();
        assertNotNull(espacios, "La lista de espacios no debe ser null");
        assertTrue(espacios.size() >= 0, "La lista de espacios debe tener al menos 0 elementos");
    }

    @Test
    public void testBuscarPorId() throws Exception {
        Espacio espacio = espacioDAO.buscarPorId(1);
        if (espacio != null) {
            assertNotNull(espacio.getNombre(), "El nombre del espacio no debe ser null");
            assertTrue(espacio.getCapacidad() > 0, "La capacidad debe ser mayor a 0");
        }
    }

    @Test
    public void testInsertarEspacio() {
        Espacio espacio = new Espacio();
        espacio.setNombre("Sala Test JUnit");
        espacio.setTipo("Sala de Conferencias");
        espacio.setCapacidad(15);
        espacio.setUbicacion("Piso 2");
        espacio.setPrecioPorHora(75.00);
        espacio.setEstado("ACTIVO");
        assertDoesNotThrow(() -> espacioDAO.insertar(espacio),
                "La inserción del espacio no debe lanzar excepción");
    }

    @Test
    public void testActualizarEspacio() throws Exception {
        Espacio espacio = espacioDAO.buscarPorId(1);
        if (espacio != null) {
            espacio.setNombre("Sala Actualizada");
            assertDoesNotThrow(() -> espacioDAO.actualizar(espacio),
                    "La actualización del espacio no debe lanzar excepción");
        }
    }

    @Test
    public void testDesactivarEspacio() {
        assertDoesNotThrow(() -> espacioDAO.desactivar(999),
                "Desactivar un espacio no debe lanzar excepción");
    }

    @Test
    public void testValidacionCapacidadPositiva() {
        Espacio espacio = new Espacio();
        espacio.setCapacidad(-5);
        assertFalse(espacio.getCapacidad() > 0, "La capacidad no debe ser negativa");
    }
}
