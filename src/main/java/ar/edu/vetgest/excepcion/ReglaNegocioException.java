package ar.edu.vetgest.excepcion;

/** Se lanza cuando una operacion viola una regla de negocio (RF02, RF03, RF06...). */
public class ReglaNegocioException extends VetGestException {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }

    @Override
    public String getCodigo() {
        return "NEGOCIO";
    }
}
