package com.spacework.controller.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.spacework.model.Espacio;
import com.spacework.dao.EspacioDAO;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Handler HTTP para operaciones CRUD de Espacios
 */
public class EspacioRestController implements HttpHandler {

    private EspacioDAO espacioDAO = new EspacioDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equals(method)) {
                if (path.matches(".*/espacios$")) {
                    handleGetAllEspacios(exchange);
                } else if (path.matches(".*/espacios/\\d+$")) {
                    int id = extractId(path);
                    handleGetEspacio(exchange, id);
                } else {
                    sendError(exchange, 404, "Ruta no encontrada");
                }
            } else if ("POST".equals(method)) {
                handleCreateEspacio(exchange);
            } else if ("PUT".equals(method)) {
                int id = extractId(path);
                handleUpdateEspacio(exchange, id);
            } else if ("DELETE".equals(method)) {
                int id = extractId(path);
                handleDeleteEspacio(exchange, id);
            } else {
                sendError(exchange, 405, "Método no permitido");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleGetAllEspacios(HttpExchange exchange) throws IOException {
        try {
            java.util.List<Espacio> espacios = espacioDAO.listar();
            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\": true, \"count\": ").append(espacios.size()).append(", \"data\": [");
            for (int i = 0; i < espacios.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(espacios.get(i)));
            }
            sb.append("]}");
            sendResponse(exchange, 200, sb.toString());
        } catch (Exception e) {
            sendError(exchange, 500, "Error al listar espacios");
        }
    }

    private void handleGetEspacio(HttpExchange exchange, int id) throws IOException {
        try {
            Espacio espacio = espacioDAO.buscarPorId(id);
            if (espacio == null) {
                sendError(exchange, 404, "Espacio no encontrado");
            } else {
                String json = "{\"success\": true, \"data\": " + toJson(espacio) + "}";
                sendResponse(exchange, 200, json);
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleCreateEspacio(HttpExchange exchange) throws IOException {
        try {
            java.io.InputStream is = exchange.getRequestBody();
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
            String body = baos.toString("UTF-8");
            
            String nombre = extractJsonString(body, "nombre");
            String tipo = extractJsonString(body, "tipo");
            int capacidad = extractJsonInt(body, "capacidad");
            String ubicacion = extractJsonString(body, "ubicacion");
            double precio = extractJsonDouble(body, "precioPorHora");
            
            String urlImagen = extractJsonString(body, "urlImagen");
            Espacio espacio = new Espacio();
            espacio.setNombre(nombre);
            espacio.setTipo(tipo);
            espacio.setCapacidad(capacidad);
            espacio.setUbicacion(ubicacion);
            espacio.setPrecioPorHora(precio);
            espacio.setEstado("ACTIVO");
            espacio.setUrlImagen(urlImagen);

            espacioDAO.insertar(espacio);
            String json = "{\"success\": true, \"message\": \"Espacio creado exitosamente\"}";
            sendResponse(exchange, 201, json);
        } catch (Exception e) {
            sendError(exchange, 500, "Error al crear espacio: " + e.getMessage());
        }
    }

    private void handleUpdateEspacio(HttpExchange exchange, int id) throws IOException {
        try {
            java.io.InputStream is = exchange.getRequestBody();
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
            String body = baos.toString("UTF-8");

            String nombre = extractJsonString(body, "nombre");
            String tipo = extractJsonString(body, "tipo");
            int capacidad = extractJsonInt(body, "capacidad");
            String ubicacion = extractJsonString(body, "ubicacion");
            double precio = extractJsonDouble(body, "precioPorHora");

            String urlImagen = extractJsonString(body, "urlImagen");
            Espacio espacio = new Espacio();
            espacio.setIdEspacio(id);
            espacio.setNombre(nombre);
            espacio.setTipo(tipo);
            espacio.setCapacidad(capacidad);
            espacio.setUbicacion(ubicacion);
            espacio.setPrecioPorHora(precio);
            espacio.setUrlImagen(urlImagen);

            espacioDAO.actualizar(espacio);
            String json = "{\"success\": true, \"message\": \"Espacio actualizado\"}";
            sendResponse(exchange, 200, json);
        } catch (Exception e) {
            sendError(exchange, 500, "Error al actualizar: " + e.getMessage());
        }
    }

    private void handleDeleteEspacio(HttpExchange exchange, int id) throws IOException {
        try {
            espacioDAO.desactivar(id);
            String json = "{\"success\": true, \"message\": \"Espacio eliminado\"}";
            sendResponse(exchange, 200, json);
        } catch (Exception e) {
            sendError(exchange, 500, "Error al eliminar: " + e.getMessage());
        }
    }

    private String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return "";
        idx += pattern.length();
        int endIdx = json.indexOf("\"", idx);
        if (endIdx == -1) return "";
        return json.substring(idx, endIdx);
    }

    private int extractJsonInt(String json, String key) {
        String pattern = "\"" + key + "\":";
        int idx = json.indexOf(pattern);
        if (idx == -1) return 0;
        idx += pattern.length();
        int endIdx = json.indexOf(",", idx);
        if (endIdx == -1) endIdx = json.indexOf("}", idx);
        try {
            return Integer.parseInt(json.substring(idx, endIdx).trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private double extractJsonDouble(String json, String key) {
        String pattern = "\"" + key + "\":";
        int idx = json.indexOf(pattern);
        if (idx == -1) return 0.0;
        idx += pattern.length();
        int endIdx = json.indexOf(",", idx);
        if (endIdx == -1) endIdx = json.indexOf("}", idx);
        try {
            return Double.parseDouble(json.substring(idx, endIdx).trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        try {
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private <T> String toJsonArray(java.util.List<T> list) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) json.append(",");
            json.append(toJson(list.get(i)));
        }
        json.append("]");
        return json.toString();
    }

    private String toJson(Object obj) {
        if (obj instanceof Espacio) {
            Espacio e = (Espacio) obj;
            return String.format(
                "{\"idEspacio\": %d, \"nombre\": \"%s\", \"tipo\": \"%s\", \"capacidad\": %d, \"ubicacion\": \"%s\", \"precioPorHora\": %.2f, \"estado\": \"%s\", \"urlImagen\": %s}",
                e.getIdEspacio(), esc(e.getNombre()), esc(e.getTipo()), e.getCapacidad(),
                esc(e.getUbicacion()), e.getPrecioPorHora(), esc(e.getEstado()),
                e.getUrlImagen() != null ? ('"' + esc(e.getUrlImagen()) + '"') : "null"
            );
        }
        return "{}";
    }


    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
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
