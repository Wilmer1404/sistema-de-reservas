package com.spacework.util;

/**
 * Utilidad para enviar correos electrónicos de confirmación de pago y solicitud de evaluación.
 * Delega en MailService, que lee credenciales desde mail.properties.
 */
public class EmailUtil {

    /**
     * Envía correo de bienvenida con credenciales al cliente
     */
    public static void enviarCredencialesCliente(String para, String nombreCliente, String passwordPlano) {
        String asunto = "Bienvenido a SpaceWork - Acceso a tu cuenta";
        String html = buildHtmlCredenciales(nombreCliente, passwordPlano);
        boolean enviado = MailService.enviarCorreo(para, asunto, html);
        if (!enviado) {
            System.out.println("⚠️  No se pudo enviar credenciales a: " + para);
        }
    }

    public static String buildHtmlCredenciales(String nombre, String passwordPlano) {
        return "<!DOCTYPE html>"
            + "<html><head><meta charset='utf-8'></head>"
            + "<body style='font-family:Arial,sans-serif;background:#f4f6f9;padding:20px;margin:0'>"
            + "<div style='max-width:520px;margin:auto;background:white;border-radius:10px;padding:30px;box-shadow:0 4px 12px rgba(0,0,0,0.1)'>"
            + "<div style='text-align:center;margin-bottom:20px'>"
            + "<h1 style='color:#2c7be5;margin:0;font-size:1.8em'>SpaceWork</h1>"
            + "<p style='color:#6c757d;margin:4px 0 0'>Sistema de Reservas</p>"
            + "</div>"
            + "<hr style='border:none;border-top:1px solid #dee2e6;margin:20px 0'>"
            + "<h2 style='color:#6366f1;margin-top:0'>¡Bienvenido/a, " + nombre + "!</h2>"
            + "<p style='color:#495057'>Tu cuenta ha sido creada exitosamente. Ya puedes acceder al sistema con tu correo y la siguiente contraseña:</p>"
            + "<div style='background:#eef2ff;border:1px solid #6366f1;border-radius:6px;padding:18px 0;margin:22px 0;text-align:center'>"
            + "<span style='font-size:1.3em;color:#2c7be5;font-weight:bold;letter-spacing:2px'>" + passwordPlano + "</span>"
            + "</div>"
            + "<p style='color:#495057;font-size:0.97em'>Por seguridad, te recomendamos cambiar tu contraseña después de iniciar sesión.</p>"
            + "<hr style='border:none;border-top:1px solid #dee2e6;margin:20px 0'>"
            + "<p style='color:#adb5bd;font-size:0.8em;text-align:center;margin:0'>Este es un mensaje automático de SpaceWork · No responda este correo</p>"
            + "</div></body></html>";
    }

    /**
     * Envía confirmación de pago al cliente
     */
    public static void enviarConfirmacionPago(String para, String nombreCliente, int idReserva, double montoAPagar, String metodoPago) {
        String asunto = "Confirmación de Pago - Reserva #" + idReserva;
        String html = buildHtmlConfirmacionPago(nombreCliente, idReserva, montoAPagar, metodoPago);
        boolean enviado = MailService.enviarCorreo(para, asunto, html);
        if (!enviado) {
            System.out.println("⚠️  No se pudo enviar confirmación de pago a: " + para);
        }
    }

    public static String buildHtmlConfirmacionPago(String nombreCliente, int idReserva, double montoAPagar, String metodoPago) {
        return "<!DOCTYPE html>"
            + "<html><head><meta charset='utf-8'></head>"
            + "<body style='font-family:Arial,sans-serif;background:#f4f6f9;padding:20px;margin:0'>"
            + "<div style='max-width:520px;margin:auto;background:white;border-radius:10px;padding:30px;box-shadow:0 4px 12px rgba(0,0,0,0.1)'>"
            + "<div style='text-align:center;margin-bottom:20px'>"
            + "<h1 style='color:#27ae60;margin:0;font-size:1.8em'>✓ Pago Confirmado</h1>"
            + "<p style='color:#6c757d;margin:4px 0 0'>SpaceWork</p>"
            + "</div>"
            + "<hr style='border:none;border-top:1px solid #dee2e6;margin:20px 0'>"
            + "<h2 style='color:#27ae60;margin-top:0'>Gracias por tu pago, " + nombreCliente + "</h2>"
            + "<div style='background:#f0f9f7;border-left:4px solid #27ae60;padding:12px 15px;margin:15px 0'>"
            + "<p style='color:#495057;margin:5px 0'><strong>ID Reserva:</strong> #" + idReserva + "</p>"
            + "<p style='color:#495057;margin:5px 0'><strong>Monto:</strong> S/. " + String.format("%.2f", montoAPagar) + "</p>"
            + "<p style='color:#495057;margin:5px 0'><strong>Método:</strong> " + metodoPago + "</p>"
            + "</div>"
            + "<p style='color:#495057;font-size:0.97em'>Tu reserva ha sido confirmada y está lista. Pronto recibirás un formulario de evaluación.</p>"
            + "<hr style='border:none;border-top:1px solid #dee2e6;margin:20px 0'>"
            + "<p style='color:#adb5bd;font-size:0.8em;text-align:center;margin:0'>Este es un mensaje automático de SpaceWork · No responda este correo</p>"
            + "</div></body></html>";
    }

    /**
     * Envía formulario de evaluación al cliente - retorna true si se envió correctamente
     */
    public static boolean enviarFormularioEvaluacion(String para, String nombreCliente, int idReserva, String token, String baseUrl) {
        String asunto = "Formulario de Evaluación - Reserva #" + idReserva;
        String html = buildHtmlFormularioEvaluacion(nombreCliente, idReserva, token, baseUrl);
        boolean enviado = MailService.enviarCorreo(para, asunto, html);
        if (!enviado) {
            System.out.println("⚠️  No se pudo enviar formulario de evaluación a: " + para);
        }
        return enviado;
    }

    public static String buildHtmlFormularioEvaluacion(String nombreCliente, int idReserva, String token, String baseUrl) {
        String enlace = baseUrl + "/evaluacion.html?token=" + token;
        return "<!DOCTYPE html>"
            + "<html><head><meta charset='utf-8'></head>"
            + "<body style='font-family:Arial,sans-serif;background:#f4f6f9;padding:20px;margin:0'>"
            + "<div style='max-width:520px;margin:auto;background:white;border-radius:10px;padding:30px;box-shadow:0 4px 12px rgba(0,0,0,0.1)'>"
            + "<div style='text-align:center;margin-bottom:20px'>"
            + "<h1 style='color:#f39c12;margin:0;font-size:1.8em'>⭐ Tu opinión importa</h1>"
            + "<p style='color:#6c757d;margin:4px 0 0'>SpaceWork</p>"
            + "</div>"
            + "<hr style='border:none;border-top:1px solid #dee2e6;margin:20px 0'>"
            + "<h2 style='color:#f39c12;margin-top:0'>¡Ayúdanos a mejorar, " + nombreCliente + "!</h2>"
            + "<p style='color:#495057'>Queremos conocer tu experiencia con nuestro servicio. Por favor, completa el siguiente formulario para la Reserva #" + idReserva + "</p>"
            + "<div style='text-align:center;margin:25px 0'>"
            + "<a href='" + enlace + "' style='display:inline-block;background:#f39c12;color:white;padding:12px 30px;text-decoration:none;border-radius:6px;font-weight:bold'>Completar Evaluación</a>"
            + "</div>"
            + "<p style='color:#6c757d;font-size:0.85em'>O copia y pega este enlace en tu navegador:</p>"
            + "<p style='background:#f8f9fa;padding:10px;border-radius:4px;font-size:0.8em;word-break:break-all;color:#495057'>" + enlace + "</p>"
            + "<hr style='border:none;border-top:1px solid #dee2e6;margin:20px 0'>"
            + "<p style='color:#adb5bd;font-size:0.8em;text-align:center;margin:0'>Este es un mensaje automático de SpaceWork · No responda este correo</p>"
            + "</div></body></html>";
    }

}
