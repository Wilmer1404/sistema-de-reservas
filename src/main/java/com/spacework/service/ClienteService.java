package com.spacework.service;

import com.spacework.dao.ClienteDAO;
import com.spacework.model.Cliente;
import com.spacework.util.Conexion;
import com.spacework.util.EmailUtil;
import com.spacework.util.HashUtil;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.List;
import java.util.Random;

@Service
public class ClienteService {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    public List<Cliente> listar() throws Exception {
        return clienteDAO.listar();
    }

    public Cliente buscarPorId(int id) throws Exception {
        List<Cliente> todos = clienteDAO.listar();
        for (Cliente c : todos) {
            if (c.getIdCliente() == id) return c;
        }
        return null;
    }

    public void registrar(Cliente c) throws Exception {
        String passwordBody = c.getPassword();
        boolean passwordProvista = passwordBody != null && !passwordBody.trim().isEmpty();
        String passwordPlano = passwordProvista ? passwordBody : generarPasswordAleatorio();
        c.setPassword(HashUtil.sha256(passwordPlano));
        c.setEstado("ACTIVO");

        try {
            clienteDAO.insertar(c);
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "";
            if (msg.contains("UQ_EMAIL_CLIENTE") || msg.contains("UQ_EMAIL")) {
                throw new IllegalArgumentException("Este email ya está registrado. Usa \"Recuperar Contraseña\" si olvidaste tu acceso.");
            } else if (msg.contains("UQ_DNI")) {
                throw new IllegalArgumentException("Este DNI ya está registrado. Usa \"Recuperar Contraseña\" si olvidaste tu acceso.");
            }
            throw ex;
        }

        try {
            EmailUtil.enviarCredencialesCliente(c.getEmail(), c.getNombre(), passwordPlano);
        } catch (Exception emailEx) {
            System.out.println("[REGISTRO] Email no enviado: " + emailEx.getMessage());
        }
    }

    public void actualizar(int id, Cliente datos) throws Exception {
        Cliente c = buscarPorId(id);
        if (c == null) throw new IllegalArgumentException("Cliente no encontrado");
        if (datos.getNombre() != null) c.setNombre(datos.getNombre());
        if (datos.getApellido() != null) c.setApellido(datos.getApellido());
        if (datos.getDni() != null) c.setDni(datos.getDni());
        if (datos.getEmail() != null) c.setEmail(datos.getEmail());
        if (datos.getTelefono() != null) c.setTelefono(datos.getTelefono());
        clienteDAO.actualizar(c);
    }

    public void desactivar(int id) throws Exception {
        clienteDAO.desactivar(id);
    }

    public void recuperarPassword(String email) throws Exception {
        Cliente c = clienteDAO.buscarPorEmail(email);
        if (c == null) throw new IllegalArgumentException("No existe una cuenta con ese email");

        String nuevaPassword = generarPasswordAleatorio();
        String hashNuevo = HashUtil.sha256(nuevaPassword);

        Connection conn = Conexion.getConexion();
        try {
            java.sql.PreparedStatement ps = conn.prepareStatement(
                "UPDATE CLIENTES SET password = ? WHERE email = ?");
            ps.setString(1, hashNuevo);
            ps.setString(2, email);
            ps.executeUpdate();
            conn.commit();
        } finally {
            Conexion.cerrar(conn);
        }

        try {
            EmailUtil.enviarCredencialesCliente(email, c.getNombre(), nuevaPassword);
        } catch (Exception emailEx) {
            System.out.println("[RECUPERAR] Email no enviado: " + emailEx.getMessage());
        }
    }

    private String generarPasswordAleatorio() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < 8; i++) sb.append(chars.charAt(rand.nextInt(chars.length())));
        return sb.toString();
    }
}
