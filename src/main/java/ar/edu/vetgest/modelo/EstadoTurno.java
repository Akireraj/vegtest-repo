package ar.edu.vetgest.modelo;

/** Estados del turno y transiciones validas (diagrama de estados, Figura 6 de la AP2). */
public enum EstadoTurno {
    RESERVADO, CONFIRMADO, ATENDIDO, CANCELADO, AUSENTE;

    /** Los estados activos ocupan la agenda y participan de la validacion de superposicion. */
    public boolean esActivo() {
        return this == RESERVADO || this == CONFIRMADO;
    }

    public boolean puedePasarA(EstadoTurno destino) {
        return switch (this) {
            case RESERVADO -> destino == CONFIRMADO || destino == CANCELADO;
            case CONFIRMADO -> destino == ATENDIDO || destino == AUSENTE || destino == CANCELADO;
            case ATENDIDO, CANCELADO, AUSENTE -> false;   // estados finales
        };
    }
}
