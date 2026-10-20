package ar.edu.vetgest.servicio;

import ar.edu.vetgest.dao.memoria.BaseDatosMemoria;
import ar.edu.vetgest.dao.memoria.FabricaDAOMemoria;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Sala de espera (cola FIFO) el lunes 28/09/2026 a las 08:30. */
class SalaEsperaServiceTest {

    private SalaEsperaService sala;
    private TurnoService turnos;

    @BeforeEach
    void preparar() {
        ZoneId zona = ZoneId.of("America/Argentina/Cordoba");
        Clock reloj = Clock.fixed(LocalDateTime.of(2026, 9, 28, 8, 30).atZone(zona).toInstant(), zona);
        FabricaDAOMemoria fabrica = new FabricaDAOMemoria(BaseDatosMemoria.conDatosDePrueba(reloj));
        sala = new SalaEsperaService(fabrica.turnos(), reloj);
        turnos = new TurnoService(fabrica.turnos(), fabrica.catalogo(), reloj);
    }

    @Test
    @DisplayName("CP-29 Los pacientes se llaman en orden de llegada (FIFO)")
    void ordenDeLlegada() {
        turnos.confirmar(9);
        assertEquals(1, sala.registrarLlegada(9, 1));
        assertEquals(2, sala.registrarLlegada(7, 1));
        assertEquals(9, sala.llamarSiguiente(1).getIdTurno());
        assertEquals(7, sala.llamarSiguiente(1).getIdTurno());
        assertThrows(ReglaNegocioException.class, () -> sala.llamarSiguiente(1));
    }

    @Test
    @DisplayName("CP-30 Solo ingresan turnos CONFIRMADOS, de la sucursal y una sola vez")
    void validacionesDeLlegada() {
        assertThrows(ReglaNegocioException.class, () -> sala.registrarLlegada(9, 1));   // RESERVADO
        assertThrows(ReglaNegocioException.class, () -> sala.registrarLlegada(7, 2));   // otra sucursal
        sala.registrarLlegada(7, 1);
        assertThrows(ReglaNegocioException.class, () -> sala.registrarLlegada(7, 1));   // duplicado
        assertEquals(1, sala.totalEnEspera());
    }
}
