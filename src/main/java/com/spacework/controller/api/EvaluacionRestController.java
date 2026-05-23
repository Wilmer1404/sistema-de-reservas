package com.spacework.controller.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.spacework.model.Evaluacion;
import com.spacework.dao.EvaluacionDAO;
import com.spacework.controller.EvaluacionController;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Handler HTTP REST para operaciones CRUD de Evaluaciones
 * GET /api/evaluaciones -> listar todas
 * GET /api/evaluaciones/{id} -> obtener una
 * POST /api/evaluaciones -> crear
 * DELETE /api/evaluaciones/{id} -> eliminar
 */
public class EvaluacionRestController implements HttpHandler {

    private EvaluacionDAO evaluacionDAO = new EvaluacionDAO();
    private EvaluacionController evaluacionController = new EvaluacionController();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
                return;
            }

            if ("GET".equals(method)) {
                if (path.matches(".*/evaluaciones$")) {
                    handleGetAllEvaluaciones(exchange);
                } else if (path.matches(".*/evaluaciones/\\d+$")) {
                    int id = extractId(path);
                    if (path.contains("promedio")) {
                        handleGetPromedioEspacio(exchange, id);
                    } else if (path.contains("conteo")) {
                        handleGetConteoEspacio(exchange, id);
                    } else {
                        handleGetEvaluacion(exchange, id);
                    }
                } else {
                    sendError(exchange, 404, "Ruta no encontrada");
                }
            } else if ("POST".equals(method)) {
                handleCreateEvaluacion(exchange);
            } else if ("PUT".equals(method)) {
                int id = extractId(path);
                handleUpdateEvaluacion(exchange, id);
            } else if ("DELETE".equals(method)) {
                int id = extractId(path);
                handleDeleteEvaluacion(exchange, id);
            } else {
                sendError(exchange, 405, "Método no permitido");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error interno: " + e.getMessage());
        }
    }

    private void handleGetAllEvaluaciones(HttpExchange exchange) throws IOException {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\": true, \"data\": [");
            
            int totalCount = 0;
            
            // 1. EVALUACIONES COMPLETADAS
            java.util.List<Evaluacion> evaluaciones = evaluacionDAO.listar();
            for (int i = 0; i < evaluaciones.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(evaluaciones.get(i)));
            }
            totalCount = evaluaciones.size();
            
            // 2. EVALUACIONES PENDIENTES (pagos sin evaluación)
            try {
                java.sql.Connection conn = com.spacework.util.Conexion.getConexion();
                String sqlPendientes = 
                    "SELECT p.id_pago, r.id_reserva, r.id_cliente, c.nombre || ' ' || c.apellido as nombre_cliente, " +
                    "       e.nombre, p.monto, p.fecha_pago " +
                    "FROM PAGOS p " +
                    "JOIN RESERVAS r ON p.id_reserva = r.id_reserva " +
                    "JOIN CLIENTES c ON r.id_cliente = c.id_cliente " +
                    "JOIN ESPACIOS e ON r.id_espacio = e.id_espacio " +
                    "WHERE p.estado_pago = 'COMPLETADO' " +
                    "  AND r.id_reserva NOT IN (SELECT id_reserva FROM EVALUACIONES) " +
                    "ORDER BY p.fecha_pago DESC";
                
                java.sql.PreparedStatement ps = conn.prepareStatement(sqlPendientes);
                java.sql.ResultSet rs = ps.executeQuery();
                
                int pendienteCount = 0;
                while (rs.next()) {
                    if (evaluaciones.size() > 0 || pendienteCount > 0) sb.append(",");
                    sb.append("{\"idEvaluacion\": 0, \"idReserva\": ").append(rs.getInt("id_reserva"));
                    sb.append(", \"idCliente\": ").append(rs.getInt("id_cliente"));
                    sb.append(", \"calificacion\": 0, \"comentario\": null");
                    sb.append(", \"fechaEvaluacion\": \"").append(rs.getTimestamp("fecha_pago"));
                    sb.append("\", \"nombreCliente\": \"").append(rs.getString("nombre_cliente"));
                    sb.append("\", \"nombreEspacio\": \"").append(rs.getString("nombre"));
                    sb.append("\", \"estado\": \"PENDIENTE\"}");
                    pendienteCount++;
                }
                totalCount += pendienteCount;
                
                rs.close();
                ps.close();
                conn.close();
            } catch (Exception pe) {
                System.err.println("[Evaluaciones] Error obteniendo pendientes: " + pe.getMessage());
                pe.printStackTrace();
            }
            
            sb.append("], \"count\": ").append(totalCount).append("}");
            sendResponse(exchange, 200, sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Error al listar: " + e.getMessage());
        }
    }

    private void handleGetEvaluacion(HttpExchange exchange, int id) throws IOException {
        try {
            Evaluacion eval = evaluacionDAO.buscarPorId(id);
            if (eval == null) {
                sendError(exchange, 404, "Evaluación no encontrada");
            } else {
                sendResponse(exchange, 200, "{\"success\": true, \"data\": " + toJson(eval) + "}");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleGetPromedioEspacio(HttpExchange exchange, int idEspacio) throws IOException {
        try {
            double promedio = evaluacionController.obtenerPromedioEspacio(idEspacio);
            String estrellas = evaluacionController.obtenerEstrellas((int) Math.round(promedio));
            sendResponse(exchange, 200, "{\"success\": true, \"promedio\": " + promedio + 
                    ", \"estrellas\": \"" + estrellas + "\"}");
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleGetConteoEspacio(HttpExchange exchange, int idEspacio) throws IOException {
        try {
            int conteo = evaluacionController.obtenerConteoEspacio(idEspacio);
            sendResponse(exchange, 200, "{\"success\": true, \"conteo\": " + conteo + "}");
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleCreateEvaluacion(HttpExchange exchange) throws IOException {
        try {
            String body = readBody(exchange);
            int idReserva = extractJsonInt(body, "idReserva");
            int idCliente = extractJsonInt(body, "idCliente");
            int calificacion = extractJsonInt(body, "calificacion");
            String comentario = extractJsonString(body, "comentario");

            if (evaluacionController.registrarEvaluacion(idReserva, idCliente, calificacion, comentario)) {
                sendResponse(exchange, 201, "{\"success\": true, \"message\": \"Evaluación registrada\"}");
            } else {
                sendError(exchange, 400, "Error al registrar");
            }
        } catch (Exception e) {
            sendError(exchange, 400, "Datos inválidos: " + e.getMessage());
        }
    }

    private void handleUpdateEvaluacion(HttpExchange exchange, int id) throws IOException {
        try {
            String body = readBody(exchange);
            int calificacion = extractJsonInt(body, "calificacion");
            String comentario = extractJsonString(body, "comentario");

            if (evaluacionController.actualizarEvaluacion(id, calificacion, comentario)) {
                sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Evaluación actualizada\"}");
            } else {
                sendError(exchange, 400, "Error al actualizar");
            }
        } catch (Exception e) {
            sendError(exchange, 400, "Error: " + e.getMessage());
        }
    }

    private void handleDeleteEvaluacion(HttpExchange exchange, int id) throws IOException {
        try {
            if (evaluacionController.eliminarEvaluacion(id)) {
                sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Evaluación eliminada\"}");
            } else {
                sendError(exchange, 400, "Error al eliminar");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private String toJson(Evaluacion eval) {
        return "{\"idEvaluacion\": " + eval.getIdEvaluacion() + ", \"idReserva\": " + eval.getIdReserva() +
                ", \"idCliente\": " + eval.getIdCliente() + ", \"calificacion\": " + eval.getCalificacion() +
                ", \"comentario\": \"" + (eval.getComentario() != null ? eval.getComentario() : "") +
                "\", \"fechaEvaluacion\": \"" + eval.getFechaEvaluacion() + "\"}";
    }

    protected int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    protected String readBody(HttpExchange exchange) throws IOException {
        byte[] buffer = new byte[2048];
        int len = exchange.getRequestBody().read(buffer);
        return new String(buffer, 0, len, StandardCharsets.UTF_8);
    }

    protected void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    protected void sendError(HttpExchange exchange, int code, String message) throws IOException {
        String response = "{\"success\": false, \"error\": \"" + message + "\"}";
        sendResponse(exchange, code, response);
    }

    protected int extractJsonInt(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*(\\d+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        throw new IllegalArgumentException("Campo no encontrado: " + key);
    }

    protected String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*?)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }
}
