package com.spacework.service;

import com.spacework.dao.ClienteDAO;
import com.spacework.dao.UsuarioDAO;
import com.spacework.model.Cliente;
import com.spacework.model.Usuario;
import com.spacework.util.HashUtil;
import com.spacework.util.SimpleJwtUtil;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    public Map<String, Object> loginAdmin(String username, String password) throws Exception {
        if (username == null || password == null)
            throw new IllegalArgumentException("Faltan credenciales");

        String hash = HashUtil.sha256(password);
        Usuario u = usuarioDAO.autenticar(username, hash);
        if (u == null)
            throw new SecurityException("Credenciales inválidas");

        String token = SimpleJwtUtil.generarToken(u.getUsername(), u.getNombre(), u.getEmail(), u.getRol());

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("idUsuario", u.getIdUsuario());
        user.put("username", u.getUsername());
        user.put("nombre", u.getNombre());
        user.put("email", u.getEmail());
        user.put("rol", u.getRol());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    public Map<String, Object> loginCliente(String emailODni, String password) throws Exception {
        if (emailODni == null || password == null)
            throw new IllegalArgumentException("Faltan credenciales");

        Cliente c = null;
        if (emailODni.contains("@")) {
            c = clienteDAO.buscarPorEmail(emailODni);
        } else if (emailODni.matches("\\d{8}")) {
            c = clienteDAO.buscarPorDni(emailODni);
        }

        if (c == null || c.getPassword() == null)
            throw new SecurityException("Usuario o contraseña inválidos");

        String hash = HashUtil.sha256(password);
        if (!hash.equals(c.getPassword()))
            throw new SecurityException("Usuario o contraseña inválidos");

        String token = SimpleJwtUtil.generarToken(c.getEmail(), c.getNombre(), c.getEmail(), "CLIENTE");

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("idUsuario", c.getIdCliente());
        user.put("idCliente", c.getIdCliente());
        user.put("nombre", c.getNombre());
        user.put("email", c.getEmail());
        user.put("dni", c.getDni());
        user.put("rol", "CLIENTE");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }
}
