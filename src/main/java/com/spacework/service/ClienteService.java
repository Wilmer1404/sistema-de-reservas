package com.spacework.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spacework.dao.ClienteDAO;
import com.spacework.exception.BusinessException;
import com.spacework.model.Cliente;

@Service
public class ClienteService {
    @Autowired
    private ClienteDAO clienteDAO;

    public Cliente obtenerPorId(Long idCliente) {
        // TODO: Obtener cliente de BD
        return null;
    }

    public Cliente obtenerPorDni(String dni) {
        // TODO: Obtener cliente por DNI de BD
        return null;
    }

    public List<Cliente> obtenerTodos() {
        // TODO: Implementar obtener todos los clientes
        return null;
    }

    public void crear(Cliente cliente) {
        cliente.setEstado("ACTIVO");
        // TODO: Implementar creación de cliente
    }

    public void actualizar(Cliente cliente) {
        try {
            clienteDAO.actualizar(cliente);
        } catch (Exception e) {
            throw new BusinessException("Error al actualizar cliente: " + e.getMessage());
        }
    }

    public void eliminar(Long idCliente) {
        try {
            clienteDAO.desactivar(idCliente.intValue());
        } catch (Exception e) {
            throw new BusinessException("Error al eliminar cliente: " + e.getMessage());
        }
    }
}
