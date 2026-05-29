package com.spacework.service;

import com.spacework.util.Conexion;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CalendarioService {

    public Map<String, Object> getSemanal() throws Exception {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        Connection conn = Conexion.getConexion();

        List<Map<String, Object>> espaciosList = new ArrayList<>();
        List<Map<String, Object>> bloques = new ArrayList<>();

        try {
            String sqlEsp = "SELECT id_espacio, nombre FROM ESPACIOS WHERE estado='ACTIVO' ORDER BY nombre";
            ResultSet rsE = conn.createStatement().executeQuery(sqlEsp);
            while (rsE.next()) {
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("idEspacio", rsE.getInt("id_espacio"));
                e.put("nombre", rsE.getString("nombre"));
                espaciosList.add(e);
            }

            for (int dia = 0; dia < 7; dia++) {
                String fechaStr = sdfDate.format(cal.getTime());
                for (int hora = 8; hora < 18; hora++) {
                    String horaIni = String.format("%02d:00", hora);
                    String horaFin = String.format("%02d:00", hora + 1);
                    Timestamp tsIni = new Timestamp(sdfFull.parse(fechaStr + " " + horaIni + ":00").getTime());
                    Timestamp tsFin = new Timestamp(sdfFull.parse(fechaStr + " " + horaFin + ":00").getTime());

                    Map<String, Object> bloque = new LinkedHashMap<>();
                    bloque.put("fecha", fechaStr);
                    bloque.put("hora", horaIni + "-" + horaFin);

                    Map<String, String> estadosPorEspacio = new LinkedHashMap<>();
                    for (Map<String, Object> esp : espaciosList) {
                        int idEsp = (int) esp.get("idEspacio");

                        PreparedStatement psRes = conn.prepareStatement(
                            "SELECT COUNT(*) cnt FROM RESERVAS WHERE id_espacio=? AND estado IN ('CONFIRMADA','COMPLETADA') "
                            + "AND (fecha_inicio < ? AND fecha_fin > ?)");
                        psRes.setInt(1, idEsp); psRes.setTimestamp(2, tsFin); psRes.setTimestamp(3, tsIni);
                        ResultSet rsRes = psRes.executeQuery();
                        int cntRes = rsRes.next() ? rsRes.getInt("cnt") : 0;
                        psRes.close();

                        PreparedStatement psHor = conn.prepareStatement(
                            "SELECT COUNT(*) cnt FROM HORARIOS_BLOQUEADOS WHERE id_espacio=? "
                            + "AND (fecha_inicio < ? AND fecha_fin > ?)");
                        psHor.setInt(1, idEsp); psHor.setTimestamp(2, tsFin); psHor.setTimestamp(3, tsIni);
                        ResultSet rsHor = psHor.executeQuery();
                        int cntHor = rsHor.next() ? rsHor.getInt("cnt") : 0;
                        psHor.close();

                        estadosPorEspacio.put(String.valueOf(idEsp),
                            cntRes > 0 ? "ocupado" : (cntHor > 0 ? "bloqueado" : "disponible"));
                    }
                    bloque.put("espacios", estadosPorEspacio);
                    bloques.add(bloque);
                }
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } finally {
            Conexion.cerrar(conn);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("espacios", espaciosList);
        data.put("bloques", bloques);
        return data;
    }
}
