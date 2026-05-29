package com.spacework.controller;

import com.spacework.dto.ApiResponse;
import com.spacework.model.Pago;
import com.spacework.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
public class PagoRestController {

    @Autowired
    private PagoService pagoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listar() {
        try {
            return ResponseEntity.ok(ApiResponse.ok(pagoService.listar()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Pago>> buscarPorId(@PathVariable int id) {
        try {
            Pago p = pagoService.buscarPorId(id);
            if (p == null) return ResponseEntity.status(404).body(ApiResponse.error("Pago no encontrado"));
            return ResponseEntity.ok(ApiResponse.ok(p));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> insertar(@RequestBody Map<String, Object> body) {
        try {
            Pago p = new Pago();
            p.setIdReserva(Integer.parseInt(String.valueOf(body.get("idReserva"))));
            p.setMonto(Double.parseDouble(String.valueOf(body.get("monto"))));
            p.setMetodoPago(String.valueOf(body.getOrDefault("metodoPago", "EFECTIVO")));
            pagoService.insertar(p);
            return ResponseEntity.ok(ApiResponse.<Void>ok(null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> actualizar(@PathVariable int id, @RequestBody Map<String, Object> body) {
        try {
            String estadoPago = body.containsKey("estadoPago") ? String.valueOf(body.get("estadoPago")) : null;
            String metodoPago = body.containsKey("metodoPago") ? String.valueOf(body.get("metodoPago")) : null;
            Double monto      = body.containsKey("monto")      ? Double.parseDouble(String.valueOf(body.get("monto"))) : null;
            pagoService.actualizar(id, estadoPago, metodoPago, monto);
            return ResponseEntity.ok(ApiResponse.<Void>ok("Pago actualizado"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<ApiResponse<Map<String, Object>>> pagar(@PathVariable int id,
                                                                   @RequestBody Map<String, Object> body,
                                                                   HttpServletRequest request) {
        try {
            String metodoPago = String.valueOf(body.get("metodoPago"));

            // Validación de tarjeta en el controller
            if ("TARJETA".equals(metodoPago)) {
                String numeroTarjeta = body.containsKey("numeroTarjeta") ? String.valueOf(body.get("numeroTarjeta")) : null;
                String expiracion    = body.containsKey("expiracion")    ? String.valueOf(body.get("expiracion"))    : null;
                String cvv           = body.containsKey("cvv")           ? String.valueOf(body.get("cvv"))           : null;
                if (numeroTarjeta == null || !numeroTarjeta.matches("\\d{16}"))
                    return ResponseEntity.status(400).body(ApiResponse.error("Número de tarjeta inválido (debe ser 16 dígitos)"));
                if (expiracion == null || !expiracion.matches("\\d{2}/\\d{2}"))
                    return ResponseEntity.status(400).body(ApiResponse.error("Fecha de expiración inválida (formato MM/YY)"));
                if (cvv == null || !cvv.matches("\\d{3,4}"))
                    return ResponseEntity.status(400).body(ApiResponse.error("CVV inválido (debe ser 3-4 dígitos)"));
                if ("4000000000000002".equals(numeroTarjeta))
                    return ResponseEntity.status(400).body(ApiResponse.error("Pasarela de pago: Transacción rechazada."));
            }

            String authHeader = request.getHeader("Authorization");
            String token = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;

            Object mfObj = body.get("montoFinal");
            Double montoFinal = (mfObj != null && !"null".equals(String.valueOf(mfObj)))
                    ? Double.parseDouble(String.valueOf(mfObj)) : null;

            Object idDObj = body.get("idDescuento");
            Integer idDescuento = (idDObj != null && !"null".equals(String.valueOf(idDObj)))
                    ? Integer.parseInt(String.valueOf(idDObj)) : null;

            Map<String, Object> result = pagoService.pagar(id, metodoPago, token, montoFinal, idDescuento);
            return ResponseEntity.ok(ApiResponse.ok(result, "Pago procesado correctamente"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error("Acceso denegado: " + e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> rechazar(@PathVariable int id) {
        try {
            pagoService.rechazar(id);
            return ResponseEntity.ok(ApiResponse.<Void>ok("Pago marcado como RECHAZADO"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }
}
