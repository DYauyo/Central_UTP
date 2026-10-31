package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static ConexionBD instancia;
    private final String url = "jdbc:mysql://localhost:3306/sistema_restaurante2"
            + "?sslMode=DISABLED"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Lima"
            + "&useUnicode=true"
            + "&characterEncoding=UTF-8";
    private final String usuario = "root";
    private final String clave = "Admin123!";

    private ConexionBD() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontro el driver de MySQL", e);
        }
    }

    public static synchronized ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    public Connection getConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }
}
