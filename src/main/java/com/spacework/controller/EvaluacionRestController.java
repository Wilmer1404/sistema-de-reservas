package com.spacework.controller;

import com.spacework.dto.ApiResponse;
import com.spacework.model.Evaluacion;
import com.spacework.model.TokenEvaluacion;
import com.spacework.service.EvaluacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/evaluaciones")
public class EvaluacionRestController {
    @Autowired
    private EvaluacionService evaluacionService;

    @PostMapping
    public ResponseEntity<ApiResponse<Evaluacion>> crear(@RequestBody Map<String, Object> request) {
        String token = (String) request.get("token");
        Double calificacion = ((Number) request.get("calificacion")).doubleValue();
        String comentario = (String) request.get("comentario");
        
        Evaluacion evaluacion = evaluacionService.crearEvaluacion(token, calificacion, comentario);
        return ResponseEntity.ok(ApiResponse.ok(evaluacion, "Evaluación registrada"));
    }

    @GetMapping("/validar-token")
    public ResponseEntity<ApiResponse<TokenEvaluacion>> validarToken(@RequestParam String token) {
        TokenEvaluacion tokenEval = evaluacionService.validarToken(token);
        return ResponseEntity.ok(ApiResponse.ok(tokenEval, "Token válido"));
    }
}
