package ar.edu.vetgest.dao.jdbc;

import ar.edu.vetgest.excepcion.DatosException;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.excepcion.VetGestException;

import java.sql.SQLException;

/** Utilidades comunes de las implementaciones JDBC. */
final class Jdbc {

    /** SQLSTATE que usa el trigger trg_turno_no_superposicion (SIGNAL '45000'). */
    static final String SQLSTATE_TRIGGER = "45000";
    /** Codigo de error de MySQL para clave duplicada (UNIQUE). */
    static final int ERROR_DUPLICADO = 1062;

    private Jdbc() {
    }

    /** Traduce una SQLException (comprobada) a la jerarquia propia (no comprobada). */
    static VetGestException traducir(String operacion, SQLException e) {
        if (SQLSTATE_TRIGGER.equals(e.getSQLState())) {
            return new ReglaNegocioException(e.getMessage() + " [rechazado por la base de datos]");
        }
        if (e.getErrorCode() == ERROR_DUPLICADO) {
            return new ReglaNegocioException("Registro duplicado al " + operacion + ": " + e.getMessage());
        }
        return new DatosException("Error al " + operacion, e);
    }
}
