package ar.edu.vetgest.modelo;

import ar.edu.vetgest.excepcion.DatoInvalidoException;

/** Especies del catalogo (tabla especie). El id coincide con la base de datos. */
public enum Especie {
    CANINO(1, "Canino"),
    FELINO(2, "Felino"),
    EXOTICO(3, "Exotico");

    private final int id;
    private final String nombre;

    Especie(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public static Especie desdeId(int id) {
        for (Especie e : values()) {
            if (e.id == id) {
                return e;
            }
        }
        throw new DatoInvalidoException("Especie inexistente: " + id);
    }
}
