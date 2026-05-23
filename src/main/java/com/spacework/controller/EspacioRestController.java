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
    public ResponseEntity<ApiResponse<List<Espacio>>> obtenerTodos() {
        List<Espacio> espacios = espacioService.obtenerTodos();
        return ResponseEntity.ok(ApiResponse.ok(espacios, "Espacios obtenidos"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Espacio>> obtenerPorId(@PathVariable Long id) {
        Espacio espacio = espacioService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.ok(espacio, "Espacio obtenido"));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<ApiResponse<List<Espacio>>> obtenerPorTipo(@PathVariable String tipo) {
        List<Espacio> espacios = espacioService.obtenerPorTipo(tipo);
        return ResponseEntity.ok(ApiResponse.ok(espacios, "Espacios del tipo encontrados"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> crear(@RequestBody Espacio espacio) {
        espacioService.crear(espacio);
        return ResponseEntity.ok(ApiResponse.ok(null, "Espacio creado"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> actualizar(@PathVariable Long id, @RequestBody Espacio espacio) {
        espacio.setIdEspacio(id.intValue());
        espacioService.actualizar(espacio);
        return ResponseEntity.ok(ApiResponse.ok(null, "Espacio actualizado"));
    }
}
