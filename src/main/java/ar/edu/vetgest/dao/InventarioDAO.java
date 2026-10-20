package ar.edu.vetgest.dao;

import ar.edu.vetgest.modelo.AlertaStock;
import ar.edu.vetgest.modelo.Insumo;
import ar.edu.vetgest.modelo.LoteInsumo;
import ar.edu.vetgest.modelo.MovimientoInventario;

import java.util.List;
import java.util.Optional;

/** Persistencia de inventario por lotes (RF06 a RF08). */
public interface InventarioDAO {

    List<Insumo> listarInsumos();

    List<LoteInsumo> lotesConStock(int idInsumo, int idSucursal);

    List<LoteInsumo> lotesDeSucursal(int idSucursal);

    Optional<LoteInsumo> buscarLote(int idLote);

    int insertarLote(LoteInsumo lote);

    /** Aplica todos los movimientos de forma atomica: se confirman todos o ninguno. */
    void registrarMovimientos(List<? extends MovimientoInventario> movimientos);

    List<AlertaStock> alertasStockMinimo();

    Optional<AlertaStock> verificarAlerta(int idInsumo, int idSucursal);
}
