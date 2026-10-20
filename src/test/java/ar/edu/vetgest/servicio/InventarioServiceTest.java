package ar.edu.vetgest.servicio;

import ar.edu.vetgest.dao.memoria.BaseDatosMemoria;
import ar.edu.vetgest.dao.memoria.FabricaDAOMemoria;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.excepcion.StockInsuficienteException;
import ar.edu.vetgest.modelo.AlertaStock;
import ar.edu.vetgest.modelo.Consumo;
import ar.edu.vetgest.modelo.ConsumoLote;
import ar.edu.vetgest.modelo.LoteInsumo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF06 a RF08: FEFO, consumo atomico, reposicion y alertas. */
class InventarioServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 9, 24);
    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Cordoba");

    private FabricaDAOMemoria fabrica;
    private InventarioService servicio;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(HOY.atTime(10, 0).atZone(ZONA).toInstant(), ZONA);
        fabrica = new FabricaDAOMemoria(BaseDatosMemoria.conDatosDePrueba(reloj));
        servicio = new InventarioService(fabrica.inventario(), reloj);
    }

    private static LoteInsumo lote(int id, String vence, int cantidad) {
        return new LoteInsumo(id, 1, 1, "L" + id, LocalDate.parse(vence), cantidad);
    }

    @Test
    @DisplayName("CP-09 Descuenta del lote mas proximo a vencer")
    void fefoEligeElMasProximo() {
        List<ConsumoLote> plan = InventarioService.planificarFEFO(
                List.of(lote(1, "2026-12-01", 5), lote(2, "2026-11-01", 5)), 3, HOY);
        assertEquals(1, plan.size());
        assertEquals(2, plan.get(0).getIdLote());
        assertEquals(3, plan.get(0).getCantidad());
    }

    @Test
    @DisplayName("CP-10 Si un lote no alcanza, reparte entre lotes (2 + 3)")
    void fefoReparte() {
        List<ConsumoLote> plan = InventarioService.planificarFEFO(
                List.of(lote(1, "2027-01-01", 4), lote(2, "2026-10-10", 2)), 5, HOY);
        assertEquals(2, plan.get(0).getCantidad());
        assertEquals(1, plan.get(1).getIdLote());
        assertEquals(3, plan.get(1).getCantidad());
    }

    @Test
    @DisplayName("CP-11 Se ignora un lote vencido aunque tenga stock")
    void fefoIgnoraVencidos() {
        List<ConsumoLote> plan = InventarioService.planificarFEFO(
                List.of(lote(1, "2026-09-10", 25), lote(2, "2027-12-31", 11)), 1, HOY);
        assertEquals(2, plan.get(0).getIdLote());
    }

    @Test
    @DisplayName("CP-12 Consumo mayor al stock vigente se rechaza")
    void stockInsuficiente() {
        StockInsuficienteException e = assertThrows(StockInsuficienteException.class,
                () -> InventarioService.planificarFEFO(List.of(lote(1, "2026-09-10", 25), lote(2, "2027-01-01", 3)),
                        4, HOY));
        assertEquals(3, e.getDisponible());
    }

    @Test
    @DisplayName("CP-13 Cantidad de consumo igual a 0 se rechaza")
    void cantidadCero() {
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarConsumo(2, 6, 1, 0, 3));
    }

    @Test
    @DisplayName("CP-24 Consumir la ultima ketamina genera alerta de stock minimo")
    void consumoGeneraAlerta() {
        ResultadoConsumo r = servicio.registrarConsumo(2, 6, 1, 1, 4);
        assertTrue(r.generoAlerta());
        assertEquals(0, r.alerta().getStockVigente());
    }

    @Test
    @DisplayName("CP-25 Una reposicion levanta la alerta de vacuna antirrabica en Nueva Cordoba")
    void reposicionLevantaAlerta() {
        assertTrue(contieneAlerta(2, 1));
        LoteInsumo lote = servicio.registrarReposicion(2, 20, 6);
        assertEquals(26, lote.getCantidadActual());
        assertFalse(contieneAlerta(2, 1));
    }

    @Test
    @DisplayName("CP-26 Los movimientos son atomicos: si uno falla no se aplica ninguno")
    void movimientosAtomicos() {
        LocalDateTime ahora = HOY.atTime(10, 0);
        assertThrows(StockInsuficienteException.class, () -> fabrica.inventario().registrarMovimientos(List.of(
                new Consumo(8, 2, 4, 1, ahora), new Consumo(9, 2, 4, 5, ahora))));
        assertEquals(120, fabrica.inventario().buscarLote(8).orElseThrow().getCantidadActual());
    }

    @Test
    @DisplayName("CP-28 Las alertas se ordenan por criticidad (menor cobertura primero)")
    void alertasOrdenadas() {
        List<AlertaStock> alertas = servicio.alertasPorCriticidad();
        for (int i = 1; i < alertas.size(); i++) {
            assertTrue(alertas.get(i - 1).getCobertura() <= alertas.get(i).getCobertura());
        }
    }

    private boolean contieneAlerta(int idSucursal, int idInsumo) {
        return servicio.alertasPorCriticidad().stream()
                .anyMatch(a -> a.getIdSucursal() == idSucursal && a.getIdInsumo() == idInsumo);
    }
}
