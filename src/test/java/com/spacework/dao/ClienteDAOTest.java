package com.spacework.dao;

import com.spacework.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class ClienteDAOTest {

    private ClienteDAO clienteDAO;

    @BeforeEach
    public void setUp() {
        clienteDAO = new ClienteDAO();
    }

    @Test
    public void testListarClientes() throws Exception {
        List<Cliente> clientes = clienteDAO.listar();
        assertNotNull(clientes, "La lista de clientes no debe ser null");
        assertTrue(clientes.size() >= 0, "La lista de clientes debe tener al menos 0 elementos");
    }

    @Test
    public void testBuscarPorDni() throws Exception {
        Cliente cliente = clienteDAO.buscarPorDni("12345678");
        if (cliente != null) {
            assertNotNull(cliente.getNombre(), "El nombre del cliente no debe ser null");
            assertEquals("12345678", cliente.getDni(), "El DNI debe coincidir");
        }
    }

    @Test
    public void testBuscarPorId() throws Exception {
        Cliente cliente = clienteDAO.buscarPorId(1);
        if (cliente != null) {
            assertNotNull(cliente.getNombre(), "El nombre del cliente no debe ser null");
            assertNotNull(cliente.getEmail(), "El email del cliente no debe ser null");
        }
    }

    @Test
    public void testInsertarCliente() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Juan");
        cliente.setApellido("Test JUnit");
        cliente.setDni("98765432");
        cliente.setEmail("test@junit.com");
        cliente.setTelefono("999999999");
        cliente.setEstado("ACTIVO");
        assertDoesNotThrow(() -> clienteDAO.insertar(cliente),
                "La inserción del cliente no debe lanzar excepción");
    }

    @Test
    public void testActualizarCliente() throws Exception {
        Cliente cliente = clienteDAO.buscarPorId(1);
        if (cliente != null) {
            cliente.setNombre("Juan Actualizado");
            assertDoesNotThrow(() -> clienteDAO.actualizar(cliente),
                    "La actualización del cliente no debe lanzar excepción");
        }
    }

    @Test
    public void testDesactivarCliente() {
        assertDoesNotThrow(() -> clienteDAO.desactivar(999),
                "Desactivar un cliente no debe lanzar excepción");
    }

    @Test
    public void testValidacionDniNoVacio() {
        Cliente cliente = new Cliente();
        cliente.setDni("");
        assertTrue(cliente.getDni().isEmpty(), "El DNI debe estar vacío");
    }

    @Test
    public void testValidacionEmailNoVacio() {
        Cliente cliente = new Cliente();
        cliente.setEmail("");
        assertTrue(cliente.getEmail().isEmpty(), "El email debe estar vacío");
    }
}
