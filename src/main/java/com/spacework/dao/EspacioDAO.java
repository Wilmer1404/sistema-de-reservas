package com.spacework.dao;

import com.spacework.model.Espacio;
import com.spacework.util.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EspacioDAO {

    public List<Espacio> listar() throws SQLException {
        String sql = "SELECT * FROM ESPACIOS WHERE estado = 'ACTIVO' ORDER BY nombre";
        List<Espacio> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearEspacio(rs));
            }
        } finally {
            Conexion.cerrar(conn);
        }
        return lista;
    }

    public Espacio buscarPorId(int idEspacio) throws SQLException {
        String sql = "SELECT * FROM ESPACIOS WHERE id_espacio = ?";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idEspacio);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapearEspacio(rs);
            return null;
        } finally {
            Conexion.cerrar(conn);
        }
    }

    public void insertar(Espacio e) throws SQLException {
        String sql = "INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora, estado, urlImagen) "
               + "VALUES (SEQ_ESPACIOS.NEXTVAL, ?, ?, ?, ?, ?, 'ACTIVO', ?)";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getTipo());
            ps.setInt(3, e.getCapacidad());
            ps.setString(4, e.getUbicacion());
            ps.setDouble(5, e.getPrecioPorHora());
            ps.setString(6, e.getUrlImagen());
            ps.executeUpdate();
            conn.commit();
        } finally {
            Conexion.cerrar(conn);
        }
    }

    public void actualizar(Espacio e) throws SQLException {
        String sql = "UPDATE ESPACIOS SET nombre=?, tipo=?, capacidad=?, ubicacion=?, precio_por_hora=?, urlImagen=? "
                   + "WHERE id_espacio=?";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getTipo());
            ps.setInt(3, e.getCapacidad());
            ps.setString(4, e.getUbicacion());
            ps.setDouble(5, e.getPrecioPorHora());
            ps.setString(6, e.getUrlImagen());
            ps.setInt(7, e.getIdEspacio());
            ps.executeUpdate();
            conn.commit();
        } finally {
            Conexion.cerrar(conn);
        }
    }

    public void desactivar(int idEspacio) throws SQLException {
        String sql = "UPDATE ESPACIOS SET estado = 'INACTIVO' WHERE id_espacio = ?";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idEspacio);
            ps.executeUpdate();
            conn.commit();
        } finally {
            Conexion.cerrar(conn);
        }
    }

    private Espacio mapearEspacio(ResultSet rs) throws SQLException {
        Espacio e = new Espacio();
        e.setIdEspacio(rs.getInt("id_espacio"));
        e.setNombre(rs.getString("nombre"));
        e.setTipo(rs.getString("tipo"));
        e.setCapacidad(rs.getInt("capacidad"));
        e.setUbicacion(rs.getString("ubicacion"));
        e.setPrecioPorHora(rs.getDouble("precio_por_hora"));
        e.setEstado(rs.getString("estado"));
        e.setUrlImagen(rs.getString("urlImagen"));
        return e;
    }
}
