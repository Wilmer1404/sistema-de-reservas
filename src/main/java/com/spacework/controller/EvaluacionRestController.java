package com.spacework.controller;

import com.spacework.dto.ApiResponse;
import com.spacework.model.Evaluacion;
import com.spacework.service.EvaluacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluaciones")
public class EvaluacionRestController {

    @Autowired
    private EvaluacionService evaluacionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listar() {
        try {
            return ResponseEntity.ok(ApiResponse.ok(evaluacionService.listar()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Evaluacion>> buscarPorId(@PathVariable int id) {
        try {
            Evaluacion ev = evaluacionService.buscarPorId(id);
            if (ev == null) return ResponseEntity.status(404).body(ApiResponse.error("Evaluación no encontrada"));
            return ResponseEntity.ok(ApiResponse.ok(ev));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> crear(@RequestBody Map<String, Object> body) {
        try {
            int idReserva    = Integer.parseInt(String.valueOf(body.get("idReserva")));
            int calificacion = Integer.parseInt(String.valueOf(body.get("calificacion")));
            String comentario = body.containsKey("comentario") ? String.valueOf(body.get("comentario")) : "";
            evaluacionService.crear(idReserva, calificacion, comentario);
            return ResponseEntity.ok(ApiResponse.<Void>ok("Evaluación registrada"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> actualizar(@PathVariable int id, @RequestBody Map<String, Object> body) {
        try {
            Integer calificacion = body.containsKey("calificacion") ? Integer.parseInt(String.valueOf(body.get("calificacion"))) : null;
            String comentario    = body.containsKey("comentario")   ? String.valueOf(body.get("comentario")) : null;
            evaluacionService.actualizar(id, calificacion, comentario);
            return ResponseEntity.ok(ApiResponse.<Void>ok("Evaluación actualizada"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable int id) {
        try {
            evaluacionService.eliminar(id);
            return ResponseEntity.ok(ApiResponse.<Void>ok("Evaluación eliminada"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/enviar/{idNotificacion}")
    public ResponseEntity<ApiResponse<Void>> enviarDesdeNotificacion(@PathVariable int idNotificacion) {
        try {
            String email = evaluacionService.enviarDesdeNotificacion(idNotificacion);
            if (email != null) return ResponseEntity.ok(ApiResponse.<Void>ok("Email de evaluación enviado a " + email));
            return ResponseEntity.status(500).body(ApiResponse.error("No se pudo enviar el correo. Verifica mail.properties"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/enviar-reserva/{idReserva}")
    public ResponseEntity<ApiResponse<Void>> enviarPorReserva(@PathVariable int idReserva) {
        try {
            String email = evaluacionService.enviarPorReserva(idReserva);
            if (email != null) return ResponseEntity.ok(ApiResponse.<Void>ok("Email enviado a " + email));
            return ResponseEntity.status(500).body(ApiResponse.error("No se pudo enviar el correo. Verifica mail.properties"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }
}
