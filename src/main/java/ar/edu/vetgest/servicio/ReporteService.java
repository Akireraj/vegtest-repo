package ar.edu.vetgest.servicio;

import ar.edu.vetgest.algoritmos.Ordenamiento;
import ar.edu.vetgest.archivo.ExportadorCSV;
import ar.edu.vetgest.excepcion.ArchivoException;
import ar.edu.vetgest.modelo.AlertaStock;
import ar.edu.vetgest.modelo.ItemAgenda;
import ar.edu.vetgest.modelo.Sucursal;
import ar.edu.vetgest.modelo.Turno;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Reportes en archivos (RF09): guarda la agenda del dia y las alertas de
 * stock en CSV y permite recuperarlos despues, por ejemplo para enviarlos a
 * la administracion o abrirlos en una planilla de calculo.
 */
public class ReporteService {

    private static final String[] COLUMNAS_AGENDA = {
            "Turno", "Fecha", "Hora", "Duracion (min)", "Veterinario/a", "Mascota", "Cliente", "Motivo", "Estado"};
    private static final String[] COLUMNAS_ALERTAS = {
            "Sucursal", "Insumo", "Stock vigente", "Stock minimo", "Cobertura (%)"};

    private final TurnoService turnos;
    private final InventarioService inventario;
    private final Path carpeta;
    private final Clock reloj;

    public ReporteService(TurnoService turnos, InventarioService inventario, Path carpeta, Clock reloj) {
        this.turnos = turnos;
        this.inventario = inventario;
        this.carpeta = carpeta;
        this.reloj = reloj;
    }

    /** Guarda la agenda del dia de la sucursal: una fila (String[]) por turno. */
    public Exportacion exportarAgenda(Sucursal sucursal, LocalDate fecha) {
        List<String[]> filas = new ArrayList<>();
        for (ItemAgenda i : turnos.agenda(sucursal.getId(), fecha)) {
            Turno t = i.turno();
            filas.add(new String[]{
                    String.valueOf(t.getIdTurno()), fecha.toString(), t.getFechaHora().toLocalTime().toString(),
                    String.valueOf(t.getDuracionMin()), i.veterinario(), i.mascota(), i.cliente(), t.getMotivo(),
                    t.getEstado().name()});
        }
        Path archivo = carpeta.resolve("agenda_" + normalizar(sucursal.getNombre()) + "_" + fecha + ".csv");
        return new Exportacion(archivo, ExportadorCSV.escribir(archivo, COLUMNAS_AGENDA, filas));
    }

    /** Guarda las alertas de stock minimo de la red, ordenadas por criticidad. */
    public Exportacion exportarAlertas() {
        List<String[]> filas = new ArrayList<>();
        for (AlertaStock a : inventario.alertasPorCriticidad()) {
            filas.add(new String[]{a.getSucursal(), a.getInsumo(), String.valueOf(a.getStockVigente()),
                    String.valueOf(a.getStockMinimo()), String.valueOf(Math.round(a.getCobertura() * 100))});
        }
        Path archivo = carpeta.resolve("alertas_stock_" + LocalDate.now(reloj) + ".csv");
        return new Exportacion(archivo, ExportadorCSV.escribir(archivo, COLUMNAS_ALERTAS, filas));
    }

    /** Reportes guardados en la carpeta, ordenados por nombre (merge sort). */
    public List<Path> reportesGuardados() {
        List<Path> archivos = new ArrayList<>();
        if (!Files.isDirectory(carpeta)) {
            return archivos;
        }
        try (Stream<Path> contenido = Files.list(carpeta)) {     // el Stream abre el directorio: se cierra siempre
            contenido.filter(p -> p.getFileName().toString().endsWith(".csv")).forEach(archivos::add);
        } catch (IOException e) {
            throw new ArchivoException("No se pudo listar la carpeta " + carpeta + ": " + e.getMessage(), e);
        }
        return Ordenamiento.mergeSort(archivos, Comparator.comparing(p -> p.getFileName().toString()));
    }

    /** Recupera el contenido de un reporte: la primera fila es el encabezado. */
    public List<String[]> abrir(Path archivo) {
        return ExportadorCSV.leer(archivo);
    }

    public Path getCarpeta() {
        return carpeta;
    }

    /** "Nueva Cordoba" -> "nueva-cordoba": nombre de archivo sin espacios ni tildes. */
    private static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }
}
