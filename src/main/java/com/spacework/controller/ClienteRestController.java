package com.spacework.controller;

import com.spacework.dto.ApiResponse;
import com.spacework.model.Cliente;
import com.spacework.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteRestController {
    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Cliente>>> obtenerTodos() {
        List<Cliente> clientes = clienteService.obtenerTodos();
        return ResponseEntity.ok(ApiResponse.ok(clientes, "Clientes obtenidos"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Cliente>> obtenerPorId(@PathVariable Long id) {
        Cliente cliente = clienteService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.ok(cliente, "Cliente obtenido"));
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<ApiResponse<Cliente>> obtenerPorDni(@PathVariable String dni) {
        Cliente cliente = clienteService.obtenerPorDni(dni);
        return ResponseEntity.ok(ApiResponse.ok(cliente, "Cliente obtenido"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Cliente>> crear(@RequestBody Cliente cliente) {
        clienteService.crear(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(cliente, "Cliente creado"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> actualizar(@PathVariable Long id, @RequestBody Cliente cliente) {
        cliente.setIdCliente(id.intValue());
        clienteService.actualizar(cliente);
        return ResponseEntity.ok(ApiResponse.ok(null, "Cliente actualizado"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Cliente eliminado"));
    }
}
