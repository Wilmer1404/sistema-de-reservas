package com.spacework.controller;

import com.spacework.dto.ApiResponse;
import com.spacework.model.Pago;
import com.spacework.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
public class PagoRestController {
    @Autowired
    private PagoService pagoService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Pago>> obtener(@PathVariable Long id) {
        // TODO: Implementar obtención de pago
        return ResponseEntity.ok(ApiResponse.error("No implementado"));
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<ApiResponse<Pago>> procesarPago(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String metodoPago = request.get("metodoPago");
        String codigoDescuento = request.get("codigoDescuento");
        
        Pago pago = pagoService.procesarPago(id, metodoPago, codigoDescuento);
        return ResponseEntity.ok(ApiResponse.ok(pago, "Pago procesado"));
    }
}
