package com.spacework.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MailService {

    private static Properties mailProps;
    private static Session session;

    static {
        cargarConfiguracion();
    }

    private static void cargarConfiguracion() {
        mailProps = new Properties();
        try (InputStream input = MailService.class.getClassLoader()
                .getResourceAsStream("mail.properties")) {
            if (input != null) {
                mailProps.load(input);
            }
        } catch (IOException ignored) {}

        final String user = mailProps.getProperty("smtp.user");
        final String pass = mailProps.getProperty("smtp.password").replace(" ", "");

        Properties props = new Properties();
        props.put("mail.smtp.host",               "smtp.gmail.com");
        props.put("mail.smtp.port",               "587");
        props.put("mail.smtp.auth",               "true");
        props.put("mail.smtp.starttls.enable",    "true");
        props.put("mail.smtp.starttls.required",  "true");
        props.put("mail.smtp.connectiontimeout",  "5000");
        props.put("mail.smtp.timeout",            "5000");

        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

    }

    public static boolean enviarCorreo(String destinatario, String asunto, String cuerpoHtml) {
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(mailProps.getProperty("smtp.from")));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            msg.setSubject(asunto, "UTF-8");
            msg.setContent(cuerpoHtml, "text/html; charset=UTF-8");
            Transport.send(msg);
            return true;
        } catch (MessagingException e) {
            return false;
        }
    }

    public static boolean enviarTokenEvaluacion(String emailCliente, String nombreCliente,
                                                 String nombreEspacio, String fechaReserva,
                                                 String token) {
        String url = "http://localhost:8080/evaluaciones/formulario?token=" + token;

        String html =
            "<html><head><meta charset='UTF-8'></head>" +
            "<body style='margin:0;padding:0;background:#f0f4f8;font-family:Arial,sans-serif;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f0f4f8;padding:30px 0;'>" +
            "<tr><td align='center'>" +
            "<table width='560' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:10px;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.1);'>" +

            // HEADER
            "<tr><td style='background:#1a73e8;padding:30px;text-align:center;'>" +
            "<h1 style='color:#fff;margin:0;font-size:24px;'>SpaceWork Perú</h1>" +
            "<p style='color:#c5d9f8;margin:6px 0 0;font-size:14px;'>Sistema de Reservas</p>" +
            "</td></tr>" +

            // BODY
            "<tr><td style='padding:35px 40px;'>" +
            "<p style='color:#333;font-size:16px;'>Hola <strong>" + nombreCliente + "</strong>,</p>" +
            "<p style='color:#555;font-size:15px;line-height:1.6;'>Tu pago fue procesado exitosamente. " +
            "¿Podrías dedicar un momento para calificar tu experiencia?</p>" +

            "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f8f9fa;border-left:4px solid #1a73e8;border-radius:4px;padding:15px 20px;margin:20px 0;'>" +
            "<tr><td><p style='margin:4px 0;color:#555;font-size:14px;'><b>📍 Espacio:</b> " + nombreEspacio + "</p>" +
            "<p style='margin:4px 0;color:#555;font-size:14px;'><b>📅 Fecha:</b> " + fechaReserva + "</p></td></tr>" +
            "</table>" +

            // STARS FORM
            "<p style='color:#333;font-size:15px;font-weight:bold;margin:25px 0 10px;'>¿Cómo fue el nivel de atención?</p>" +
            "<table cellpadding='0' cellspacing='0' style='margin:0 auto 25px;'><tr>" +
            "<td style='padding:0 6px;'><a href='" + url + "&calificacion=1' style='text-decoration:none;font-size:36px;'>⭐</a></td>" +
            "<td style='padding:0 6px;'><a href='" + url + "&calificacion=2' style='text-decoration:none;font-size:36px;'>⭐</a></td>" +
            "<td style='padding:0 6px;'><a href='" + url + "&calificacion=3' style='text-decoration:none;font-size:36px;'>⭐</a></td>" +
            "<td style='padding:0 6px;'><a href='" + url + "&calificacion=4' style='text-decoration:none;font-size:36px;'>⭐</a></td>" +
            "<td style='padding:0 6px;'><a href='" + url + "&calificacion=5' style='text-decoration:none;font-size:36px;'>⭐</a></td>" +
            "</tr><tr>" +
            "<td style='text-align:center;color:#888;font-size:12px;'>1</td>" +
            "<td style='text-align:center;color:#888;font-size:12px;'>2</td>" +
            "<td style='text-align:center;color:#888;font-size:12px;'>3</td>" +
            "<td style='text-align:center;color:#888;font-size:12px;'>4</td>" +
            "<td style='text-align:center;color:#888;font-size:12px;'>5</td>" +
            "</tr></table>" +

            "<p style='color:#999;font-size:12px;margin-top:20px;'>Este enlace es válido por 30 días y es de uso único.</p>" +
            "</td></tr>" +

            // FOOTER
            "<tr><td style='background:#f8f9fa;padding:20px 40px;text-align:center;border-top:1px solid #eee;'>" +
            "<p style='color:#999;font-size:12px;margin:0;'>© 2026 SpaceWork Perú S.A.C. | Todos los derechos reservados</p>" +
            "</td></tr>" +

            "</table></td></tr></table></body></html>";

        return enviarCorreo(emailCliente,
                "¿Cómo fue tu experiencia en " + nombreEspacio + "? ⭐ - SpaceWork",
                html);
    }

    public static boolean enviarConfirmacionPago(String emailCliente, String nombreCliente,
                                                   String nombreEspacio, double monto, String metodo) {
        String html =
            "<html><body style='font-family:Arial,sans-serif;'>" +
            "<h2 style='color:#27ae60;'>✓ Pago Completado</h2>" +
            "<p>Hola <strong>" + nombreCliente + "</strong>, tu pago fue procesado exitosamente.</p>" +
            "<p><b>Espacio:</b> " + nombreEspacio + "<br>" +
            "<b>Monto:</b> S/. " + String.format("%.2f", monto) + "<br>" +
            "<b>Método:</b> " + metodo + "</p>" +
            "<p style='color:#888;font-size:13px;'>Recibirás un correo para evaluar tu experiencia.</p>" +
            "<p style='color:#aaa;font-size:11px;'>© 2026 SpaceWork Perú S.A.C.</p>" +
            "</body></html>";
        return enviarCorreo(emailCliente, "Confirmación de Pago - SpaceWork", html);
    }
}

