package com.spacework.dao;

import com.spacework.model.HorarioBloqueado;
import com.spacework.util.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HorarioBloqueadoDAO {

    public void registrar(HorarioBloqueado bloqueo) throws SQLException {
        String sql = "INSERT INTO HORARIOS_BLOQUEADOS (id_bloqueo, id_espacio, fecha_inicio, fecha_fin, razon, usuario_creador) " +
                     "VALUES (SEQ_BLOQUEOS.NEXTVAL, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bloqueo.getIdEspacio());
            pstmt.setTimestamp(2, new java.sql.Timestamp(bloqueo.getFechaInicio().getTime()));
            pstmt.setTimestamp(3, new java.sql.Timestamp(bloqueo.getFechaFin().getTime()));
            pstmt.setString(4, bloqueo.getRazon());
            pstmt.setString(5, bloqueo.getUsuarioCreador());
            pstmt.executeUpdate();
        }
    }

    public List<HorarioBloqueado> listarPorEspacio(int idEspacio) throws SQLException {
        List<HorarioBloqueado> lista = new ArrayList<>();
        String sql = "SELECT id_bloqueo, id_espacio, fecha_inicio, fecha_fin, razon, fecha_creacion, usuario_creador " +
                     "FROM HORARIOS_BLOQUEADOS WHERE id_espacio = ? ORDER BY fecha_inicio";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idEspacio);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new HorarioBloqueado(
                        rs.getInt("id_bloqueo"),
                        rs.getInt("id_espacio"),
                        new Date(rs.getTimestamp("fecha_inicio").getTime()),
                        new Date(rs.getTimestamp("fecha_fin").getTime()),
                        rs.getString("razon"),
                        new Date(rs.getTimestamp("fecha_creacion").getTime()),
                        rs.getString("usuario_creador")
                    ));
                }
            }
        }
        return lista;
    }

    public List<HorarioBloqueado> listarTodos() throws SQLException {
        List<HorarioBloqueado> lista = new ArrayList<>();
        String sql = "SELECT id_bloqueo, id_espacio, fecha_inicio, fecha_fin, razon, fecha_creacion, usuario_creador " +
                     "FROM HORARIOS_BLOQUEADOS ORDER BY fecha_inicio DESC";
        try (Connection conn = Conexion.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new HorarioBloqueado(
                    rs.getInt("id_bloqueo"),
                    rs.getInt("id_espacio"),
                    new Date(rs.getTimestamp("fecha_inicio").getTime()),
                    new Date(rs.getTimestamp("fecha_fin").getTime()),
                    rs.getString("razon"),
                    new Date(rs.getTimestamp("fecha_creacion").getTime()),
                    rs.getString("usuario_creador")
                ));
            }
        }
        return lista;
    }

    public void eliminar(int idBloqueo) throws SQLException {
        String sql = "DELETE FROM HORARIOS_BLOQUEADOS WHERE id_bloqueo = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idBloqueo);
            pstmt.executeUpdate();
        }
    }

    public boolean verificarDisponibilidadHorarios(int idEspacio, java.sql.Timestamp inicio, java.sql.Timestamp fin) throws SQLException {
        String sql = "SELECT COUNT(*) as cnt FROM HORARIOS_BLOQUEADOS " +
                     "WHERE id_espacio = ? AND NOT (fecha_fin <= ? OR fecha_inicio >= ?)";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idEspacio);
            pstmt.setTimestamp(2, inicio);
            pstmt.setTimestamp(3, fin);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") == 0;
                }
            }
        }
        return true;
    }
}
