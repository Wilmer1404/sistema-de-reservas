package com.spacework.service;

import com.spacework.dao.ClienteDAO;
import com.spacework.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

    @Mock
    private ClienteDAO clienteDAO;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteMock;

    @BeforeEach
    public void setUp() {
        clienteMock = new Cliente();
        clienteMock.setIdCliente(1);
        clienteMock.setNombre("Test");
        clienteMock.setApellido("User");
        clienteMock.setDni("12345678");
        clienteMock.setEmail("test@correo.com");
    }

    @Test
    public void testListarClientes() throws Exception {
        when(clienteDAO.listar()).thenReturn(Arrays.asList(clienteMock));
        
        List<Cliente> resultado = clienteService.listar();
        
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Test", resultado.get(0).getNombre());
        verify(clienteDAO, times(1)).listar();
    }

    @Test
    public void testRegistrarClienteExito() throws Exception {
        doNothing().when(clienteDAO).insertar(any(Cliente.class));
        
        assertDoesNotThrow(() -> clienteService.registrar(clienteMock));
        
        verify(clienteDAO, times(1)).insertar(any(Cliente.class));
    }

    @Test
    public void testRegistrarClienteDniDuplicado() throws Exception {
        doThrow(new SQLException("Error UQ_DNI")).when(clienteDAO).insertar(any(Cliente.class));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.registrar(clienteMock);
        });
        
        assertTrue(exception.getMessage().contains("DNI ya está registrado"));
    }

    @Test
    public void testRegistrarClienteEmailDuplicado() throws Exception {
        doThrow(new SQLException("Error UQ_EMAIL_CLIENTE")).when(clienteDAO).insertar(any(Cliente.class));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.registrar(clienteMock);
        });
        
        assertTrue(exception.getMessage().contains("email ya está registrado"));
    }

    @Test
    public void testActualizarClienteNoEncontrado() throws Exception {
        when(clienteDAO.listar()).thenReturn(Arrays.asList());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.actualizar(99, clienteMock);
        });
        
        assertEquals("Cliente no encontrado", exception.getMessage());
    }
}
