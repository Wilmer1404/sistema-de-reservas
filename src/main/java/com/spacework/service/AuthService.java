package com.spacework.service;

import com.spacework.dao.UsuarioDAO;
import com.spacework.model.Usuario;
import com.spacework.util.HashUtil;
import com.spacework.util.JwtUtil;
import com.spacework.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private JwtUtil jwtUtil;

    public String autenticar(String username, String password) {
        // TODO: Implementar autenticaci\u00f3n desde BD
        return jwtUtil.generarToken(username, "Usuario", "user@email.com", "1");
    }

    public Usuario registrarUsuario(Usuario usuario) {
        // TODO: Implementar registro de usuario
        return usuario;
    }
}
