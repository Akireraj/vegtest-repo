package ar.edu.vetgest.modelo;

import java.util.EnumSet;
import java.util.Set;

/** Roles de la tabla usuario y modulos habilitados para cada uno (RF10). */
public enum Rol {
    RECEPCION(EnumSet.of(Modulo.TURNOS, Modulo.SALA_ESPERA, Modulo.CONSULTAS)),
    VETERINARIO(EnumSet.of(Modulo.SALA_ESPERA, Modulo.ATENCION, Modulo.CONSULTAS)),
    STOCK(EnumSet.of(Modulo.INVENTARIO, Modulo.CONSULTAS)),
    ADMIN(EnumSet.allOf(Modulo.class));

    private final Set<Modulo> modulos;

    Rol(Set<Modulo> modulos) {
        this.modulos = modulos;
    }

    public boolean puedeAcceder(Modulo modulo) {
        return modulos.contains(modulo);
    }
}
