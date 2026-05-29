package com.spacework.controller;

import com.spacework.dto.ApiResponse;
import com.spacework.model.Descuento;
import com.spacework.service.DescuentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/descuentos")
public class DescuentoRestController {

    @Autowired
    private DescuentoService descuentoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Descuento>>> listar() {
        try {
            return ResponseEntity.ok(ApiResponse.ok(descuentoService.listar()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Descuento>> buscarPorId(@PathVariable int id) {
        try {
            Descuento d = descuentoService.buscarPorId(id);
            if (d == null) return ResponseEntity.status(404).body(ApiResponse.error("No encontrado"));
            return ResponseEntity.ok(ApiResponse.ok(d));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> insertar(@RequestBody Map<String, Object> body) {
        try {
            Descuento d = mapToDescuento(body);
            descuentoService.insertar(d);
            return ResponseEntity.ok(ApiResponse.<Void>ok(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> actualizar(@PathVariable int id, @RequestBody Map<String, Object> body) {
        try {
            Descuento d = mapToDescuento(body);
            descuentoService.actualizar(id, d);
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
            descuentoService.desactivar(id);
            return ResponseEntity.ok(ApiResponse.<Void>ok(null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/validar")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validar(@RequestBody Map<String, Object> body) {
        try {
            String codigo = String.valueOf(body.get("codigo"));
            double monto  = body.containsKey("monto") ? Double.parseDouble(String.valueOf(body.get("monto"))) : 0;
            Descuento d = descuentoService.validar(codigo, monto);
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("porcentaje", d.getPorcentaje());
            result.put("descripcion", d.getDescripcion());
            result.put("idDescuento", d.getIdDescuento());
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    private Descuento mapToDescuento(Map<String, Object> body) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Descuento d = new Descuento();
        if (body.containsKey("codigo"))       d.setCodigo(String.valueOf(body.get("codigo")));
        if (body.containsKey("descripcion"))  d.setDescripcion(String.valueOf(body.get("descripcion")));
        if (body.containsKey("porcentaje"))   d.setPorcentaje(Double.parseDouble(String.valueOf(body.get("porcentaje"))));
        if (body.containsKey("montoMinimo"))  d.setMontoMinimo(Double.parseDouble(String.valueOf(body.get("montoMinimo"))));
        if (body.containsKey("usosMaximos"))  d.setUsosMaximos(Integer.parseInt(String.valueOf(body.get("usosMaximos"))));
        if (body.containsKey("estado"))       d.setEstado(String.valueOf(body.get("estado")));
        if (body.containsKey("fechaInicio") && body.get("fechaInicio") != null)
            d.setFechaInicio(sdf.parse(String.valueOf(body.get("fechaInicio"))));
        if (body.containsKey("fechaFin") && body.get("fechaFin") != null)
            d.setFechaFin(sdf.parse(String.valueOf(body.get("fechaFin"))));
        return d;
    }
}
