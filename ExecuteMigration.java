import java.sql.*;

public class ExecuteMigration {
    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@localhost:1521:XE";
        String user = "juan";
        String password = "MiContra123";
        
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(url, user, password);
            
            // SQL statements from migrations
            String[] sqls = {
                "ALTER TABLE reservas ADD (urlImagen CLOB)",
                "COMMENT ON COLUMN reservas.urlImagen IS 'Imagen de referencia de la reserva en formato Base64 (data:image/...)'",
                "ALTER TABLE espacios ADD (urlImagen CLOB)",
                "COMMENT ON COLUMN espacios.urlImagen IS 'Imagen de portada del espacio en formato Base64 (data:image/...)'"
            };
            
            for (String sql : sqls) {
                try {
                    Statement stmt = con.createStatement();
                    stmt.execute(sql);
                    stmt.close();
                    System.out.println("[OK] Executed: " + sql);
                } catch (SQLException e) {
                    if (e.getMessage().contains("column already exists")) {
                        System.out.println("[SKIP] Column already exists");
                    } else if (e.getMessage().contains("invalid identifier")) {
                        System.out.println("[SKIP] " + e.getMessage());
                    } else {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                }
            }
            
            // Verify RESERVAS
            Statement verify = con.createStatement();
            ResultSet rs = verify.executeQuery("SELECT column_name FROM user_tab_columns WHERE table_name='RESERVAS'");
            boolean hasUrlImagenReservas = false;
            while (rs.next()) {
                String col = rs.getString("column_name");
                if ("URLIMAGEN".equals(col)) {
                    hasUrlImagenReservas = true;
                    break;
                }
            }
            
            if (hasUrlImagenReservas) {
                System.out.println("[SUCCESS] Column urlImagen verified in RESERVAS table");
            } else {
                System.out.println("[WARNING] Column urlImagen not found in RESERVAS");
            }
            
            // Verify ESPACIOS
            Statement verify2 = con.createStatement();
            ResultSet rs2 = verify2.executeQuery("SELECT column_name FROM user_tab_columns WHERE table_name='ESPACIOS'");
            boolean hasUrlImagenEspacios = false;
            while (rs2.next()) {
                String col = rs2.getString("column_name");
                if ("URLIMAGEN".equals(col)) {
                    hasUrlImagenEspacios = true;
                    break;
                }
            }
            
            if (hasUrlImagenEspacios) {
                System.out.println("[SUCCESS] Column urlImagen verified in ESPACIOS table");
            } else {
                System.out.println("[WARNING] Column urlImagen not found in ESPACIOS");
            }
            
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
