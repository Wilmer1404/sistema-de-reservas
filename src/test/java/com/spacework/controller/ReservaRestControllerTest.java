package com.spacework.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spacework.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ReservaRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReservaService reservaService;

    @InjectMocks
    private ReservaRestController reservaRestController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(reservaRestController).build();
    }

    @Test
    public void testCrearReservaExito() throws Exception {
        Map<String, Object> respuestaMock = new HashMap<>();
        respuestaMock.put("idReserva", 100);
        
        when(reservaService.crear(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(respuestaMock);

        Map<String, Object> request = new HashMap<>();
        request.put("idCliente", 1);
        request.put("idEspacio", 2);
        request.put("fechaInicio", "2026-07-02T10:00:00");
        request.put("fechaFin", "2026-07-02T12:00:00");

        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void testCrearReservaFallaPorConflicto() throws Exception {
        when(reservaService.crear(anyInt(), anyInt(), anyString(), anyString()))
                .thenThrow(new IllegalStateException("El espacio ya tiene una reserva"));

        Map<String, Object> request = new HashMap<>();
        request.put("idCliente", 1);
        request.put("idEspacio", 2);
        request.put("fechaInicio", "2026-07-02T10:00:00");
        request.put("fechaFin", "2026-07-02T12:00:00");

        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()); // HTTP 409
    }
}
