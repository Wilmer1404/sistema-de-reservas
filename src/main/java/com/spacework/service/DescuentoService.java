package com.spacework.service;

import com.spacework.dao.DescuentoDAO;
import com.spacework.model.Descuento;
import com.spacework.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DescuentoService {
    @Autowired
    private DescuentoDAO descuentoDAO;

    public Descuento obtenerPorCodigo(String codigo) {
        // TODO: Implementar obtener por código
        return null;
    }

    public List<Descuento> obtenerTodos() {
        // TODO: Implementar obtener todos
        return null;
    }

    public Descuento obtenerPorId(Long idDescuento) {
        // TODO: Implementar obtener por ID
        return null;
    }

    public void crear(Descuento descuento) {
        // TODO: Implementar creación
    }

    public void actualizar(Descuento descuento) {
        // TODO: Implementar actualización
    }

    public boolean validarDescuento(String codigo, double montoCompra) {
        // TODO: Validar descuento desde BD
        return false;
    }

    public double calcularDescuento(String codigo, double montoCompra) {
        // TODO: Calcular descuento desde BD
        return 0.0;
    }
}
