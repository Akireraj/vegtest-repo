package ar.edu.vetgest.archivo;

import ar.edu.vetgest.dao.memoria.BaseDatosMemoria;
import ar.edu.vetgest.dao.memoria.FabricaDAOMemoria;
import ar.edu.vetgest.excepcion.ArchivoException;
import ar.edu.vetgest.excepcion.DatoInvalidoException;
import ar.edu.vetgest.modelo.Sucursal;
import ar.edu.vetgest.servicio.Exportacion;
import ar.edu.vetgest.servicio.InventarioService;
import ar.edu.vetgest.servicio.ReporteService;
import ar.edu.vetgest.servicio.TurnoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Persistencia en archivos: guardar y recuperar reportes CSV (carpeta temporal de JUnit). */
class ArchivosTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Cordoba");

    @TempDir
    Path carpeta;

    @Test
    @DisplayName("CP-37 Guardar y recuperar un CSV conserva filas, columnas, tildes, comillas y punto y coma")
    void guardarYRecuperar() {
        String[] encabezado = {"Insumo", "Detalle", "Cantidad"};
        List<String[]> filas = new ArrayList<>();
        filas.add(new String[]{"Vacuna antirrábica", "dosis; mantener refrigerada", "6"});
        filas.add(new String[]{"Jeringa \"5 ml\"", "", "45"});
        Path archivo = carpeta.resolve("prueba.csv");

        assertEquals(2, ExportadorCSV.escribir(archivo, encabezado, filas));
        List<String[]> leidas = ExportadorCSV.leer(archivo);

        assertEquals(3, leidas.size());
        assertArrayEquals(encabezado, leidas.get(0));
        assertArrayEquals(filas.get(0), leidas.get(1));
        assertArrayEquals(filas.get(1), leidas.get(2));
    }

    @Test
    @DisplayName("CP-38 Una fila con distinta cantidad de columnas se rechaza y no se crea el archivo")
    void filaInconsistenteSeRechaza() {
        Path archivo = carpeta.resolve("invalido.csv");
        List<String[]> filas = new ArrayList<>();
        filas.add(new String[]{"solo una columna"});
        assertThrows(DatoInvalidoException.class,
                () -> ExportadorCSV.escribir(archivo, new String[]{"A", "B"}, filas));
        assertFalse(Files.exists(archivo));
    }

    @Test
    @DisplayName("CP-39 Leer un archivo inexistente lanza ArchivoException, que envuelve la IOException")
    void archivoInexistente() {
        ArchivoException e = assertThrows(ArchivoException.class,
                () -> ExportadorCSV.leer(carpeta.resolve("no_existe.csv")));
        assertInstanceOf(IOException.class, e.getCause());
        assertEquals("ARCHIVO", e.getCodigo());
    }

    @Test
    @DisplayName("CP-40 ReporteService guarda la agenda y las alertas y luego las recupera de la carpeta")
    void reporteServiceGuardaYRecupera() {
        Clock reloj = Clock.fixed(LocalDateTime.of(2026, 9, 24, 8, 0).atZone(ZONA).toInstant(), ZONA);
        FabricaDAOMemoria fabrica = new FabricaDAOMemoria(BaseDatosMemoria.conDatosDePrueba(reloj));
        TurnoService turnos = new TurnoService(fabrica.turnos(), fabrica.catalogo(), reloj);
        InventarioService inventario = new InventarioService(fabrica.inventario(), reloj);
        ReporteService reportes = new ReporteService(turnos, inventario, carpeta, reloj);
        Sucursal centro = fabrica.catalogo().listarSucursales().get(0);

        Exportacion agenda = reportes.exportarAgenda(centro, LocalDate.of(2026, 9, 28));
        Exportacion alertas = reportes.exportarAlertas();

        assertEquals("agenda_centro_2026-09-28.csv", agenda.archivo().getFileName().toString());
        assertEquals(2, agenda.filas());
        assertEquals(5, alertas.filas());
        List<Path> guardados = reportes.reportesGuardados();
        assertEquals(List.of(agenda.archivo(), alertas.archivo()), guardados);   // ordenados por nombre
        List<String[]> recuperado = reportes.abrir(agenda.archivo());
        assertEquals(9, recuperado.get(0).length);
        assertEquals("CONFIRMADO", recuperado.get(1)[8]);
    }
}
