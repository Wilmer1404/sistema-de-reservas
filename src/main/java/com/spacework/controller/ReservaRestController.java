package com.spacework.controller;

import com.spacework.dto.ApiResponse;
import com.spacework.model.Reserva;
import com.spacework.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservas")
public class ReservaRestController {

    @Autowired
    private ReservaService reservaService;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Map<String, Object> reservaToMap(Reserva r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("idReserva",     r.getIdReserva());
        m.put("idCliente",     r.getCliente() != null ? r.getCliente().getIdCliente() : null);
        m.put("nombreCliente", r.getCliente() != null ? r.getCliente().getNombre() + " " + r.getCliente().getApellido() : "");
        m.put("idEspacio",     r.getEspacio() != null ? r.getEspacio().getIdEspacio() : null);
        m.put("nombreEspacio", r.getEspacio() != null ? r.getEspacio().getNombre() : "");
        m.put("fechaInicio",   r.getFechaInicio() != null ? SDF.format(r.getFechaInicio()) : null);
        m.put("fechaFin",      r.getFechaFin()    != null ? SDF.format(r.getFechaFin())    : null);
        m.put("montoTotal",    r.getMontoTotal());
        m.put("estado",        r.getEstado());
        return m;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listar(
            @RequestParam(required = false) Integer idCliente) {
        try {
            List<Reserva> lista = reservaService.listarTodas();
            if (idCliente != null) {
                final int id = idCliente;
                lista = lista.stream()
                        .filter(r -> r.getCliente() != null && r.getCliente().getIdCliente() == id)
                        .collect(Collectors.toList());
            }
            List<Map<String, Object>> result = lista.stream().map(this::reservaToMap).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> buscarPorId(@PathVariable int id) {
        try {
            Reserva r = reservaService.buscarPorId(id);
            if (r == null) return ResponseEntity.status(404).body(ApiResponse.error("Reserva no encontrada"));
            return ResponseEntity.ok(ApiResponse.ok(reservaToMap(r)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> crear(@RequestBody Map<String, Object> body) {
        try {
            int idCliente = Integer.parseInt(String.valueOf(body.get("idCliente")));
            int idEspacio = Integer.parseInt(String.valueOf(body.get("idEspacio")));
            String fechaInicio = String.valueOf(body.get("fechaInicio"));
            String fechaFin    = String.valueOf(body.get("fechaFin"));
            Map<String, Object> result = reservaService.crear(idCliente, idEspacio, fechaInicio, fechaFin);
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> actualizar(@PathVariable int id,
                                                                        @RequestBody Map<String, Object> body) {
        try {
            int idCliente = body.containsKey("idCliente") ? Integer.parseInt(String.valueOf(body.get("idCliente"))) : 0;
            int idEspacio = body.containsKey("idEspacio") ? Integer.parseInt(String.valueOf(body.get("idEspacio"))) : 0;
            String fechaInicio = body.containsKey("fechaInicio") ? String.valueOf(body.get("fechaInicio")) : null;
            String fechaFin    = body.containsKey("fechaFin")    ? String.valueOf(body.get("fechaFin"))    : null;
            Map<String, Object> result = reservaService.actualizar(id, idCliente, idEspacio, fechaInicio, fechaFin);
            return ResponseEntity.ok(ApiResponse.ok(result, "Reserva actualizada"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/confirmar")
    public ResponseEntity<ApiResponse<Void>> confirmar(@PathVariable int id) {
        try {
            reservaService.confirmar(id);
            return ResponseEntity.ok(ApiResponse.<Void>ok("Reserva confirmada. Pago creado automáticamente."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/completar")
    public ResponseEntity<ApiResponse<Void>> completar(@PathVariable int id) {
        try {
            reservaService.cambiarEstado(id, "COMPLETADA");
            return ResponseEntity.ok(ApiResponse.<Void>ok(null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelar(@PathVariable int id) {
        try {
            reservaService.cambiarEstado(id, "CANCELADA");
            return ResponseEntity.ok(ApiResponse.<Void>ok(null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }
}
