package com.spacework.controller.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.spacework.dao.TokenEvaluacionDAO;
import com.spacework.dao.EvaluacionDAO;
import com.spacework.dao.PagoDAO;
import com.spacework.dao.ReservaDAO;
import com.spacework.model.TokenEvaluacion;
import com.spacework.model.Evaluacion;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Handler para recibir evaluaciones desde el link del email
 * GET /evaluaciones/formulario?token=xxx&calificacion=5
 * 
 * - Valida el token
 * - Registra la evaluación en BD
 * - Muestra página de agradecimiento
 */
public class EvaluacionFormularioHandler implements HttpHandler {

    private final TokenEvaluacionDAO tokenDAO = new TokenEvaluacionDAO();
    private final EvaluacionDAO evaluacionDAO = new EvaluacionDAO();
    private final PagoDAO pagoDAO = new PagoDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        String query = exchange.getRequestURI().getQuery(); // token=xxx&calificacion=5&comentario=...
        String token = extraerParam(query, "token");
        String calificacionStr = extraerParam(query, "calificacion");
        String comentario = extraerParam(query, "comentario");
        
        // Decodificar URL encoding del comentario
        if (comentario != null && !comentario.isEmpty()) {
            try {
                comentario = java.net.URLDecoder.decode(comentario, "UTF-8");
            } catch (Exception e) {
                comentario = "";
            }
        }

        String html;

        if (token == null || token.isEmpty()) {
            html = paginaError("Enlace inválido", "El enlace de evaluación no es válido o está incompleto.");
            enviarRespuesta(exchange, 400, html);
            return;
        }

        try {
            // Validar token
            TokenEvaluacion tokenEval = tokenDAO.buscarPorToken(token);

            if (tokenEval == null) {
                html = paginaError("Enlace no encontrado", "Este enlace de evaluación no existe.");
                enviarRespuesta(exchange, 404, html);
                return;
            }

            if (tokenEval.getUtilizado() == 1) {
                html = paginaError("Enlace ya utilizado",
                        "Ya enviaste tu evaluación anteriormente. ¡Gracias por tu opinión!");
                enviarRespuesta(exchange, 200, html);
                return;
            }

            if (tokenEval.estaExpirado()) {
                html = paginaError("Enlace expirado",
                        "Este enlace de evaluación ya expiró (válido por 30 días).");
                enviarRespuesta(exchange, 200, html);
                return;
            }

            // Si no viene calificacion, mostrar formulario de selección
            if (calificacionStr == null || calificacionStr.isEmpty()) {
                html = paginaSeleccionEstrellas(token);
                enviarRespuesta(exchange, 200, html);
                return;
            }

            // Registrar evaluación
            int calificacion = Integer.parseInt(calificacionStr);
            if (calificacion < 1 || calificacion > 5) {
                html = paginaError("Calificación inválida", "La calificación debe ser entre 1 y 5.");
                enviarRespuesta(exchange, 400, html);
                return;
            }

            // Obtener id_reserva e id_cliente via token → pago → reserva
            int idPago = tokenEval.getIdPago();
            com.spacework.model.Pago pago = pagoDAO.buscarPorId(idPago);

            if (pago == null) {
                html = paginaError("Error interno", "No se encontró el pago asociado.");
                enviarRespuesta(exchange, 500, html);
                return;
            }

            // Obtener reserva para id_cliente
            int idReserva = pago.getIdReserva();
            com.spacework.model.Reserva reserva = null;
            try {
                reserva = reservaDAO.buscarPorId(idReserva);
            } catch (Exception e) {
                // Si ReservaDAO no tiene buscarPorId, manejar el error
            }

            int idCliente = (reserva != null && reserva.getCliente() != null)
                    ? reserva.getCliente().getIdCliente() : 0;

            // Fallback defensivo: obtener id_cliente directo de RESERVAS si no vino mapeado
            if (idCliente <= 0) {
                java.sql.Connection conn = null;
                try {
                    conn = com.spacework.util.Conexion.getConexion();
                    java.sql.PreparedStatement ps = conn.prepareStatement(
                            "SELECT id_cliente FROM RESERVAS WHERE id_reserva = ?");
                    ps.setInt(1, idReserva);
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        idCliente = rs.getInt("id_cliente");
                    }
                    rs.close();
                    ps.close();
                    com.spacework.util.Conexion.cerrar(conn);
                } catch (Exception ignored) {
                    if (conn != null) {
                        try { com.spacework.util.Conexion.cerrar(conn); } catch (Exception ex) { }
                    }
                }
            }

            if (idCliente <= 0) {
                html = paginaError("Error de datos",
                        "No se pudo identificar al cliente de la reserva. Solicita un nuevo enlace de evaluación.");
                enviarRespuesta(exchange, 400, html);
                return;
            }

            // Usar comentario enviado, o valor por defecto si está vacío
            if (comentario == null || comentario.trim().isEmpty()) {
                comentario = "Evaluación recibida por email (sin comentario adicional)";
            }

            // Insertar en EVALUACIONES
            Evaluacion ev = new Evaluacion();
            ev.setIdReserva(idReserva);
            ev.setIdCliente(idCliente);
            ev.setCalificacion(calificacion);
            ev.setComentario(comentario);

            boolean guardado = evaluacionDAO.insertar(ev);

            if (guardado) {
                // Marcar token como utilizado
                tokenDAO.marcarUtilizado(tokenEval.getIdToken());
                System.out.println("[EvaluacionFormulario] Evaluación registrada: " 
                        + calificacion + " estrellas para reserva #" + idReserva 
                        + " | Comentario: " + comentario);
                html = paginaExito(calificacion);
            } else {
                html = paginaError("Error al guardar",
                        "Hubo un problema al registrar tu evaluación. Intenta nuevamente.");
            }

            enviarRespuesta(exchange, 200, html);

        } catch (Exception e) {
            String detalle = (e.getMessage() == null || e.getMessage().trim().isEmpty())
                    ? "sin detalle técnico"
                    : e.getMessage();
            System.err.println("[EvaluacionFormulario] Error: " + detalle);
            html = paginaError("Error interno", "Ocurrió un error inesperado: " + detalle);
            enviarRespuesta(exchange, 500, html);
        }
    }

    // ── Páginas HTML ──────────────────────────────────────────────────────────

    private String paginaSeleccionEstrellas(String token) {
        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
                "<title>Califica tu experiencia - SpaceWork</title>" +
                "<style>" +
                "body{margin:0;font-family:Arial,sans-serif;background:#f0f4f8;" +
                "display:flex;justify-content:center;align-items:center;min-height:100vh;padding:20px;}" +
                ".card{background:#fff;border-radius:12px;padding:40px;max-width:500px;width:100%;" +
                "box-shadow:0 4px 20px rgba(0,0,0,0.12);}" +
                ".header{text-align:center;margin-bottom:30px;}" +
                "h1{color:#1a73e8;margin:0 0 8px;font-size:28px;}" +
                ".subtitle{color:#999;font-size:14px;}" +
                "p{color:#555;font-size:15px;line-height:1.6;}" +
                ".stars-section{text-align:center;margin:30px 0;}" +
                ".stars{display:flex;justify-content:center;gap:12px;margin:20px 0 10px;}" +
                ".star{font-size:48px;cursor:pointer;transition:transform .2s,color .2s;color:#ddd;}" +
                ".star:hover{transform:scale(1.15);color:#ffc107;}" +
                ".star.selected{color:#ffc107;}" +
                ".labels{display:flex;justify-content:center;gap:32px;color:#888;font-size:12px;}" +
                ".form-group{margin:25px 0;}" +
                "label{display:block;color:#333;font-weight:bold;margin-bottom:8px;font-size:14px;}" +
                "textarea{width:100%;padding:12px;border:1px solid #ddd;border-radius:6px;font-family:Arial,sans-serif;" +
                "font-size:14px;resize:vertical;min-height:100px;max-height:300px;box-sizing:border-box;" +
                "color:#333;}" +
                "textarea:focus{outline:none;border-color:#1a73e8;box-shadow:0 0 4px rgba(26,115,232,0.3);}" +
                ".char-count{font-size:12px;color:#999;margin-top:4px;text-align:right;}" +
                ".button-group{display:flex;gap:12px;margin-top:25px;}" +
                "button{flex:1;padding:14px;border:none;border-radius:6px;font-weight:bold;font-size:16px;cursor:pointer;transition:all .2s;}" +
                ".submit-btn{background:#1a73e8;color:white;}" +
                ".submit-btn:hover:not(:disabled){background:#1557b0;}" +
                ".submit-btn:disabled{background:#ccc;cursor:not-allowed;}" +
                ".reset-btn{background:#f0f0f0;color:#333;border:1px solid #ddd;}" +
                ".reset-btn:hover{background:#e8e8e8;}" +
                ".note{font-size:12px;color:#999;margin-top:20px;text-align:center;}" +
                ".rating-display{margin-top:5px;font-size:14px;color:#666;}" +
                "</style></head><body>" +
                "<div class='card'>" +
                "<div class='header'>" +
                "<h1>SpaceWork</h1>" +
                "<p class='subtitle'>Sistema de Reservas</p>" +
                "</div>" +
                "<div class='stars-section'>" +
                "<p style='font-size:16px;color:#333;margin-bottom:15px;'><strong>¿Cómo calificarías el nivel de atención recibido?</strong></p>" +
                "<div class='stars'>" +
                "<span class='star' data-rating='1' title='1 - Muy malo'>⭐</span>" +
                "<span class='star' data-rating='2' title='2 - Malo'>⭐</span>" +
                "<span class='star' data-rating='3' title='3 - Regular'>⭐</span>" +
                "<span class='star' data-rating='4' title='4 - Bueno'>⭐</span>" +
                "<span class='star' data-rating='5' title='5 - Excelente'>⭐</span>" +
                "</div>" +
                "<div class='labels'><span>1</span><span>2</span><span>3</span><span>4</span><span>5</span></div>" +
                "<div class='rating-display' id='ratingDisplay'></div>" +
                "</div>" +
                "<form id='evaluacionForm' onsubmit='return submitEvaluacion(\"" + token + "\")'>" +
                "<div class='form-group'>" +
                "<label for='comentario'>Comentario (opcional - máximo 500 caracteres):</label>" +
                "<textarea id='comentario' name='comentario' placeholder='Cuéntanos tu experiencia...' maxlength='500'></textarea>" +
                "<div class='char-count'><span id='charCount'>0</span>/500</div>" +
                "</div>" +
                "<div class='button-group'>" +
                "<button type='submit' class='submit-btn' id='submitBtn' disabled>Enviar Evaluación</button>" +
                "<button type='reset' class='reset-btn'>Limpiar</button>" +
                "</div>" +
                "</form>" +
                "<p class='note'>© 2026 SpaceWork Perú S.A.C.</p>" +
                "</div>" +
                "<script>" +
                "let selectedRating = 0;" +
                "const stars = document.querySelectorAll('.star');" +
                "const submitBtn = document.getElementById('submitBtn');" +
                "const ratingDisplay = document.getElementById('ratingDisplay');" +
                "const comentarioField = document.getElementById('comentario');" +
                "const charCount = document.getElementById('charCount');" +
                "" +
                "stars.forEach(star => {" +
                "  star.addEventListener('click', function() {" +
                "    selectedRating = this.dataset.rating;" +
                "    updateStars();" +
                "    updateSubmitBtn();" +
                "  });" +
                "  star.addEventListener('mouseover', function() {" +
                "    const rating = this.dataset.rating;" +
                "    document.querySelectorAll('.star').forEach(s => {" +
                "      s.style.color = s.dataset.rating <= rating ? '#ffc107' : '#ddd';" +
                "    });" +
                "  });" +
                "});" +
                "" +
                "document.querySelector('.stars-section').addEventListener('mouseleave', updateStars);" +
                "" +
                "function updateStars() {" +
                "  stars.forEach(star => {" +
                "    if (star.dataset.rating <= selectedRating) {" +
                "      star.classList.add('selected');" +
                "      star.style.color = '#ffc107';" +
                "    } else {" +
                "      star.classList.remove('selected');" +
                "      star.style.color = '#ddd';" +
                "    }" +
                "  });" +
                "  " +
                "  if (selectedRating > 0) {" +
                "    const labels = ['', 'Muy malo', 'Malo', 'Regular', 'Bueno', 'Excelente'];" +
                "    ratingDisplay.textContent = selectedRating + '/5 - ' + labels[selectedRating];" +
                "  } else {" +
                "    ratingDisplay.textContent = '';" +
                "  }" +
                "}" +
                "" +
                "function updateSubmitBtn() {" +
                "  submitBtn.disabled = selectedRating === 0;" +
                "}" +
                "" +
                "comentarioField.addEventListener('input', function() {" +
                "  charCount.textContent = this.value.length;" +
                "});" +
                "" +
                "function submitEvaluacion(token) {" +
                "  if (selectedRating === 0) {" +
                "    alert('Por favor selecciona una calificación');" +
                "    return false;" +
                "  }" +
                "  " +
                "  const comentario = document.getElementById('comentario').value;" +
                "  const url = '/evaluaciones/formulario?token=' + token + '&calificacion=' + selectedRating + " +
                "              (comentario ? '&comentario=' + encodeURIComponent(comentario) : '');" +
                "  window.location.href = url;" +
                "  return false;" +
                "}" +
                "</script>" +
                "</body></html>";
    }

    private String paginaExito(int calificacion) {
        String estrellas = "";
        for (int i = 0; i < calificacion; i++) estrellas += "⭐";
        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
                "<title>¡Gracias! - SpaceWork</title>" +
                "<style>" +
                "body{margin:0;font-family:Arial,sans-serif;background:#f0f4f8;" +
                "display:flex;justify-content:center;align-items:center;min-height:100vh;}" +
                ".card{background:#fff;border-radius:12px;padding:40px;max-width:480px;width:90%;" +
                "box-shadow:0 4px 20px rgba(0,0,0,0.12);text-align:center;}" +
                ".check{font-size:64px;margin-bottom:16px;}" +
                "h1{color:#27ae60;font-size:26px;margin-bottom:8px;}" +
                "p{color:#555;font-size:15px;}" +
                ".stars{font-size:36px;margin:16px 0;}" +
                ".footer{color:#aaa;font-size:11px;margin-top:30px;}" +
                "</style></head><body>" +
                "<div class='card'>" +
                "<div class='check'>✅</div>" +
                "<h1>¡Gracias por tu evaluación!</h1>" +
                "<div class='stars'>" + estrellas + "</div>" +
                "<p>Tu calificación de <strong>" + calificacion + "/5</strong> ha sido registrada exitosamente.</p>" +
                "<p>Tu opinión nos ayuda a mejorar nuestros servicios.</p>" +
                "<p class='footer'>© 2026 SpaceWork Perú S.A.C.</p>" +
                "</div></body></html>";
    }

    private String paginaError(String titulo, String mensaje) {
        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
                "<title>" + titulo + " - SpaceWork</title>" +
                "<style>" +
                "body{margin:0;font-family:Arial,sans-serif;background:#f0f4f8;" +
                "display:flex;justify-content:center;align-items:center;min-height:100vh;}" +
                ".card{background:#fff;border-radius:12px;padding:40px;max-width:480px;width:90%;" +
                "box-shadow:0 4px 20px rgba(0,0,0,0.12);text-align:center;}" +
                ".icon{font-size:64px;margin-bottom:16px;}" +
                "h1{color:#e74c3c;font-size:22px;margin-bottom:8px;}" +
                "p{color:#555;font-size:15px;}" +
                ".footer{color:#aaa;font-size:11px;margin-top:30px;}" +
                "</style></head><body>" +
                "<div class='card'>" +
                "<div class='icon'>⚠️</div>" +
                "<h1>" + titulo + "</h1>" +
                "<p>" + mensaje + "</p>" +
                "<p class='footer'>© 2026 SpaceWork Perú S.A.C.</p>" +
                "</div></body></html>";
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private String extraerParam(String query, String param) {
        if (query == null) return null;
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(param)) {
                return kv[1];
            }
        }
        return null;
    }

    private void enviarRespuesta(HttpExchange exchange, int code, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
