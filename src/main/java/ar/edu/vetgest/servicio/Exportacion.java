package ar.edu.vetgest.servicio;

import java.nio.file.Path;

/** Resultado de guardar un reporte: el archivo generado y la cantidad de filas de datos. */
public record Exportacion(Path archivo, int filas) {
}
