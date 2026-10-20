package ar.edu.vetgest.servicio;

import ar.edu.vetgest.algoritmos.Ordenamiento;
import ar.edu.vetgest.dao.InventarioDAO;
import ar.edu.vetgest.excepcion.EntidadNoEncontradaException;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.excepcion.StockInsuficienteException;
import ar.edu.vetgest.modelo.AlertaStock;
import ar.edu.vetgest.modelo.Consumo;
import ar.edu.vetgest.modelo.ConsumoLote;
import ar.edu.vetgest.modelo.Insumo;
import ar.edu.vetgest.modelo.LoteInsumo;
import ar.edu.vetgest.modelo.MovimientoInventario;
import ar.edu.vetgest.modelo.Reposicion;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** CU08 consumo con FEFO (RF06), CU10 reposicion (RF07) y CU09/CU11 alertas (RF08). */
public class InventarioService {

    private final InventarioDAO inventarioDAO;
    private final Clock reloj;

    public InventarioService(InventarioDAO inventarioDAO, Clock reloj) {
        this.inventarioDAO = inventarioDAO;
        this.reloj = reloj;
    }

    public ResultadoConsumo registrarConsumo(int idHistoria, int idInsumo, int idSucursal, int cantidad, int idUsuario) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad a consumir debe ser mayor a cero");
        }
        List<ConsumoLote> plan = planificarFEFO(inventarioDAO.lotesConStock(idInsumo, idSucursal), cantidad,
                LocalDate.now(reloj));
        LocalDateTime ahora = LocalDateTime.now(reloj);
        List<MovimientoInventario> movimientos = new ArrayList<>();
        for (ConsumoLote parte : plan) {
            movimientos.add(new Consumo(parte.getIdLote(), idHistoria, idUsuario, parte.getCantidad(), ahora));
        }
        inventarioDAO.registrarMovimientos(movimientos);
        AlertaStock alerta = inventarioDAO.verificarAlerta(idInsumo, idSucursal).orElse(null);   // CU09 extiende CU08
        return new ResultadoConsumo(plan, alerta);
    }

    /**
     * FEFO (First Expired, First Out): descarta lotes vencidos o vacios,
     * ordena los vigentes por vencimiento (insercion) y reparte la cantidad
     * desde el mas proximo a vencer.
     */
    static List<ConsumoLote> planificarFEFO(List<LoteInsumo> lotes, int cantidad, LocalDate hoy) {
        List<LoteInsumo> vigentes = new ArrayList<>();
        for (LoteInsumo l : lotes) {
            if (l.estaVigente(hoy) && l.getCantidadActual() > 0) {
                vigentes.add(l);
            }
        }
        Ordenamiento.insercion(vigentes, Comparator.comparing(LoteInsumo::getFechaVencimiento));

        int disponible = 0;
        for (LoteInsumo l : vigentes) {
            disponible += l.getCantidadActual();
        }
        if (disponible < cantidad) {
            throw new StockInsuficienteException(disponible, cantidad);
        }

        List<ConsumoLote> plan = new ArrayList<>();
        int restante = cantidad;
        for (LoteInsumo l : vigentes) {
            if (restante == 0) {
                break;
            }
            int tomar = Math.min(restante, l.getCantidadActual());
            plan.add(new ConsumoLote(l.getIdLote(), tomar));
            restante -= tomar;
        }
        return plan;
    }

    /** CU10: suma la cantidad recibida a un lote existente, en una transaccion. */
    public LoteInsumo registrarReposicion(int idLote, int cantidad, int idUsuario) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad repuesta debe ser mayor a cero");
        }
        LoteInsumo lote = buscarLote(idLote);
        if (!lote.estaVigente(LocalDate.now(reloj))) {
            throw new ReglaNegocioException("No se puede reponer el lote " + lote.getNroLote() + ": esta vencido");
        }
        inventarioDAO.registrarMovimientos(List.of(new Reposicion(idLote, idUsuario, cantidad, LocalDateTime.now(reloj))));
        return buscarLote(idLote);
    }

    /** CU10, precondicion: da de alta el lote si no existia en la sucursal y registra la reposicion. */
    public LoteInsumo ingresarLoteNuevo(int idInsumo, int idSucursal, String nroLote, LocalDate vencimiento,
                                        int cantidad, int idUsuario) {
        if (!vencimiento.isAfter(LocalDate.now(reloj))) {
            throw new ReglaNegocioException("No se puede ingresar un lote vencido o que vence hoy");
        }
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad recibida debe ser mayor a cero");
        }
        LoteInsumo lote = new LoteInsumo(null, idInsumo, idSucursal, nroLote, vencimiento, 0);
        inventarioDAO.insertarLote(lote);
        return registrarReposicion(lote.getIdLote(), cantidad, idUsuario);
    }

    /** Alertas de la red ordenadas por criticidad: primero las de menor cobertura del minimo. */
    public List<AlertaStock> alertasPorCriticidad() {
        return Ordenamiento.mergeSort(inventarioDAO.alertasStockMinimo(),
                Comparator.comparingDouble(AlertaStock::getCobertura).thenComparing(AlertaStock::getSucursal));
    }

    /** Lotes con stock vencidos o que vencen dentro de {@code dias} dias, ordenados por vencimiento. */
    public List<LoteInsumo> lotesPorVencer(int idSucursal, int dias) {
        LocalDate limite = LocalDate.now(reloj).plusDays(dias);
        List<LoteInsumo> resultado = new ArrayList<>();
        for (LoteInsumo l : inventarioDAO.lotesDeSucursal(idSucursal)) {
            if (l.getCantidadActual() > 0 && !l.getFechaVencimiento().isAfter(limite)) {
                resultado.add(l);
            }
        }
        return Ordenamiento.mergeSort(resultado, Comparator.comparing(LoteInsumo::getFechaVencimiento));
    }

    public List<LoteInsumo> lotesDeSucursal(int idSucursal) {
        return inventarioDAO.lotesDeSucursal(idSucursal);
    }

    public List<Insumo> insumos() {
        return inventarioDAO.listarInsumos();
    }

    public LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    private LoteInsumo buscarLote(int idLote) {
        return inventarioDAO.buscarLote(idLote).orElseThrow(() -> new EntidadNoEncontradaException("lote", idLote));
    }
}
