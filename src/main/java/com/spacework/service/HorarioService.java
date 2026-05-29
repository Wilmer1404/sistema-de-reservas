package com.spacework.service;

import com.spacework.dao.HorarioBloqueadoDAO;
import com.spacework.model.HorarioBloqueado;
import com.spacework.util.Conexion;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class HorarioService {

    private final HorarioBloqueadoDAO horarioDAO = new HorarioBloqueadoDAO();

    public List<Map<String, Object>> listar() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = "SELECT hb.id_bloqueo, hb.id_espacio, hb.fecha_inicio, hb.fecha_fin, hb.razon, "
                   + "e.nombre AS nombre_espacio FROM HORARIOS_BLOQUEADOS hb "
                   + "JOIN ESPACIOS e ON hb.id_espacio = e.id_espacio ORDER BY hb.fecha_inicio DESC";
        List<Map<String, Object>> lista = new ArrayList<>();
        Connection conn = Conexion.getConexion();
        try {
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("idHorarioBloqueado", rs.getInt("id_bloqueo"));
                m.put("idEspacio", rs.getInt("id_espacio"));
                m.put("nombreEspacio", rs.getString("nombre_espacio"));
                Timestamp tsIni = rs.getTimestamp("fecha_inicio");
                Timestamp tsFin = rs.getTimestamp("fecha_fin");
                m.put("fechaInicio", tsIni != null ? sdf.format(tsIni) : "");
                m.put("fechaFin", tsFin != null ? sdf.format(tsFin) : "");
                m.put("razon", rs.getString("razon") != null ? rs.getString("razon") : "");
                lista.add(m);
            }
        } finally {
            Conexion.cerrar(conn);
        }
        return lista;
    }

    public void registrar(int idEspacio, String fechaIniStr, String fechaFinStr, String razon) throws Exception {
        String fi = fechaIniStr.replace("T", " ");
        String ff = fechaFinStr.replace("T", " ");
        if (fi.length() == 16) fi += ":00";
        if (ff.length() == 16) ff += ":00";
        Timestamp tsIni = Timestamp.valueOf(fi);
        Timestamp tsFin = Timestamp.valueOf(ff);
        HorarioBloqueado h = new HorarioBloqueado(idEspacio, tsIni, tsFin, razon, "admin");
        horarioDAO.registrar(h);
    }

    public void eliminar(int id) throws Exception {
        horarioDAO.eliminar(id);
    }
}
