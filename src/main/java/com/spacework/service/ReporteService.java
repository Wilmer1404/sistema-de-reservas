package com.spacework.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.spacework.dao.ReporteDAO;

@Service
public class ReporteService {

    private final ReporteDAO reporteDAO = new ReporteDAO();

    public Map<String, Object> ingresosMensuales(int anio) throws SQLException {
        Map<String, Double> datos = reporteDAO.ingresosMensuales(anio);
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("anio", anio);
        respuesta.put("datos", datos);
        respuesta.put("total", datos.values().stream().mapToDouble(Double::doubleValue).sum());
        return respuesta;
    }

    public Map<String, Object> reservasPorEstado() throws SQLException {
        Map<String, Integer> datos = reporteDAO.reservasPorEstado();
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("datos", datos);
        respuesta.put("totalReservas", datos.values().stream().mapToInt(Integer::intValue).sum());
        return respuesta;
    }

    public Map<String, Object> ocupacionPorEspacio() throws SQLException {
        Map<String, Integer> datos = reporteDAO.ocupacionPorEspacio();
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("datos", datos);
        respuesta.put("totalEspacios", datos.size());
        return respuesta;
    }
}
