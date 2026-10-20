package ar.edu.vetgest.excepcion;

/** Un dato no cumple las validaciones del modelo (encapsulamiento). */
public class DatoInvalidoException extends VetGestException {

    private static final long serialVersionUID = 1L;

    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }

    @Override
    public String getCodigo() {
        return "DATO_INVALIDO";
    }
}
