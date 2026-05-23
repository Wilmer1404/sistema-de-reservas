package com.spacework.controller;

import com.spacework.dao.ClienteDAO;
import com.spacework.model.Cliente;
import com.spacework.util.HashUtil;
import com.spacework.util.EmailUtil;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.List;

public class ClienteController {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    public List<Cliente> listarClientes() throws SQLException {
        return clienteDAO.listar();
    }

    public Cliente buscarPorDni(String dni) throws SQLException {
        return clienteDAO.buscarPorDni(dni);
    }

    public void registrarCliente(String nombre, String apellido, String dni,
                                  String email, String telefono) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (apellido == null || apellido.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }
        if (dni == null || !dni.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("El email no tiene un formato válido.");
        }
        // Verificar que el DNI no esté registrado
        if (clienteDAO.buscarPorDni(dni) != null) {
            throw new IllegalArgumentException("Ya existe un cliente con ese DNI.");
        }
        // Verificar que el email no esté registrado
        if (clienteDAO.buscarPorEmail(email.trim()) != null) {
            throw new IllegalArgumentException("Ya existe un cliente con ese correo electrónico.");
        }
        // Generar contraseña aleatoria
        String passwordPlano = generarPassword(10);
        String passwordHash = HashUtil.sha256(passwordPlano);

        Cliente c = new Cliente();
        c.setNombre(nombre.trim());
        c.setApellido(apellido.trim());
        c.setDni(dni.trim());
        c.setEmail(email.trim());
        c.setTelefono(telefono != null ? telefono.trim() : "");
        c.setPassword(passwordHash);
        clienteDAO.insertar(c);

        // Enviar email de bienvenida con la contraseña
        EmailUtil.enviarCredencialesCliente(email.trim(), nombre.trim(), passwordPlano);
    }

    // Utilidad para generar contraseña segura
    private String generarPassword(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789@$%&*";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public void actualizarCliente(int idCliente, String nombre, String apellido,
                                   String email, String telefono) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (apellido == null || apellido.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("El email no tiene un formato válido.");
        }
        Cliente c = new Cliente();
        c.setIdCliente(idCliente);
        c.setNombre(nombre.trim());
        c.setApellido(apellido.trim());
        c.setEmail(email.trim());
        c.setTelefono(telefono != null ? telefono.trim() : "");
        clienteDAO.actualizar(c);
    }

    public void desactivarCliente(int idCliente) throws SQLException {
        clienteDAO.desactivar(idCliente);
    }
}
