package com.spacework.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.spacework.util.Conexion;

@Service
public class NotificacionService {

    public List<Map<String, Object>> listar() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = "SELECT id_notificacion, id_usuario, tipo, asunto, mensaje, leida, fecha_creacion "
                   + "FROM NOTIFICACIONES "
                   + "ORDER BY leida ASC, fecha_creacion DESC";
        List<Map<String, Object>> lista = new ArrayList<>();
        Connection conn = Conexion.getConexion();
        try {
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("idNotificacion", rs.getInt("id_notificacion"));
                m.put("idUsuario", rs.getInt("id_usuario"));
                m.put("tipo", rs.getString("tipo"));
                m.put("asunto", rs.getString("asunto"));
                m.put("mensaje", rs.getString("mensaje"));
                m.put("estado", rs.getInt("leida") == 1 ? "LEIDA" : "NO_LEIDA");
                java.util.Date fc = rs.getDate("fecha_creacion");
                m.put("fechaCreacion", fc != null ? sdf.format(fc) : "");
                lista.add(m);
            }
        } finally {
            Conexion.cerrar(conn);
        }
        return lista;
    }

    public void marcarLeida(int idNotificacion) throws Exception {
        Connection conn = Conexion.getConexion();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE NOTIFICACIONES SET leida=1 WHERE id_notificacion=?");
            ps.setInt(1, idNotificacion);
            ps.executeUpdate();
            conn.commit();
        } finally {
            Conexion.cerrar(conn);
        }
    }
}
