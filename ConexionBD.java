package com.vetgest.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Administra la conexion JDBC hacia la base de datos MySQL "vetgest_db".
 *
 * Las credenciales se leen desde src/main/resources/config.properties
 * (ver config.properties.example como plantilla).
 */
public class ConexionBD {

    private static final String ARCHIVO_CONFIG = "/config.properties";
    private static String url;
    private static String usuario;
    private static String contrasenia;

    static {
        cargarConfiguracion();
    }

    private ConexionBD() {
        // clase utilitaria, no instanciable
    }

    private static void cargarConfiguracion() {
        Properties props = new Properties();
        try (InputStream input = ConexionBD.class.getResourceAsStream(ARCHIVO_CONFIG)) {
            if (input == null) {
                url = "jdbc:mysql://localhost:3306/vetgest_db?useSSL=false&serverTimezone=UTC";
                usuario = "root";
                contrasenia = "";
                System.err.println("[ConexionBD] No se encontro config.properties, se usan valores por defecto.");
                return;
            }
            props.load(input);
            url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/vetgest_db?useSSL=false&serverTimezone=UTC");
            usuario = props.getProperty("db.usuario", "root");
            contrasenia = props.getProperty("db.contrasenia", "");
        } catch (IOException e) {
            throw new RuntimeException("Error al leer config.properties", e);
        }
    }

    /**
     * Obtiene una nueva conexion JDBC. El llamador es responsable de
     * cerrarla (recomendado: try-with-resources).
     */
    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, contrasenia);
    }
}
