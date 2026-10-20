package ar.edu.vetgest.util;

import ar.edu.vetgest.excepcion.DatosException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Obtiene conexiones JDBC a vetgest_db. Los parametros se leen de
 * db.properties: primero desde la ruta indicada con -Dvetgest.config,
 * luego desde el directorio de trabajo y por ultimo desde el classpath.
 */
public final class ConexionBD {

    private static final String ARCHIVO = "db.properties";
    private static Properties configuracion;

    private ConexionBD() {
    }

    private static synchronized Properties configuracion() {
        if (configuracion == null) {
            configuracion = cargar();
        }
        return configuracion;
    }

    private static Properties cargar() {
        Properties props = new Properties();
        Path ruta = Path.of(System.getProperty("vetgest.config", ARCHIVO));
        try {
            if (Files.exists(ruta)) {
                try (InputStream in = Files.newInputStream(ruta)) {
                    props.load(in);
                }
            } else {
                try (InputStream in = ConexionBD.class.getResourceAsStream("/" + ARCHIVO)) {
                    if (in == null) {
                        throw new DatosException("No se encontro " + ruta.toAbsolutePath()
                                + " ni db.properties en el classpath (copie db.properties.example)", null);
                    }
                    props.load(in);
                }
            }
        } catch (IOException e) {
            throw new DatosException("No se pudo leer la configuracion de la base de datos", e);
        }
        if (props.getProperty("db.url") == null) {
            throw new DatosException("Falta la propiedad db.url en db.properties", null);
        }
        return props;
    }

    /** Nueva conexion: quien la pide debe cerrarla (try-with-resources). */
    public static Connection obtener() throws SQLException {
        Properties p = configuracion();
        return DriverManager.getConnection(p.getProperty("db.url"), p.getProperty("db.usuario"),
                p.getProperty("db.clave", ""));
    }

    /** Verifica la conexion y devuelve el nombre y la version del servidor. */
    public static String probarConexion() throws SQLException {
        try (Connection cn = obtener()) {
            DatabaseMetaData md = cn.getMetaData();
            return md.getDatabaseProductName() + " " + md.getDatabaseProductVersion();
        }
    }
}
