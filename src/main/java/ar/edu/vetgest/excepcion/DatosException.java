package ar.edu.vetgest.excepcion;

/**
 * Error tecnico de acceso a datos. Envuelve la SQLException (comprobada)
 * para que la capa de servicios no dependa de java.sql.
 */
public class DatosException extends VetGestException {

    private static final long serialVersionUID = 1L;

    public DatosException(String mensaje, Throwable causa) {
        super(causa == null ? mensaje : mensaje + " (" + causa.getMessage() + ")", causa);
    }

    @Override
    public String getCodigo() {
        return "DATOS";
    }
}
