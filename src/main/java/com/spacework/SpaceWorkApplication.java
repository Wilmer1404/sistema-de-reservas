package com.spacework;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.sql.Connection;

@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
public class SpaceWorkApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpaceWorkApplication.class, args);
    }

    @Bean
    public CommandLineRunner init() {
        return args -> {
            initDatabase();
            sincronizarPagos();
        };
    }

    private void initDatabase() {
        try {
            java.sql.Connection conn = com.spacework.util.Conexion.getConexion();
            if (conn == null) return;
            try {
                conn.createStatement().execute("ALTER TABLE ESPACIOS ADD (imagen_url CLOB)");
                System.out.println("[DB] Columna imagen_url CLOB agregada a ESPACIOS");
            } catch (java.sql.SQLException e2) {
                String msg = String.valueOf(e2.getMessage());
                if (msg.contains("1430") || msg.contains("ya existe") || msg.contains("already")) {
                    try {
                        java.sql.ResultSet rs = conn.createStatement().executeQuery(
                            "SELECT data_type FROM user_tab_columns WHERE table_name='ESPACIOS' AND column_name='IMAGEN_URL'");
                        if (rs.next()) {
                            String dtype = rs.getString(1);
                            if (!"CLOB".equals(dtype)) {
                                conn.createStatement().execute("ALTER TABLE ESPACIOS ADD (imagen_url_tmp CLOB)");
                                conn.createStatement().execute("UPDATE ESPACIOS SET imagen_url_tmp = imagen_url");
                                conn.commit();
                                conn.createStatement().execute("ALTER TABLE ESPACIOS DROP COLUMN imagen_url");
                                conn.createStatement().execute("ALTER TABLE ESPACIOS RENAME COLUMN imagen_url_tmp TO imagen_url");
                                conn.commit();
                            }
                        }
                        rs.close();
                    } catch (Exception ex2) {
                        System.out.println("[DB] No se pudo convertir columna: " + ex2.getMessage());
                    }
                }
            }
            com.spacework.util.Conexion.cerrar(conn);
        } catch (Exception ex) {
            System.out.println("[DB] No se pudo verificar columna imagen_url: " + ex.getMessage());
        }
    }

    private void sincronizarPagos() {
        try {
            Connection conn = com.spacework.util.Conexion.getConexion();
            try {
                conn.createStatement().executeUpdate(
                    "CREATE SEQUENCE SEQ_PAGOS START WITH 100 INCREMENT BY 1 NOCACHE NOCYCLE");
                conn.commit();
            } catch (Exception ignored) {}

            String sql = "INSERT INTO PAGOS (id_pago, id_reserva, monto, metodo_pago, estado_pago, fecha_pago, fecha_creacion) "
                       + "SELECT SEQ_PAGOS.NEXTVAL, r.id_reserva, r.monto_total, 'EFECTIVO', "
                       + "CASE r.estado WHEN 'COMPLETADA' THEN 'COMPLETADO' WHEN 'CANCELADA' THEN 'RECHAZADO' ELSE 'PENDIENTE' END, "
                       + "r.fecha_inicio, SYSDATE "
                       + "FROM RESERVAS r "
                       + "WHERE r.id_reserva NOT IN (SELECT DISTINCT id_reserva FROM PAGOS)";
            int rows = conn.createStatement().executeUpdate(sql);
            conn.commit();
            conn.close();
            if (rows > 0) System.out.println("[DB] " + rows + " pagos sincronizados");
        } catch (Exception e) {
            System.out.println("[DB] Sincronización de pagos: " + e.getMessage());
        }
    }
}
