package ar.edu.vetgest.modelo;

import ar.edu.vetgest.excepcion.DatoInvalidoException;
import ar.edu.vetgest.excepcion.StockInsuficienteException;
import ar.edu.vetgest.excepcion.TransicionInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModeloTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 9, 24, 10, 0);

    @Test
    @DisplayName("Polimorfismo: consumo resta y reposicion suma sobre el mismo metodo aplicarA")
    void movimientosPolimorficos() {
        List<MovimientoInventario> movimientos = List.of(new Consumo(1, 5, 3, 4, AHORA), new Reposicion(1, 6, 10, AHORA));
        int stock = 5;
        for (MovimientoInventario m : movimientos) {
            stock = m.aplicarA(stock);
        }
        assertEquals(11, stock);
        assertEquals(TipoMovimiento.CONSUMO, movimientos.get(0).getTipo());
        assertEquals(5, movimientos.get(0).getIdHistoria());
        assertEquals(null, movimientos.get(1).getIdHistoria());
        assertThrows(StockInsuficienteException.class, () -> new Consumo(1, 5, 3, 9, AHORA).aplicarA(2));
        assertThrows(DatoInvalidoException.class, () -> new Reposicion(1, 6, 0, AHORA));
    }

    @Test
    @DisplayName("Metodo de fabrica: cada especie crea su subclase con su duracion sugerida")
    void mascotasPorEspecie() {
        Mascota exotico = Mascota.crear(Especie.EXOTICO, 1, 1, "Pancho", "Conejo", 'm', LocalDate.of(2023, 1, 9));
        assertInstanceOf(Exotico.class, exotico);
        assertEquals(45, exotico.getDuracionConsultaMin());
        assertEquals(3, exotico.edadEnAnios(LocalDate.of(2026, 9, 24)));
        assertEquals('M', exotico.getSexo());
        assertThrows(DatoInvalidoException.class,
                () -> Mascota.crear(Especie.CANINO, 1, 1, "Toby", null, 'X', null));
    }

    @Test
    @DisplayName("Herencia y polimorfismo en Persona: toString usa getTipo() y getDetalle() de cada subclase")
    void personas() {
        Persona cliente = new Cliente(1, "30111222", "Laura", "Fernandez", "351-5551001", null);
        Persona vet = new Veterinario(1, 1, "MP-4521", "Andrea", "Gomez", null);
        assertTrue(cliente.toString().startsWith("[Cliente] Fernandez, Laura - DNI 30111222"));
        assertEquals("[Veterinario/a] Gomez, Andrea - Mat. MP-4521 - Clinica general", vet.toString());
        assertThrows(DatoInvalidoException.class, () -> new Cliente("12AB", "A", "B", "1", null));
        assertThrows(DatoInvalidoException.class, () -> new Cliente("12345678", "A", "B", "1", "sin-arroba"));
    }

    @Test
    @DisplayName("Encapsulamiento del turno: estados, superposicion y validaciones")
    void turno() {
        Turno t = new Turno(1, 1, 1, 1, AHORA, 30, "Control");
        assertTrue(t.seSuperponeCon(AHORA.plusMinutes(15), 30));
        assertFalse(t.seSuperponeCon(AHORA.plusMinutes(30), 30));      // contiguo
        t.cambiarEstado(EstadoTurno.CONFIRMADO);
        t.cambiarEstado(EstadoTurno.ATENDIDO);
        assertThrows(TransicionInvalidaException.class, () -> t.cambiarEstado(EstadoTurno.CANCELADO));
        assertThrows(DatoInvalidoException.class, () -> new Turno(1, 1, 1, 1, AHORA, 5, "Muy corto"));
        assertThrows(DatoInvalidoException.class, () -> new Turno(1, 1, 1, 1, AHORA, 30, "  "));
        assertThrows(DatoInvalidoException.class,
                () -> new HistoriaClinica(1, BigDecimal.ZERO, "Sano", "Ninguno", null));
    }
}
