package com.spacework.dao;

import com.spacework.model.Cliente;
import com.spacework.util.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public List<Cliente> listar() throws SQLException {
        String sql = "SELECT * FROM CLIENTES WHERE estado = 'ACTIVO' ORDER BY apellido, nombre";
        List<Cliente> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            while (rs.next()) lista.add(mapearCliente(rs));
        } finally {
            Conexion.cerrar(conn);
        }
        return lista;
    }

    public Cliente buscarPorDni(String dni) throws SQLException {
        String sql = "SELECT * FROM CLIENTES WHERE dni = ?";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, dni);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapearCliente(rs);
            return null;
        } finally {
            Conexion.cerrar(conn);
        }
    }

    public void insertar(Cliente c) throws SQLException {
        String sql = "INSERT INTO CLIENTES (id_cliente, nombre, apellido, dni, email, telefono, estado, password) "
                   + "VALUES (SEQ_CLIENTES.NEXTVAL, ?, ?, ?, ?, ?, 'ACTIVO', ?)";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellido());
            ps.setString(3, c.getDni());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getPassword());
            ps.executeUpdate();
            conn.commit();
        } finally {
            Conexion.cerrar(conn);
        }
    }

    public void actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE CLIENTES SET nombre=?, apellido=?, email=?, telefono=? WHERE id_cliente=?";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellido());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getTelefono());
            ps.setInt(5, c.getIdCliente());
            ps.executeUpdate();
            conn.commit();
        } finally {
            Conexion.cerrar(conn);
        }
    }

    public void desactivar(int idCliente) throws SQLException {
        String sql = "UPDATE CLIENTES SET estado = 'INACTIVO' WHERE id_cliente = ?";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idCliente);
            ps.executeUpdate();
            conn.commit();
        } finally {
            Conexion.cerrar(conn);
        }
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setNombre(rs.getString("nombre"));
        c.setApellido(rs.getString("apellido"));
        c.setDni(rs.getString("dni"));
        c.setEmail(rs.getString("email"));
        c.setTelefono(rs.getString("telefono"));
        c.setEstado(rs.getString("estado"));
        c.setPassword(rs.getString("password"));
        return c;
    }

    // Buscar cliente por email (para login)
    public Cliente buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM CLIENTES WHERE email = ? AND estado = 'ACTIVO'";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapearCliente(rs);
            return null;
        } finally {
            Conexion.cerrar(conn);
        }
    }
}
