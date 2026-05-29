package com.spacework.service;

import com.spacework.dao.EspacioDAO;
import com.spacework.model.Espacio;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EspacioService {

    private final EspacioDAO espacioDAO = new EspacioDAO();

    public List<Espacio> listar() throws Exception {
        return espacioDAO.listar();
    }

    public Espacio buscarPorId(int id) throws Exception {
        return espacioDAO.buscarPorId(id);
    }

    public void insertar(Espacio e) throws Exception {
        e.setEstado("ACTIVO");
        espacioDAO.insertar(e);
    }

    public void actualizar(int id, Espacio datos) throws Exception {
        Espacio e = espacioDAO.buscarPorId(id);
        if (e == null) throw new IllegalArgumentException("Espacio no encontrado");
        if (datos.getNombre() != null && !datos.getNombre().isEmpty()) e.setNombre(datos.getNombre());
        if (datos.getTipo() != null && !datos.getTipo().isEmpty()) e.setTipo(datos.getTipo());
        if (datos.getCapacidad() > 0) e.setCapacidad(datos.getCapacidad());
        if (datos.getUbicacion() != null && !datos.getUbicacion().isEmpty()) e.setUbicacion(datos.getUbicacion());
        if (datos.getPrecioPorHora() > 0) e.setPrecioPorHora(datos.getPrecioPorHora());
        if (datos.getUrlImagen() != null && !datos.getUrlImagen().isEmpty()) e.setUrlImagen(datos.getUrlImagen());
        espacioDAO.actualizar(e);
    }

    public void desactivar(int id) throws Exception {
        espacioDAO.desactivar(id);
    }
}
