package ar.edu.vetgest.ui;

/** Se cerro la entrada estandar (fin de archivo): el programa termina ordenadamente. */
public class EntradaFinalizadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EntradaFinalizadaException() {
        super("Fin de la entrada estandar");
    }
}
