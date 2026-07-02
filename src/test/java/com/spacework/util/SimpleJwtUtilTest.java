package com.spacework.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleJwtUtilTest {

    @Test
    public void testGenerarYValidarToken() {
        String token = SimpleJwtUtil.generarToken("admin", "Admin User", "admin@test.com", "ADMIN");
        assertNotNull(token, "El token no debe ser nulo");
        assertTrue(token.length() > 0, "El token debe tener contenido");

        // Validar el token generado
        assertTrue(SimpleJwtUtil.validarToken(token), "El token debería ser válido");
    }

    @Test
    public void testValidarTokenInvalido() {
        String tokenInvalido = "esto.no.es.un.token.jwt";
        assertFalse(SimpleJwtUtil.validarToken(tokenInvalido), "Un token con formato inválido debe ser rechazado");
        assertFalse(SimpleJwtUtil.validarToken(null), "Un token nulo debe ser rechazado");
        assertFalse(SimpleJwtUtil.validarToken(""), "Un token vacío debe ser rechazado");
    }

    @Test
    public void testExtraerUsuarioYRol() {
        String token = SimpleJwtUtil.generarToken("testuser", "Test Name", "test@test.com", "RECEPCIONISTA");
        assertNotNull(token);

        String username = SimpleJwtUtil.obtenerUsuario(token);
        assertEquals("testuser", username, "El usuario extraído debe coincidir");

        String rol = SimpleJwtUtil.obtenerRol(token);
        assertEquals("RECEPCIONISTA", rol, "El rol extraído debe coincidir");
    }
}
