package ar.edu.vetgest.excepcion;

/** Se busco un registro (turno, mascota, lote...) que no existe. */
public class EntidadNoEncontradaException extends VetGestException {

    private static final long serialVersionUID = 1L;

    public EntidadNoEncontradaException(String entidad, Object id) {
        super("No existe " + entidad + " con identificador " + id);
    }

    @Override
    public String getCodigo() {
        return "NO_ENCONTRADO";
    }
}
