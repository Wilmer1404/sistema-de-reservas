package com.spacework.controller;

import com.spacework.dto.ApiResponse;
import com.spacework.model.Reserva;
import com.spacework.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaRestController {
    @Autowired
    private ReservaService reservaService;

    @PostMapping
    public ResponseEntity<ApiResponse<Reserva>> crear(@RequestBody Reserva reserva) {
        Reserva creada = reservaService.crearReserva(reserva);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(creada, "Reserva creada"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Reserva>> obtener(@PathVariable Long id) {
        // TODO: Implementar obtención de reserva individual
        return ResponseEntity.ok(ApiResponse.error("No implementado"));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<Void>> cancelar(@PathVariable Long id) {
        reservaService.cancelarReserva(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Reserva cancelada"));
    }

    @PutMapping("/{id}/completar")
    public ResponseEntity<ApiResponse<Void>> completar(@PathVariable Long id) {
        reservaService.completarReserva(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Reserva completada"));
    }
}
