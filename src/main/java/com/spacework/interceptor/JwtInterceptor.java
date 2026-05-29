package com.spacework.interceptor;

import com.spacework.util.SimpleJwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (SimpleJwtUtil.validarToken(token)) {
                request.setAttribute("usuario", SimpleJwtUtil.obtenerUsuario(token));
                request.setAttribute("rol", SimpleJwtUtil.obtenerRol(token));
            }
        }
        return true;
    }
}
