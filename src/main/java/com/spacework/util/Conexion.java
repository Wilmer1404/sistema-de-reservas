package com.spacework.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    // Ajusta estos valores si tu Docker usa otro puerto, SID o usuario
    private static final String URL  = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USER = "spacework";
    private static final String PASS = "spacework";

    public static Connection getConexion() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASS);
        conn.setAutoCommit(false);
        return conn;
    }

    // Método auxiliar para cerrar la conexión de forma segura
    public static void cerrar(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}
