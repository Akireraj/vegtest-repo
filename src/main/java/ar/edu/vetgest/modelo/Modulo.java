package ar.edu.vetgest.modelo;

/** Modulos del menu principal; el acceso depende del rol (RF10). */
public enum Modulo {
    TURNOS("Turnos y pacientes"),
    SALA_ESPERA("Sala de espera"),
    ATENCION("Atencion clinica"),
    INVENTARIO("Inventario"),
    CONSULTAS("Consultas y busquedas");

    private final String descripcion;

    Modulo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
