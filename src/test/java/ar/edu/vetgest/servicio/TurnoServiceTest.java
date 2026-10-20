package ar.edu.vetgest.servicio;

import ar.edu.vetgest.dao.memoria.BaseDatosMemoria;
import ar.edu.vetgest.dao.memoria.FabricaDAOMemoria;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.excepcion.TransicionInvalidaException;
import ar.edu.vetgest.modelo.EstadoTurno;
import ar.edu.vetgest.modelo.Turno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Reglas RF01 a RF03 con DAO en memoria y reloj fijo (jueves 24/09/2026 08:00). */
class TurnoServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Cordoba");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 9, 24, 8, 0);

    private TurnoService servicio;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        FabricaDAOMemoria fabrica = new FabricaDAOMemoria(BaseDatosMemoria.conDatosDePrueba(reloj));
        servicio = new TurnoService(fabrica.turnos(), fabrica.catalogo(), reloj);
    }

    private static LocalDateTime dia28(int hora, int minuto) {
        return LocalDateTime.of(2026, 9, 28, hora, minuto);
    }

    @Test
    @DisplayName("CP-01 Registrar un turno valido lo deja RESERVADO")
    void registrarTurnoValido() {
        Turno t = servicio.registrar(2, 3, 2, 2, dia28(10, 0), null, "Control");
        assertEquals(EstadoTurno.RESERVADO, t.getEstado());
        assertEquals(11, t.getIdTurno());
    }

    @Test
    @DisplayName("CP-02 Turno 9:15 superpuesto a otro de 9:00 a 9:30 del mismo veterinario")
    void turnoSuperpuestoSeRechaza() {
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(2, 1, 1, 1, dia28(9, 15), null, "Control"));
        assertTrue(e.getMessage().contains("RF02"));
    }

    @Test
    @DisplayName("CP-03 Turno contiguo (comienza cuando termina el anterior) se acepta")
    void turnoContiguoSeAcepta() {
        Turno t = servicio.registrar(2, 1, 1, 1, dia28(10, 0), null, "Control");
        assertEquals(EstadoTurno.RESERVADO, t.getEstado());
    }

    @Test
    @DisplayName("CP-04 El mismo horario con otro veterinario se acepta")
    void mismoHorarioOtroVeterinario() {
        Turno t = servicio.registrar(2, 2, 1, 1, dia28(9, 0), null, "Control");
        assertEquals(2, t.getIdVeterinario());
    }

    @Test
    @DisplayName("CP-05 Cancelar un turno libera su horario")
    void cancelarLiberaHorario() {
        servicio.cancelar(9);
        Turno t = servicio.registrar(2, 1, 1, 1, dia28(9, 30), null, "Control");
        assertEquals(dia28(9, 30), t.getFechaHora());
    }

    @Test
    @DisplayName("CP-06 Turno en fecha pasada se rechaza")
    void fechaPasadaSeRechaza() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(2, 1, 1, 1, LocalDateTime.of(2026, 9, 23, 10, 0), null, "Control"));
    }

    @Test
    @DisplayName("CP-07 CANCELADO -> CONFIRMADO es una transicion invalida")
    void transicionInvalida() {
        TransicionInvalidaException e = assertThrows(TransicionInvalidaException.class, () -> servicio.confirmar(6));
        assertEquals(EstadoTurno.CANCELADO, e.getDesde());
    }

    @Test
    @DisplayName("CP-08 Reprogramar solapando solo con el propio horario se acepta")
    void reprogramarExcluyePropioTurno() {
        Turno t = servicio.reprogramar(9, dia28(9, 40));
        assertEquals(dia28(9, 40), t.getFechaHora());
        assertEquals(dia28(9, 40), servicio.obtener(9).getFechaHora());
    }

    @Test
    @DisplayName("CP-20 Domingo o fuera del horario de atencion se rechaza")
    void fueraDeHorario() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(2, 1, 1, 1, LocalDateTime.of(2026, 9, 27, 10, 0), null, "Domingo"));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(2, 1, 1, 1, dia28(19, 45), null, "Termina 20:15"));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(2, 1, 1, 1, dia28(7, 30), null, "Antes de abrir"));
    }

    @Test
    @DisplayName("CP-21 Sin duracion indicada se usa la sugerida por la especie (polimorfismo)")
    void duracionSugeridaPorEspecie() {
        Turno exotico = servicio.registrar(6, 6, 3, 1, LocalDateTime.of(2026, 9, 30, 10, 0), null, "Control");
        Turno canino = servicio.registrar(5, 5, 3, 1, LocalDateTime.of(2026, 9, 30, 10, 0), null, "Control");
        assertEquals(45, exotico.getDuracionMin());
        assertEquals(30, canino.getDuracionMin());
    }

    @Test
    @DisplayName("CP-22 Deshacer cancela la ultima reserva de la sesion (pila)")
    void deshacerUltimaReserva() {
        Turno primero = servicio.registrar(2, 3, 2, 2, dia28(10, 0), null, "Primero");
        Turno segundo = servicio.registrar(4, 3, 2, 2, dia28(11, 0), null, "Segundo");
        assertEquals(segundo.getIdTurno(), servicio.deshacerUltimaReserva().getIdTurno());
        assertEquals(primero.getIdTurno(), servicio.deshacerUltimaReserva().getIdTurno());
        assertEquals(EstadoTurno.CANCELADO, servicio.obtener(primero.getIdTurno()).getEstado());
        assertThrows(ReglaNegocioException.class, servicio::deshacerUltimaReserva);
    }

    @Test
    @DisplayName("CP-23 Primer horario libre salta los turnos ocupados")
    void primerHorarioLibre() {
        Optional<LocalDateTime> libre = servicio.buscarPrimerHorarioLibre(1, LocalDate.of(2026, 9, 28), 30,
                LocalTime.of(9, 0));
        assertEquals(Optional.of(dia28(10, 0)), libre);
    }

    @Test
    @DisplayName("CP-27 No se marca AUSENTE un turno que aun no comenzo")
    void ausenteAntesDeTiempo() {
        assertThrows(ReglaNegocioException.class, () -> servicio.marcarAusente(7));
    }
}
