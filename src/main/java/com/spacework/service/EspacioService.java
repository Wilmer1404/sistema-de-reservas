package com.spacework.service;

import com.spacework.dao.EspacioDAO;
import com.spacework.model.Espacio;
import com.spacework.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EspacioService {
    @Autowired
    private EspacioDAO espacioDAO;

    public Espacio obtenerPorId(Long idEspacio) {
        // Implementar con el DAO existente
        return null; // TODO: Implementar
    }

    public List<Espacio> obtenerTodos() {
        // TODO: Implementar
        return null;
    }

    public List<Espacio> obtenerPorTipo(String tipo) {
        // TODO: Implementar
        return null;
    }

    public void crear(Espacio espacio) {
        try {
            espacioDAO.insertar(espacio);
        } catch (Exception ex) {
            throw new RuntimeException("Error al crear espacio: " + ex.getMessage(), ex);
        }
    }

    public void actualizar(Espacio espacio) {
        // TODO: Implementar actualización de espacio
    }

    public Double obtenerCalificacionPromedio(Long idEspacio) {
        return 0.0;
    }
}
