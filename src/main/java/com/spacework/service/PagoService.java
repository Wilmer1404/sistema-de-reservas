package com.spacework.service;

import com.spacework.dao.*;
import com.spacework.model.*;
import com.spacework.util.Conexion;
import com.spacework.util.EmailUtil;
import com.spacework.util.SimpleJwtUtil;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class PagoService {

    private final PagoDAO pagoDAO = new PagoDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final DescuentoDAO descuentoDAO = new DescuentoDAO();
    private final TokenEvaluacionDAO tokenEvaluacionDAO = new TokenEvaluacionDAO();

    public List<Map<String, Object>> listar() throws Exception {
        String sql = "SELECT p.id_pago, p.id_reserva, p.monto, p.metodo_pago, p.estado_pago, "
                   + "p.fecha_creacion, p.fecha_pago, NVL(p.descuento_aplicado, 0) AS descuento_aplicado, "
                   + "c.nombre || ' ' || c.apellido AS nombre_cliente, c.email AS email_cliente "
                   + "FROM PAGOS p "
                   + "JOIN RESERVAS r ON p.id_reserva = r.id_reserva "
                   + "JOIN CLIENTES c ON r.id_cliente = c.id_cliente "
                   + "ORDER BY p.estado_pago ASC, p.fecha_creacion DESC";
        List<Map<String, Object>> lista = new ArrayList<>();
        Connection conn = Conexion.getConexion();
        try {
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("idPago", rs.getInt("id_pago"));
                m.put("idReserva", rs.getInt("id_reserva"));
                m.put("monto", rs.getDouble("monto"));
                m.put("metodoPago", rs.getString("metodo_pago") != null ? rs.getString("metodo_pago") : "");
                m.put("estadoPago", rs.getString("estado_pago"));
                m.put("nombreCliente", rs.getString("nombre_cliente"));
                m.put("emailCliente", rs.getString("email_cliente"));
                m.put("fechaCreacion", rs.getDate("fecha_creacion") != null ? rs.getDate("fecha_creacion").toString() : "");
                m.put("fechaPago", rs.getDate("fecha_pago") != null ? rs.getDate("fecha_pago").toString() : "");
                lista.add(m);
            }
        } finally {
            Conexion.cerrar(conn);
        }
        return lista;
    }

    public Pago buscarPorId(int id) throws Exception {
        return pagoDAO.buscarPorId(id);
    }

    public void insertar(Pago p) throws Exception {
        p.setEstadoPago("PENDIENTE");
        pagoDAO.insertar(p);
        crearTokenEvaluacion(p.getIdReserva(), obtenerMaxPagoId(p.getIdReserva()));
    }

    public void actualizar(int id, String estadoPago, String metodoPago, Double monto) throws Exception {
        Pago p = pagoDAO.buscarPorId(id);
        if (p == null) throw new IllegalArgumentException("Pago no encontrado");
        if (estadoPago != null && !estadoPago.isEmpty()) p.setEstadoPago(estadoPago);
        if (metodoPago != null && !metodoPago.isEmpty()) p.setMetodoPago(metodoPago);
        if (monto != null) p.setMonto(monto);
        if (!pagoDAO.actualizar(p)) throw new RuntimeException("No se pudo actualizar el pago");
    }

    public Map<String, Object> pagar(int idPago, String metodoPago, String token,
                                     Double montoFinal, Integer idDescuento) throws Exception {
        // Validar método por rol
        if (token != null) {
            String rol = SimpleJwtUtil.obtenerRol(token);
            boolean esAdmin = "ADMIN".equals(rol) || "ADMINISTRADOR".equals(rol);
            if (("EFECTIVO".equals(metodoPago) || "TRANSFERENCIA".equals(metodoPago)) && !esAdmin)
                throw new SecurityException("El método " + metodoPago + " solo está disponible para administradores");
        }

        if ("TARJETA".equals(metodoPago) && token != null) {
            // validación de tarjeta ya viene del controller
        }

        Pago pago = pagoDAO.buscarPorId(idPago);
        if (pago == null) throw new IllegalArgumentException("Pago no encontrado");
        if ("COMPLETADO".equals(pago.getEstadoPago())) throw new IllegalStateException("El pago ya fue procesado");

        double montoOriginal = pago.getMonto();
        double montoAPagar = (montoFinal != null && montoFinal > 0 && montoFinal < montoOriginal) ? montoFinal : montoOriginal;
        double descuentoMonto = montoOriginal - montoAPagar;
        int descId = idDescuento != null ? idDescuento : 0;

        if (!pagoDAO.pagar(idPago, metodoPago, montoAPagar, descId, descuentoMonto))
            throw new RuntimeException("Error al procesar el pago");

        if (descId > 0) descuentoDAO.incrementarUsos(descId);
        reservaDAO.cambiarEstado(pago.getIdReserva(), "COMPLETADA");

        // Email + token evaluación
        String tokenEval = enviarConfirmacionYCrearToken(idPago, pago.getIdReserva(), montoAPagar, metodoPago);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("descuentoAplicado", descuentoMonto);
        result.put("montoFinal", montoAPagar);
        result.put("data", Collections.singletonMap("tokenEvaluacion", tokenEval != null ? tokenEval : ""));
        return result;
    }

    public void rechazar(int id) throws Exception {
        if (!pagoDAO.cambiarEstado(id, "RECHAZADO"))
            throw new IllegalArgumentException("Pago no encontrado");
    }

    private String enviarConfirmacionYCrearToken(int idPago, int idReserva, double monto, String metodo) {
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT c.nombre || ' ' || c.apellido AS nombre_cliente, c.email, c.id_cliente "
                + "FROM RESERVAS r JOIN CLIENTES c ON r.id_cliente = c.id_cliente WHERE r.id_reserva = ?");
            ps.setInt(1, idReserva);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String email = rs.getString("email");
                String nombre = rs.getString("nombre_cliente");
                int idCliente = rs.getInt("id_cliente");
                try { EmailUtil.enviarConfirmacionPago(email, nombre, idReserva, monto, metodo); } catch (Exception ignored) {}

                String token = java.util.UUID.randomUUID().toString();
                TokenEvaluacion te = new TokenEvaluacion();
                te.setIdPago(idPago);
                te.setToken(token);
                te.setEmailCliente(email);
                te.setFechaCreacion(new java.util.Date());
                te.setFechaExpiracion(new java.util.Date(System.currentTimeMillis() + 30L * 24 * 3600 * 1000));
                te.setUtilizado(0);
                if (tokenEvaluacionDAO.crearToken(te)) {
                    crearNotificacionEvaluacion(conn, idReserva, token, email, nombre);
                    return token;
                }
            }
        } catch (Exception e) {
            System.err.println("[PagoService] enviarConfirmacion: " + e.getMessage());
        } finally {
            Conexion.cerrar(conn);
        }
        return null;
    }

    private void crearNotificacionEvaluacion(Connection conn, int idReserva, String token, String email, String nombre) {
        try {
            int idAdmin = 1;
            PreparedStatement psAdmin = conn.prepareStatement("SELECT MIN(id_usuario) FROM USUARIOS WHERE estado='ACTIVO'");
            ResultSet rsAdmin = psAdmin.executeQuery();
            if (rsAdmin.next()) idAdmin = rsAdmin.getInt(1);
            psAdmin.close();

            PreparedStatement psNotif = conn.prepareStatement(
                "INSERT INTO NOTIFICACIONES (id_notificacion, id_usuario, tipo, asunto, mensaje, leida, fecha_creacion) "
                + "VALUES (SEQ_NOTIFICACIONES.NEXTVAL, ?, 'EVALUACION', ?, ?, 0, SYSDATE)");
            psNotif.setInt(1, idAdmin);
            psNotif.setString(2, "Solicitar evaluación - Reserva #" + idReserva);
            psNotif.setString(3, "Token: " + token + " | Email: " + email + " | Cliente: " + nombre);
            psNotif.executeUpdate();
            conn.commit();
            psNotif.close();
        } catch (Exception e) {
            System.err.println("[PagoService] crearNotificacion: " + e.getMessage());
        }
    }

    private void crearTokenEvaluacion(int idReserva, int idPago) {
        if (idPago == 0) return;
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT c.nombre || ' ' || c.apellido AS nombre_cliente, c.email "
                + "FROM RESERVAS r JOIN CLIENTES c ON r.id_cliente = c.id_cliente WHERE r.id_reserva = ?");
            ps.setInt(1, idReserva);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String email = rs.getString("email");
                String token = java.util.UUID.randomUUID().toString().replace("-", "");
                TokenEvaluacion te = new TokenEvaluacion();
                te.setIdPago(idPago);
                te.setToken(token);
                te.setEmailCliente(email);
                te.setFechaExpiracion(new java.util.Date(System.currentTimeMillis() + 30L * 24 * 3600 * 1000));
                tokenEvaluacionDAO.crearToken(te);
                crearNotificacionEvaluacion(conn, idReserva, token, email, rs.getString("nombre_cliente"));
            }
        } catch (Exception e) {
            System.err.println("[PagoService] crearTokenEvaluacion: " + e.getMessage());
        } finally {
            Conexion.cerrar(conn);
        }
    }

    private int obtenerMaxPagoId(int idReserva) {
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement("SELECT MAX(id_pago) FROM PAGOS WHERE id_reserva = ?");
            ps.setInt(1, idReserva);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.err.println("[PagoService] obtenerMaxPagoId: " + e.getMessage());
        } finally {
            Conexion.cerrar(conn);
        }
        return 0;
    }
}
