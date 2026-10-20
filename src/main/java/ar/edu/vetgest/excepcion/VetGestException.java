package ar.edu.vetgest.excepcion;

/**
 * Raiz de la jerarquia de excepciones propias de VetGest.
 * Es abstracta (no se lanza directamente) y no comprobada (extiende
 * RuntimeException), de modo que las capas intermedias no necesitan
 * declararla y la interfaz la captura en un unico punto.
 */
public abstract class VetGestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected VetGestException(String mensaje) {
        super(mensaje);
    }

    protected VetGestException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    /** Codigo corto usado en la bitacora y en los mensajes de la interfaz. */
    public abstract String getCodigo();
}
