package com.spacework.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spacework.model.Cliente;
import com.spacework.service.AuthService;
import com.spacework.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ClienteRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClienteService clienteService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ClienteRestController clienteRestController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(clienteRestController).build();
    }

    @Test
    public void testRegistrarClienteExito() throws Exception {
        doNothing().when(clienteService).registrar(any(Cliente.class));

        Cliente c = new Cliente();
        c.setNombre("Luis");
        c.setApellido("Perez");
        c.setDni("87654321");
        c.setEmail("luis@perez.com");

        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isOk());
    }

    @Test
    public void testRegistrarClienteFallaConflicto() throws Exception {
        doThrow(new IllegalArgumentException("Este DNI ya está registrado"))
                .when(clienteService).registrar(any(Cliente.class));

        Cliente c = new Cliente();
        c.setNombre("Luis");
        c.setDni("87654321");

        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isConflict()); // HTTP 409
    }
}
