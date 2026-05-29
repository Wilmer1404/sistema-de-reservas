package com.spacework.service;

import com.spacework.dao.*;
import com.spacework.model.*;
import com.spacework.util.Conexion;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReservaService {

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final EspacioDAO espacioDAO = new EspacioDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final PagoDAO pagoDAO = new PagoDAO();
    private final HorarioBloqueadoDAO horarioBloqueadoDAO = new HorarioBloqueadoDAO();

    public List<Reserva> listarTodas() throws Exception {
        return reservaDAO.listarTodas();
    }

    public Reserva buscarPorId(int id) throws Exception {
        return reservaDAO.buscarPorId(id);
    }

    public Map<String, Object> crear(int idCliente, int idEspacio, String fechaIniStr, String fechaFinStr) throws Exception {
        Espacio esp = espacioDAO.buscarPorId(idEspacio);
        List<Cliente> clientes = clienteDAO.listar();
        Cliente cli = null;
        for (Cliente c : clientes) { if (c.getIdCliente() == idCliente) { cli = c; break; } }

        if (esp == null || cli == null)
            throw new IllegalArgumentException("Cliente o espacio no encontrado");

        String fi = fechaIniStr.replace("T", " ").replace("Z", "").trim();
        String ff = fechaFinStr.replace("T", " ").replace("Z", "").trim();
        if (fi.length() == 16) fi += ":00";
        if (ff.length() == 16) ff += ":00";
        Timestamp tsIni = Timestamp.valueOf(fi);
        Timestamp tsFin = Timestamp.valueOf(ff);

        if (!horarioBloqueadoDAO.verificarDisponibilidadHorarios(idEspacio, tsIni, tsFin))
            throw new IllegalStateException("El espacio tiene un horario bloqueado en ese rango de fechas");

        if (!reservaDAO.verificarDisponibilidad(idEspacio, tsIni, tsFin))
            throw new IllegalStateException("El espacio ya tiene una reserva en ese rango de horario");

        double horas = (tsFin.getTime() - tsIni.getTime()) / (1000.0 * 60 * 60);
        double monto = esp.getPrecioPorHora() * horas;

        Reserva r = new Reserva();
        r.setCliente(cli);
        r.setEspacio(esp);
        r.setFechaInicio(tsIni);
        r.setFechaFin(tsFin);
        r.setMontoTotal(monto);
        r.setEstado("PENDIENTE");
        reservaDAO.insertar(r);

        int newId = obtenerMaxId("RESERVAS", "id_reserva", "id_cliente", idCliente);
        if (newId == 0) throw new RuntimeException("No se pudo obtener el ID de la reserva creada");

        Pago pago = new Pago();
        pago.setIdReserva(newId);
        pago.setMonto(monto);
        pago.setMontoFinal(monto);
        pago.setDescuentoAplicado(0);
        pago.setIgv(0);
        pago.setEstadoPago("PENDIENTE");
        pago.setMetodoPago("EFECTIVO");
        try { pagoDAO.insertar(pago); } catch (Exception e) {
            System.err.println("[ReservaService] Pago no creado: " + e.getMessage());
        }

        int pagoId = obtenerMaxId("PAGOS", "id_pago", "id_reserva", newId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("idReserva", newId);
        result.put("idPago", pagoId);
        result.put("monto", monto);
        return result;
    }

    public Map<String, Object> actualizar(int idReserva, int idCliente, int idEspacio,
                                          String fechaIniStr, String fechaFinStr) throws Exception {
        Reserva actual = reservaDAO.buscarPorId(idReserva);
        if (actual == null) throw new IllegalArgumentException("Reserva no encontrada");

        if (idCliente == 0) idCliente = actual.getCliente().getIdCliente();
        if (idEspacio == 0) idEspacio = actual.getEspacio().getIdEspacio();

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (fechaIniStr == null || fechaIniStr.isEmpty()) fechaIniStr = sdf.format(actual.getFechaInicio());
        if (fechaFinStr == null || fechaFinStr.isEmpty()) fechaFinStr = sdf.format(actual.getFechaFin());

        String fi = fechaIniStr.replace("T", " ").replace("Z", "").trim();
        String ff = fechaFinStr.replace("T", " ").replace("Z", "").trim();
        if (fi.length() == 16) fi += ":00";
        if (ff.length() == 16) ff += ":00";
        Timestamp tsIni = Timestamp.valueOf(fi);
        Timestamp tsFin = Timestamp.valueOf(ff);

        if (tsFin.before(tsIni) || tsFin.equals(tsIni))
            throw new IllegalArgumentException("Rango de fechas inválido");

        Connection conn = Conexion.getConexion();
        try {
            String sqlVal = "SELECT COUNT(*) cnt FROM RESERVAS WHERE id_espacio = ? AND id_reserva <> ? "
                + "AND estado NOT IN ('CANCELADA') AND (fecha_inicio < ? AND fecha_fin > ?)";
            PreparedStatement psVal = conn.prepareStatement(sqlVal);
            psVal.setInt(1, idEspacio); psVal.setInt(2, idReserva);
            psVal.setTimestamp(3, tsFin); psVal.setTimestamp(4, tsIni);
            ResultSet rsVal = psVal.executeQuery();
            if (rsVal.next() && rsVal.getInt("cnt") > 0)
                throw new IllegalStateException("El espacio ya tiene una reserva en ese rango");
            psVal.close();

            Espacio esp = espacioDAO.buscarPorId(idEspacio);
            if (esp == null) throw new IllegalArgumentException("Espacio no encontrado");

            double horas = (tsFin.getTime() - tsIni.getTime()) / (1000.0 * 60 * 60);
            double monto = esp.getPrecioPorHora() * horas;

            PreparedStatement psUpd = conn.prepareStatement(
                "UPDATE RESERVAS SET id_cliente=?, id_espacio=?, fecha_inicio=?, fecha_fin=?, monto_total=? WHERE id_reserva=?");
            psUpd.setInt(1, idCliente); psUpd.setInt(2, idEspacio);
            psUpd.setTimestamp(3, tsIni); psUpd.setTimestamp(4, tsFin);
            psUpd.setDouble(5, monto); psUpd.setInt(6, idReserva);
            psUpd.executeUpdate();

            PreparedStatement psPago = conn.prepareStatement(
                "UPDATE PAGOS SET monto=? WHERE id_reserva=? AND estado_pago='PENDIENTE'");
            psPago.setDouble(1, monto); psPago.setInt(2, idReserva);
            psPago.executeUpdate();
            conn.commit();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("monto", monto);
            return result;
        } finally {
            Conexion.cerrar(conn);
        }
    }

    public void confirmar(int id) throws Exception {
        Reserva reserva = reservaDAO.buscarPorId(id);
        if (reserva == null) throw new IllegalArgumentException("Reserva no encontrada");
        reservaDAO.cambiarEstado(id, "CONFIRMADA");

        Pago pago = new Pago();
        pago.setIdReserva(id);
        pago.setMonto(reserva.getMontoTotal());
        pago.setMontoFinal(reserva.getMontoTotal());
        pago.setMetodoPago("EFECTIVO");
        pago.setEstadoPago("PENDIENTE");
        pago.setFechaCreacion(new java.util.Date());
        pagoDAO.insertar(pago);
    }

    public void cambiarEstado(int id, String estado) throws Exception {
        reservaDAO.cambiarEstado(id, estado);
    }

    private int obtenerMaxId(String tabla, String colId, String colWhere, int valWhere) {
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT MAX(" + colId + ") FROM " + tabla + " WHERE " + colWhere + " = ?");
            ps.setInt(1, valWhere);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.err.println("[ReservaService] obtenerMaxId: " + e.getMessage());
        } finally {
            Conexion.cerrar(conn);
        }
        return 0;
    }
}
