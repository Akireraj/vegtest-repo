package ar.edu.vetgest.servicio;

import ar.edu.vetgest.dao.memoria.BaseDatosMemoria;
import ar.edu.vetgest.dao.memoria.FabricaDAOMemoria;
import ar.edu.vetgest.modelo.EstadoTurno;
import ar.edu.vetgest.modelo.Veterinario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Arreglos y ArrayList: grilla boolean[][] e int[] construidos desde la agenda.
 * Datos del script 02: el 28/09/2026 la Dra. Gomez (id 1) tiene turnos de 09:00 y 09:30 en Centro.
 */
class GrillaDisponibilidadTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Cordoba");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 9, 24, 8, 0);
    private static final LocalDate DIA_28 = LocalDate.of(2026, 9, 28);
    private static final int CENTRO = 1;
    private static final int DRA_GOMEZ = 1;
    private static final int DR_PAEZ = 2;

    private TurnoService servicio;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        FabricaDAOMemoria fabrica = new FabricaDAOMemoria(BaseDatosMemoria.conDatosDePrueba(reloj));
        servicio = new TurnoService(fabrica.turnos(), fabrica.catalogo(), reloj);
    }

    @Test
    @DisplayName("CP-31 La grilla tiene una fila por veterinario de la sucursal y 48 franjas de 15 minutos")
    void dimensionesDeLaGrilla() {
        GrillaDisponibilidad g = servicio.grilla(CENTRO, DIA_28);
        assertEquals(2, g.cantidadFilas());
        assertEquals(48, GrillaDisponibilidad.FRANJAS);
        assertEquals(48, g.filaComoTexto(0).length());
        assertEquals(LocalTime.of(19, 45), GrillaDisponibilidad.horaDeFranja(47));
    }

    @Test
    @DisplayName("CP-32 Los turnos activos ocupan sus franjas: Dra. Gomez de 09:00 a 10:00")
    void turnosActivosOcupanFranjas() {
        GrillaDisponibilidad g = servicio.grilla(CENTRO, DIA_28);
        int fila = g.filaDe(DRA_GOMEZ);
        assertFalse(g.estaOcupada(fila, 3));            // 08:45
        for (int franja = 4; franja <= 7; franja++) {   // 09:00 a 10:00
            assertTrue(g.estaOcupada(fila, franja));
        }
        assertFalse(g.estaOcupada(fila, 8));            // 10:00
        assertEquals(8, g.porcentajeOcupacion(fila));   // 4 de 48 franjas
    }

    @Test
    @DisplayName("CP-33 Los horarios libres de 30 min (ArrayList) saltan las franjas ocupadas")
    void horariosLibresSaltanOcupadas() {
        List<LocalTime> libres = servicio.grilla(CENTRO, DIA_28).horariosLibres(DRA_GOMEZ, 30);
        assertTrue(libres.contains(LocalTime.of(8, 30)));
        assertFalse(libres.contains(LocalTime.of(8, 45)));   // terminaria 09:15
        assertFalse(libres.contains(LocalTime.of(9, 45)));
        assertTrue(libres.contains(LocalTime.of(10, 0)));    // contiguo: valor limite
        assertEquals(LocalTime.of(19, 30), libres.get(libres.size() - 1));
        assertEquals(42, libres.size());                     // 47 inicios posibles - 5 bloqueados
    }

    @Test
    @DisplayName("CP-34 Un turno que no empieza en multiplo de 15 ocupa todas las franjas que toca")
    void turnoFueraDeGrillaOcupaFranjasQueToca() {
        servicio.registrar(2, DR_PAEZ, CENTRO, 1, DIA_28.atTime(11, 10), 30, "Control");   // 11:10 a 11:40
        GrillaDisponibilidad g = servicio.grilla(CENTRO, DIA_28);
        int fila = g.filaDe(DR_PAEZ);
        assertFalse(g.estaOcupada(fila, 11));                 // 10:45
        assertTrue(g.estaOcupada(fila, 12));                  // 11:00 a 11:15
        assertTrue(g.estaOcupada(fila, 13));
        assertTrue(g.estaOcupada(fila, 14));                  // 11:30 a 11:45
        assertFalse(g.estaOcupada(fila, 15));
    }

    @Test
    @DisplayName("CP-35 El contador por estado (int[]) cuenta la agenda y se entrega como copia defensiva")
    void contadorPorEstadoYCopias() {
        GrillaDisponibilidad g = servicio.grilla(CENTRO, DIA_28);
        int[] porEstado = g.getTurnosPorEstado();
        assertEquals(1, porEstado[EstadoTurno.CONFIRMADO.ordinal()]);
        assertEquals(1, porEstado[EstadoTurno.RESERVADO.ordinal()]);
        porEstado[EstadoTurno.CONFIRMADO.ordinal()] = 99;
        assertEquals(1, g.getTurnosPorEstado()[EstadoTurno.CONFIRMADO.ordinal()]);
        Veterinario[] filas = g.getVeterinarios();
        filas[0] = null;
        assertNotNull(g.getVeterinarios()[0]);
    }

    @Test
    @DisplayName("CP-36 En el dia actual no se ofrecen horarios que ya pasaron")
    void horariosDelDiaActualExcluyenPasados() {
        LocalDate hoy = AHORA.toLocalDate();
        List<LocalTime> libres = servicio.horariosLibres(servicio.grilla(CENTRO, hoy), DRA_GOMEZ, 30);
        assertEquals(LocalTime.of(8, 15), libres.get(0));    // son las 08:00
        assertEquals(46, libres.size());
    }
}
