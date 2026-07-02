package com.spacework.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spacework.service.AuthService;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
public class AuthRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthRestController authRestController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authRestController).build();
    }

    @Test
    public void testLoginExitoso() throws Exception {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("token", "dummy-token");
        when(authService.loginAdmin(anyString(), anyString())).thenReturn(mockResponse);

        Map<String, String> request = new HashMap<>();
        request.put("username", "admin");
        request.put("password", "12345");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("dummy-token"));
    }

    @Test
    public void testLoginFalloCredenciales() throws Exception {
        when(authService.loginAdmin(anyString(), anyString()))
                .thenThrow(new SecurityException("Credenciales incorrectas"));

        Map<String, String> request = new HashMap<>();
        request.put("username", "admin");
        request.put("password", "mal");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales incorrectas"));
    }
}
