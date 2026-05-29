package com.spacework.service;

import com.spacework.dao.*;
import com.spacework.model.*;
import com.spacework.util.Conexion;
import com.spacework.util.EmailUtil;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class EvaluacionService {

    private final EvaluacionDAO evaluacionDAO = new EvaluacionDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final TokenEvaluacionDAO tokenDAO = new TokenEvaluacionDAO();

    public List<Map<String, Object>> listar() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = "SELECT e.id_evaluacion, e.id_reserva, e.id_cliente, e.calificacion, "
                   + "e.comentario, e.fecha_evaluacion AS fecha_ref, c.nombre, c.apellido, c.email, "
                   + "es.nombre as nombreEspacio, 'COMPLETADA' as estado "
                   + "FROM EVALUACIONES e "
                   + "JOIN CLIENTES c ON e.id_cliente = c.id_cliente "
                   + "JOIN RESERVAS r ON e.id_reserva = r.id_reserva "
                   + "JOIN ESPACIOS es ON r.id_espacio = es.id_espacio "
                   + "UNION ALL "
                   + "SELECT 0 AS id_evaluacion, r.id_reserva, r.id_cliente, 0 AS calificacion, "
                   + "NULL AS comentario, p.fecha_pago AS fecha_ref, c.nombre, c.apellido, c.email, "
                   + "es.nombre as nombreEspacio, "
                   + "CASE WHEN EXISTS (SELECT 1 FROM NOTIFICACIONES n "
                   + "WHERE n.tipo = 'EVALUACION' AND n.asunto = 'Solicitar evaluación - Reserva #' || r.id_reserva AND n.leida = 1) "
                   + "THEN 'ENVIADO' ELSE 'PENDIENTE' END as estado "
                   + "FROM PAGOS p JOIN RESERVAS r ON p.id_reserva = r.id_reserva "
                   + "JOIN CLIENTES c ON r.id_cliente = c.id_cliente "
                   + "JOIN ESPACIOS es ON r.id_espacio = es.id_espacio "
                   + "WHERE p.estado_pago = 'COMPLETADO' "
                   + "AND NOT EXISTS (SELECT 1 FROM EVALUACIONES ev WHERE ev.id_reserva = p.id_reserva) "
                   + "ORDER BY fecha_ref DESC";

        List<Map<String, Object>> lista = new ArrayList<>();
        Connection conn = Conexion.getConexion();
        try {
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("idEvaluacion", rs.getInt("id_evaluacion"));
                m.put("idReserva", rs.getInt("id_reserva"));
                m.put("idCliente", rs.getInt("id_cliente"));
                m.put("calificacion", rs.getInt("calificacion"));
                m.put("comentarios", rs.getString("comentario") != null ? rs.getString("comentario") : "");
                m.put("nombreCliente", rs.getString("nombre") + " " + rs.getString("apellido"));
                m.put("nombreEspacio", rs.getString("nombreEspacio"));
                m.put("emailCliente", rs.getString("email"));
                m.put("estado", rs.getString("estado"));
                java.util.Date fe = rs.getTimestamp("fecha_ref");
                m.put("fechaEvaluacion", fe != null ? sdf.format(fe) : "");
                lista.add(m);
            }
        } finally {
            Conexion.cerrar(conn);
        }
        return lista;
    }

    public Evaluacion buscarPorId(int id) throws Exception {
        return evaluacionDAO.buscarPorId(id);
    }

    public void crear(int idReserva, int calificacion, String comentario) throws Exception {
        Reserva reserva = reservaDAO.buscarPorId(idReserva);
        if (reserva == null) throw new IllegalArgumentException("Reserva no encontrada");
        Evaluacion ev = new Evaluacion();
        ev.setIdReserva(idReserva);
        ev.setIdCliente(reserva.getCliente().getIdCliente());
        ev.setCalificacion(calificacion);
        ev.setComentario(comentario != null ? comentario : "");
        ev.setFechaEvaluacion(new java.util.Date());
        evaluacionDAO.insertar(ev);
        reservaDAO.cambiarEstado(idReserva, "COMPLETADA");
    }

    public void actualizar(int id, Integer calificacion, String comentario) throws Exception {
        Evaluacion ev = evaluacionDAO.buscarPorId(id);
        if (ev == null) throw new IllegalArgumentException("Evaluación no encontrada");
        if (calificacion != null) ev.setCalificacion(calificacion);
        if (comentario != null) ev.setComentario(comentario);
        if (!evaluacionDAO.actualizar(ev)) throw new RuntimeException("No se pudo actualizar la evaluación");
    }

    public void eliminar(int id) throws Exception {
        if (!evaluacionDAO.eliminar(id)) throw new IllegalArgumentException("Evaluación no encontrada");
    }

    public String enviarPorReserva(int idReserva) throws Exception {
        Connection conn = Conexion.getConexion();
        try {
            String sql = "SELECT te.token, te.email_cliente, c.nombre || ' ' || c.apellido AS nombre_cliente "
                       + "FROM TOKENS_EVALUACION te "
                       + "JOIN PAGOS p ON te.id_pago = p.id_pago "
                       + "JOIN RESERVAS r ON p.id_reserva = r.id_reserva "
                       + "JOIN CLIENTES c ON r.id_cliente = c.id_cliente "
                       + "WHERE r.id_reserva = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idReserva);
            ResultSet rs = ps.executeQuery();

            String token, email, nombre;
            if (rs.next()) {
                token = rs.getString("token");
                email = rs.getString("email_cliente");
                nombre = rs.getString("nombre_cliente");
            } else {
                // Generar nuevo token
                String sqlPago = "SELECT p.id_pago, c.nombre || ' ' || c.apellido AS nombre_cliente, c.email "
                               + "FROM PAGOS p JOIN RESERVAS r ON p.id_reserva = r.id_reserva "
                               + "JOIN CLIENTES c ON r.id_cliente = c.id_cliente "
                               + "WHERE p.id_reserva = ? AND p.estado_pago = 'COMPLETADO'";
                PreparedStatement ps2 = conn.prepareStatement(sqlPago);
                ps2.setInt(1, idReserva);
                ResultSet rs2 = ps2.executeQuery();
                if (!rs2.next()) throw new IllegalArgumentException("Pago completado no encontrado para esta reserva");

                int idPago = rs2.getInt("id_pago");
                email = rs2.getString("email");
                nombre = rs2.getString("nombre_cliente");
                token = java.util.UUID.randomUUID().toString().replace("-", "");

                PreparedStatement psIns = conn.prepareStatement(
                    "INSERT INTO TOKENS_EVALUACION (id_token, id_pago, token, email_cliente, fecha_expiracion) "
                    + "VALUES (SEQ_TOKENS_EVALUACION.NEXTVAL, ?, ?, ?, SYSDATE + 7)");
                psIns.setInt(1, idPago); psIns.setString(2, token); psIns.setString(3, email);
                psIns.executeUpdate();
                conn.commit();
            }

            boolean enviado = EmailUtil.enviarFormularioEvaluacion(email, nombre, idReserva, token, "http://localhost:8080");
            if (enviado) {
                PreparedStatement psN = conn.prepareStatement(
                    "UPDATE NOTIFICACIONES SET leida = 1 WHERE tipo = 'EVALUACION' AND asunto = ?");
                psN.setString(1, "Solicitar evaluación - Reserva #" + idReserva);
                psN.executeUpdate();
                conn.commit();
            }
            return enviado ? email : null;
        } finally {
            Conexion.cerrar(conn);
        }
    }

    public String enviarDesdeNotificacion(int idNotificacion) throws Exception {
        Connection conn = Conexion.getConexion();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT mensaje FROM NOTIFICACIONES WHERE id_notificacion = ?");
            ps.setInt(1, idNotificacion);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) throw new IllegalArgumentException("Notificación no encontrada");

            String mensaje = rs.getString("mensaje");
            String token = mensaje.split("Token: ")[1].split(" \\| ")[0].trim();
            String emailRaw = mensaje.split("Email: ")[1].trim();
            String email = emailRaw.split("[ \\|\\n\\r]")[0].trim();

            PreparedStatement psToken = conn.prepareStatement(
                "SELECT id_pago FROM TOKENS_EVALUACION WHERE token = ?");
            psToken.setString(1, token);
            ResultSet rsToken = psToken.executeQuery();
            if (!rsToken.next()) throw new IllegalArgumentException("Token inválido");

            int idPago = rsToken.getInt("id_pago");
            PreparedStatement psRes = conn.prepareStatement(
                "SELECT r.id_reserva, c.nombre || ' ' || c.apellido AS nombre_cliente "
                + "FROM PAGOS p JOIN RESERVAS r ON p.id_reserva = r.id_reserva "
                + "JOIN CLIENTES c ON r.id_cliente = c.id_cliente WHERE p.id_pago = ?");
            psRes.setInt(1, idPago);
            ResultSet rsRes = psRes.executeQuery();
            if (!rsRes.next()) throw new IllegalArgumentException("Reserva no encontrada");

            int idReserva = rsRes.getInt("id_reserva");
            String nombre = rsRes.getString("nombre_cliente");

            boolean enviado = EmailUtil.enviarFormularioEvaluacion(email, nombre, idReserva, token, "http://localhost:8080");
            if (enviado) {
                PreparedStatement psLeida = conn.prepareStatement(
                    "UPDATE NOTIFICACIONES SET leida=1 WHERE id_notificacion=?");
                psLeida.setInt(1, idNotificacion);
                psLeida.executeUpdate();
                conn.commit();
            }
            return enviado ? email : null;
        } finally {
            Conexion.cerrar(conn);
        }
    }
}
