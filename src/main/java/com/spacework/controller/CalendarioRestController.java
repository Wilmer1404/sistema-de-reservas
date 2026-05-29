package com.spacework.controller;

import com.spacework.dto.ApiResponse;
import com.spacework.service.CalendarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/calendario")
public class CalendarioRestController {

    @Autowired
    private CalendarioService calendarioService;

    @GetMapping("/semanal")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSemanal() {
        try {
            return ResponseEntity.ok(ApiResponse.ok(calendarioService.getSemanal()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }
}
