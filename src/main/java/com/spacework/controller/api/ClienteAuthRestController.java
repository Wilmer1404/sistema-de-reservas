package com.spacework.controller.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.spacework.model.Cliente;
import com.spacework.controller.ClienteAuthController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Handler HTTP para autenticación de clientes
 */
public class ClienteAuthRestController implements HttpHandler {

    private final ClienteAuthController clienteAuthController = ClienteAuthController.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
                return;
            }

            if ("POST".equals(method) && path.contains("login")) {
                handleLogin(exchange);
            } else {
                sendError(exchange, 404, "Ruta no encontrada");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = leerBody(exchange);
        String username = extraerValorJson(body, "username");
        String password = extraerValorJson(body, "password");

        if (username == null || password == null) {
            sendError(exchange, 400, "Usuario y contraseña requeridos");
            return;
        }

        try {
            boolean ok = clienteAuthController.login(username, password);
            if (ok) {
                Cliente c = clienteAuthController.getClienteActual();
                // Generar JWT real para que pase la validación de requireAuth
                com.spacework.util.JwtUtil jwtUtil = new com.spacework.util.JwtUtil();
                String token = jwtUtil.generarToken(c.getEmail(), c.getNombre(), c.getEmail(), "CLIENTE");
                String json = String.format(
                    "{\"success\": true, \"message\": \"Login exitoso\", \"token\": \"%s\", \"user\": {\"id\": %d, \"nombre\": \"%s\", \"email\": \"%s\", \"dni\": \"%s\", \"rol\": \"CLIENTE\"}}",
                    token, c.getIdCliente(), c.getNombre(), c.getEmail(), c.getDni()
                );
                sendResponse(exchange, 200, json);
            } else {
                sendError(exchange, 401, "Usuario o contraseña inválidos");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error en autenticación: " + e.getMessage());
        }
    }

    private String leerBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int n;
        while ((n = is.read(buffer)) != -1) {
            baos.write(buffer, 0, n);
        }
        return baos.toString(StandardCharsets.UTF_8.name());
    }

    private String extraerValorJson(String json, String clave) {
        String buscar = "\"" + clave + "\"";
        int pos = json.indexOf(buscar);
        if (pos == -1) return null;
        int inicio = json.indexOf("\"", pos + buscar.length() + 1);
        if (inicio == -1) return null;
        int fin = json.indexOf("\"", inicio + 1);
        if (fin == -1) return null;
        return json.substring(inicio + 1, fin);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String json = String.format("{\"success\": false, \"error\": \"%s\"}", message);
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }
}
