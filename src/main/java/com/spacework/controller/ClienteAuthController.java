package com.spacework.controller;

import com.spacework.dao.ClienteDAO;
import com.spacework.model.Cliente;
import com.spacework.util.HashUtil;

import java.sql.SQLException;

public class ClienteAuthController {

    private static ClienteAuthController instancia;
    private static Cliente clienteActual;

    private final ClienteDAO clienteDAO = new ClienteDAO();

    private ClienteAuthController() {}

    public static ClienteAuthController getInstance() {
        if (instancia == null) {
            instancia = new ClienteAuthController();
        }
        return instancia;
    }

    /**
     * Intenta autenticar al cliente por email o dni.
     * @return true si las credenciales son válidas, false si no.
     */
    public boolean login(String emailODni, String password) throws SQLException {
        Cliente c = null;
        if (emailODni.contains("@")) {
            c = clienteDAO.buscarPorEmail(emailODni);
        } else if (emailODni.matches("\\d{8}")) {
            c = clienteDAO.buscarPorDni(emailODni);
        }
        if (c != null && c.getPassword() != null) {
            String hash = HashUtil.sha256(password);
            if (hash.equals(c.getPassword())) {
                clienteActual = c;
                return true;
            }
        }
        return false;
    }

    public void logout() {
        clienteActual = null;
    }

    public Cliente getClienteActual() {
        return clienteActual;
    }

    public boolean estaAutenticado() {
        return clienteActual != null;
    }
}
