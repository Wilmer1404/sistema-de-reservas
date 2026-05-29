package com.spacework.controller;

import com.spacework.dto.ApiResponse;
import com.spacework.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionRestController {

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listar() {
        try {
            return ResponseEntity.ok(ApiResponse.ok(notificacionService.listar()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/leida")
    public ResponseEntity<ApiResponse<Void>> marcarLeida(@PathVariable int id) {
        try {
            notificacionService.marcarLeida(id);
            return ResponseEntity.ok(ApiResponse.<Void>ok(null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }
}
