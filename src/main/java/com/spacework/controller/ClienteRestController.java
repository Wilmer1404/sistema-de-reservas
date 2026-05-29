package com.spacework.controller;

import com.spacework.dto.ApiResponse;
import com.spacework.model.Cliente;
import com.spacework.service.AuthService;
import com.spacework.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteRestController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private AuthService authService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Cliente>>> listar() {
        try {
            return ResponseEntity.ok(ApiResponse.ok(clienteService.listar()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Cliente>> buscarPorId(@PathVariable int id) {
        try {
            Cliente c = clienteService.buscarPorId(id);
            if (c == null) return ResponseEntity.status(404).body(ApiResponse.error("No encontrado"));
            return ResponseEntity.ok(ApiResponse.ok(c));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, String> body) {
        try {
            Map<String, Object> result = authService.loginCliente(body.get("username"), body.get("password"));
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<ApiResponse<Void>> recuperarPassword(@RequestBody Map<String, String> body) {
        try {
            clienteService.recuperarPassword(body.get("email"));
            return ResponseEntity.ok(ApiResponse.<Void>ok("Contraseña restablecida. Revisa tu correo."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> registrar(@RequestBody Cliente cliente) {
        try {
            clienteService.registrar(cliente);
            return ResponseEntity.ok(ApiResponse.<Void>ok("Cuenta creada exitosamente. Te hemos enviado tu contraseña al correo registrado."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> actualizar(@PathVariable int id, @RequestBody Cliente datos) {
        try {
            clienteService.actualizar(id, datos);
            return ResponseEntity.ok(ApiResponse.<Void>ok("Cliente actualizado"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable int id) {
        try {
            clienteService.desactivar(id);
            return ResponseEntity.ok(ApiResponse.<Void>ok(null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }
}
