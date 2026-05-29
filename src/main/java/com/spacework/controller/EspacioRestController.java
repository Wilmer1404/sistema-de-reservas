package com.spacework.controller;

import com.spacework.dto.ApiResponse;
import com.spacework.model.Espacio;
import com.spacework.service.EspacioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/espacios")
public class EspacioRestController {

    @Autowired
    private EspacioService espacioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Espacio>>> listar() {
        try {
            return ResponseEntity.ok(ApiResponse.ok(espacioService.listar()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Espacio>> buscarPorId(@PathVariable int id) {
        try {
            Espacio e = espacioService.buscarPorId(id);
            if (e == null) return ResponseEntity.status(404).body(ApiResponse.error("No encontrado"));
            return ResponseEntity.ok(ApiResponse.ok(e));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> insertar(@RequestBody Espacio espacio) {
        try {
            espacioService.insertar(espacio);
            return ResponseEntity.ok(ApiResponse.<Void>ok(null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> actualizar(@PathVariable int id, @RequestBody Espacio datos) {
        try {
            espacioService.actualizar(id, datos);
            return ResponseEntity.ok(ApiResponse.<Void>ok(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable int id) {
        try {
            espacioService.desactivar(id);
            return ResponseEntity.ok(ApiResponse.<Void>ok(null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }
}
