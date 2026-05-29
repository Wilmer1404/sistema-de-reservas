package com.spacework.controller;

import com.spacework.dto.ApiResponse;
import com.spacework.service.HorarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/horarios")
public class HorarioRestController {

    @Autowired
    private HorarioService horarioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listar() {
        try {
            return ResponseEntity.ok(ApiResponse.ok(horarioService.listar()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> registrar(@RequestBody Map<String, Object> body) {
        try {
            int idEspacio      = Integer.parseInt(String.valueOf(body.get("idEspacio")));
            String fechaInicio = String.valueOf(body.get("fechaInicio"));
            String fechaFin    = String.valueOf(body.get("fechaFin"));
            String razon       = body.containsKey("razon") ? String.valueOf(body.get("razon")) : "";
            horarioService.registrar(idEspacio, fechaInicio, fechaFin, razon);
            return ResponseEntity.ok(ApiResponse.<Void>ok(null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable int id) {
        try {
            horarioService.eliminar(id);
            return ResponseEntity.ok(ApiResponse.<Void>ok(null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }
}
