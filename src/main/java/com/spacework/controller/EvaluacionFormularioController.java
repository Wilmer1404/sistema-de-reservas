package com.spacework.controller;

import com.spacework.dao.EvaluacionDAO;
import com.spacework.dao.PagoDAO;
import com.spacework.dao.ReservaDAO;
import com.spacework.dao.TokenEvaluacionDAO;
import com.spacework.model.Evaluacion;
import com.spacework.model.Pago;
import com.spacework.model.Reserva;
import com.spacework.model.TokenEvaluacion;
import com.spacework.util.Conexion;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Controller
@RequestMapping("/evaluaciones/formulario")
public class EvaluacionFormularioController {

    private final TokenEvaluacionDAO tokenDAO = new TokenEvaluacionDAO();
    private final EvaluacionDAO evaluacionDAO = new EvaluacionDAO();
    private final PagoDAO pagoDAO = new PagoDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();

    @GetMapping
    @ResponseBody
    public String formulario(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String calificacion,
            @RequestParam(required = false) String comentario) {

        if (comentario != null && !comentario.isEmpty()) {
            try { comentario = URLDecoder.decode(comentario, "UTF-8"); } catch (UnsupportedEncodingException e) { comentario = ""; }
        }

        if (token == null || token.isEmpty())
            return paginaError("Enlace inválido", "El enlace de evaluación no es válido o está incompleto.");

        try {
            TokenEvaluacion tokenEval = tokenDAO.buscarPorToken(token);
            if (tokenEval == null) return paginaError("Enlace no encontrado", "Este enlace de evaluación no existe.");
            if (tokenEval.getUtilizado() == 1) return paginaError("Enlace ya utilizado", "Ya enviaste tu evaluación anteriormente. ¡Gracias por tu opinión!");
            if (tokenEval.estaExpirado()) return paginaError("Enlace expirado", "Este enlace de evaluación ya expiró (válido por 30 días).");

            if (calificacion == null || calificacion.isEmpty())
                return paginaSeleccionEstrellas(token);

            int cal = Integer.parseInt(calificacion);
            if (cal < 1 || cal > 5) return paginaError("Calificación inválida", "La calificación debe ser entre 1 y 5.");

            Pago pago = pagoDAO.buscarPorId(tokenEval.getIdPago());
            if (pago == null) return paginaError("Error interno", "No se encontró el pago asociado.");

            int idReserva = pago.getIdReserva();
            Reserva reserva = null;
            try { reserva = reservaDAO.buscarPorId(idReserva); } catch (Exception ignored) {}

            int idCliente = (reserva != null && reserva.getCliente() != null) ? reserva.getCliente().getIdCliente() : 0;
            if (idCliente <= 0) {
                java.sql.Connection conn = null;
                try {
                    conn = Conexion.getConexion();
                    java.sql.PreparedStatement ps = conn.prepareStatement("SELECT id_cliente FROM RESERVAS WHERE id_reserva = ?");
                    ps.setInt(1, idReserva);
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next()) idCliente = rs.getInt("id_cliente");
                } catch (Exception ignored) {} finally { Conexion.cerrar(conn); }
            }

            if (idCliente <= 0) return paginaError("Error de datos", "No se pudo identificar al cliente de la reserva.");

            if (comentario == null || comentario.trim().isEmpty())
                comentario = "Evaluación recibida por email (sin comentario adicional)";

            Evaluacion ev = new Evaluacion();
            ev.setIdReserva(idReserva);
            ev.setIdCliente(idCliente);
            ev.setCalificacion(cal);
            ev.setComentario(comentario);

            boolean guardado = evaluacionDAO.insertar(ev);
            if (guardado) {
                tokenDAO.marcarUtilizado(tokenEval.getIdToken());
                return paginaExito(cal);
            }
            return paginaError("Error al guardar", "Hubo un problema al registrar tu evaluación. Intenta nuevamente.");

        } catch (Exception e) {
            return paginaError("Error interno", "Ocurrió un error inesperado: " + e.getMessage());
        }
    }

    private String paginaSeleccionEstrellas(String token) {
        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>"
            + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
            + "<title>Califica tu experiencia - SpaceWork</title>"
            + "<style>body{margin:0;font-family:Arial,sans-serif;background:#f0f4f8;"
            + "display:flex;justify-content:center;align-items:center;min-height:100vh;padding:20px;}"
            + ".card{background:#fff;border-radius:12px;padding:40px;max-width:500px;width:100%;"
            + "box-shadow:0 4px 20px rgba(0,0,0,0.12);}"
            + "h1{color:#1a73e8;margin:0 0 8px;font-size:28px;text-align:center;}"
            + "p{color:#555;font-size:15px;}"
            + ".stars{display:flex;justify-content:center;gap:12px;margin:20px 0 10px;}"
            + ".star{font-size:48px;cursor:pointer;transition:transform .2s,color .2s;color:#ddd;}"
            + ".star:hover{transform:scale(1.15);color:#ffc107;}"
            + "textarea{width:100%;padding:12px;border:1px solid #ddd;border-radius:6px;font-family:Arial,sans-serif;"
            + "font-size:14px;resize:vertical;min-height:100px;box-sizing:border-box;}"
            + "button{width:100%;padding:14px;border:none;border-radius:6px;background:#1a73e8;color:white;"
            + "font-weight:bold;font-size:16px;cursor:pointer;margin-top:15px;}"
            + "button:disabled{background:#ccc;cursor:not-allowed;}"
            + "</style></head><body>"
            + "<div class='card'><h1>SpaceWork</h1>"
            + "<p style='text-align:center'><strong>¿Cómo calificarías el nivel de atención recibido?</strong></p>"
            + "<div class='stars'>"
            + "<span class='star' data-rating='1'>⭐</span><span class='star' data-rating='2'>⭐</span>"
            + "<span class='star' data-rating='3'>⭐</span><span class='star' data-rating='4'>⭐</span>"
            + "<span class='star' data-rating='5'>⭐</span></div>"
            + "<label>Comentario (opcional):</label>"
            + "<textarea id='comentario' maxlength='500'></textarea>"
            + "<button id='btn' disabled onclick='enviar()'>Enviar Evaluación</button>"
            + "<p style='text-align:center;color:#aaa;font-size:11px;margin-top:20px;'>© 2026 SpaceWork Perú S.A.C.</p>"
            + "</div>"
            + "<script>"
            + "let selectedRating=0;"
            + "document.querySelectorAll('.star').forEach(s=>{"
            + "s.addEventListener('click',function(){selectedRating=this.dataset.rating;"
            + "document.querySelectorAll('.star').forEach(x=>x.style.color=x.dataset.rating<=selectedRating?'#ffc107':'#ddd');"
            + "document.getElementById('btn').disabled=false;});});"
            + "function enviar(){"
            + "if(!selectedRating)return;"
            + "const c=document.getElementById('comentario').value;"
            + "window.location.href='/evaluaciones/formulario?token=" + token + "&calificacion='+selectedRating+(c?'&comentario='+encodeURIComponent(c):'');}"
            + "</script></body></html>";
    }

    private String paginaExito(int calificacion) {
        String estrellas = "";
        for (int i = 0; i < calificacion; i++) estrellas += "⭐";
        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>"
            + "<title>¡Gracias! - SpaceWork</title>"
            + "<style>body{margin:0;font-family:Arial,sans-serif;background:#f0f4f8;"
            + "display:flex;justify-content:center;align-items:center;min-height:100vh;}"
            + ".card{background:#fff;border-radius:12px;padding:40px;max-width:480px;width:90%;"
            + "box-shadow:0 4px 20px rgba(0,0,0,0.12);text-align:center;}"
            + "h1{color:#27ae60;font-size:26px;} .stars{font-size:36px;margin:16px 0;}"
            + "p{color:#555;font-size:15px;} .footer{color:#aaa;font-size:11px;margin-top:30px;}"
            + "</style></head><body>"
            + "<div class='card'><div style='font-size:64px'>✅</div>"
            + "<h1>¡Gracias por tu evaluación!</h1>"
            + "<div class='stars'>" + estrellas + "</div>"
            + "<p>Tu calificación de <strong>" + calificacion + "/5</strong> ha sido registrada.</p>"
            + "<p class='footer'>© 2026 SpaceWork Perú S.A.C.</p>"
            + "</div></body></html>";
    }

    private String paginaError(String titulo, String mensaje) {
        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>"
            + "<title>" + titulo + " - SpaceWork</title>"
            + "<style>body{margin:0;font-family:Arial,sans-serif;background:#f0f4f8;"
            + "display:flex;justify-content:center;align-items:center;min-height:100vh;}"
            + ".card{background:#fff;border-radius:12px;padding:40px;max-width:480px;width:90%;"
            + "box-shadow:0 4px 20px rgba(0,0,0,0.12);text-align:center;}"
            + "h1{color:#e74c3c;font-size:22px;} p{color:#555;font-size:15px;}"
            + ".footer{color:#aaa;font-size:11px;margin-top:30px;}"
            + "</style></head><body>"
            + "<div class='card'><div style='font-size:64px'>⚠️</div>"
            + "<h1>" + titulo + "</h1><p>" + mensaje + "</p>"
            + "<p class='footer'>© 2026 SpaceWork Perú S.A.C.</p>"
            + "</div></body></html>";
    }
}
