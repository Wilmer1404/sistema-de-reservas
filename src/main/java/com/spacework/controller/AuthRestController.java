package com.spacework.controller;

import com.spacework.dto.ApiResponse;
import com.spacework.model.Usuario;
import com.spacework.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@RequestBody Map<String, String> request) {
        String token = authService.autenticar(request.get("username"), request.get("password"));
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(ApiResponse.ok(response, "Login exitoso"));
    }

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<Usuario>> registrar(@RequestBody Usuario usuario) {
        Usuario registrado = authService.registrarUsuario(usuario);
        return ResponseEntity.ok(ApiResponse.ok(registrado, "Usuario registrado exitosamente"));
    }
}
