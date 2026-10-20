package ar.edu.vetgest.modelo;

/** Fila de la agenda: el turno junto con los nombres para mostrar (record inmutable). */
public record ItemAgenda(Turno turno, String mascota, String cliente, String veterinario) {
}
