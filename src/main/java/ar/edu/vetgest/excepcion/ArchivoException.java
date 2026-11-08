package ar.edu.vetgest.excepcion;

/**
 * Error al guardar o recuperar un archivo (reportes CSV). Envuelve la
 * IOException comprobada de java.io / java.nio, igual que DatosException
 * envuelve la SQLException: los servicios y menus no dependen de java.io.
 */
public class ArchivoException extends VetGestException {

    private static final long serialVersionUID = 1L;

    public ArchivoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    @Override
    public String getCodigo() {
        return "ARCHIVO";
    }
}
