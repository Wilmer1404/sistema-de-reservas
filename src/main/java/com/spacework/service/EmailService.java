package com.spacework.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.spacework.model.Cliente;
import com.spacework.model.Reserva;
import com.spacework.model.Pago;

@Service
public class EmailService {
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@spacework.com}")
    private String fromEmail;

    public void enviarConfirmacionReserva(Cliente cliente, Reserva reserva, Pago pago) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(cliente.getEmail());
            message.setSubject("Confirmación de Reserva - SpaceWork");
            message.setText(String.format(
                "Hola %s,\n\nTu reserva ha sido confirmada exitosamente.\n" +
                "Número de reserva: %d\n" +
                "Monto pagado: $%s\n\n" +
                "Gracias por usar SpaceWork",
                cliente.getNombre(), reserva.getIdReserva(), pago.getMontoFinal()
            ));
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error enviando email de confirmación: " + e.getMessage());
        }
    }

    public void enviarLinkEvaluacion(Cliente cliente, Reserva reserva, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(cliente.getEmail());
            message.setSubject("¿Cómo fue tu experiencia? - Evaluación - SpaceWork");
            message.setText(String.format(
                "Hola %s,\n\nNos gustaría saber tu opinión sobre tu reserva.\n" +
                "Por favor, haz clic en el siguiente enlace para evaluar:\n\n" +
                "http://localhost:8080/api/evaluacion.html?token=%s\n\n" +
                "Tu evaluación nos ayuda a mejorar nuestro servicio.\n" +
                "Gracias,\nEquipo SpaceWork",
                cliente.getNombre(), token
            ));
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error enviando link de evaluación: " + e.getMessage());
        }
    }

    public void enviarConfirmacionEvaluacion(Cliente cliente) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(cliente.getEmail());
            message.setSubject("Evaluación recibida - SpaceWork");
            message.setText(String.format(
                "Hola,\n\nThanks for your feedback! Your evaluation has been recorded.\n" +
                "We appreciate your time and will use it to improve our services.\n\n" +
                "Best regards,\nSpaceWork Team"
            ));
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error enviando confirmación de evaluación: " + e.getMessage());
        }
    }
}
