package ar.edu.vetgest.excepcion;

import ar.edu.vetgest.modelo.EstadoTurno;

/** Transicion no permitida por el diagrama de estados del turno (Figura 6 de la AP2). */
public class TransicionInvalidaException extends ReglaNegocioException {

    private static final long serialVersionUID = 1L;

    private final EstadoTurno desde;
    private final EstadoTurno hacia;

    public TransicionInvalidaException(EstadoTurno desde, EstadoTurno hacia) {
        super("Transicion invalida: un turno " + desde + " no puede pasar a " + hacia);
        this.desde = desde;
        this.hacia = hacia;
    }

    public EstadoTurno getDesde() {
        return desde;
    }

    public EstadoTurno getHacia() {
        return hacia;
    }

    @Override
    public String getCodigo() {
        return "TRANSICION";
    }
}
