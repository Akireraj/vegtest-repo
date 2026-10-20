package ar.edu.vetgest.ui;

import ar.edu.vetgest.excepcion.VetGestException;

/** El usuario decidio no continuar una operacion; el menu informa y vuelve a mostrarse. */
public class OperacionCanceladaException extends VetGestException {

    private static final long serialVersionUID = 1L;

    public OperacionCanceladaException(String mensaje) {
        super(mensaje);
    }

    @Override
    public String getCodigo() {
        return "CANCELADA";
    }
}
