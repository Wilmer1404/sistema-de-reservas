package com.spacework.controller;

import java.util.Calendar;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spacework.dto.ApiResponse;
import com.spacework.service.ReporteService;

@RestController
@RequestMapping("/api/reportes")
public class ReporteRestController {

    @Autowired
    private ReporteService reporteService;

    @GetMapping("/ingresos-mensuales")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ingresosMensuales(
            @RequestParam(value = "anio", defaultValue = "") String anioStr) {
        try {
            int anio = anioStr.isEmpty() ? Calendar.getInstance().get(Calendar.YEAR) : Integer.parseInt(anioStr);
            Map<String, Object> datos = reporteService.ingresosMensuales(anio);
            return ResponseEntity.ok(ApiResponse.ok(datos));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/reservas-por-estado")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reservasPorEstado() {
        try {
            Map<String, Object> datos = reporteService.reservasPorEstado();
            return ResponseEntity.ok(ApiResponse.ok(datos));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/ocupacion-espacios")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ocupacionPorEspacio() {
        try {
            Map<String, Object> datos = reporteService.ocupacionPorEspacio();
            return ResponseEntity.ok(ApiResponse.ok(datos));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }
}
