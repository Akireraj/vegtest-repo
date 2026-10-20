package ar.edu.vetgest.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Fila del historial clinico de una mascota en cualquier sucursal (RF05). */
public record EntradaHistorial(LocalDate fecha, String sucursal, String veterinario, BigDecimal pesoKg,
                               String diagnostico, String tratamiento) {
}
