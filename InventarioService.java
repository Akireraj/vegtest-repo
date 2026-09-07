package com.vetgest.servicio;

import com.vetgest.dao.InsumoDAO;
import com.vetgest.modelo.Insumo;
import com.vetgest.modelo.TipoMovimiento;

import java.sql.SQLException;
import java.util.List;

/**
 * Reglas de negocio del modulo de Inventario. Implementa RF06, RF07 y
 * RF08, y el caso de uso "Registrar consumo de insumos" del informe.
 */
public class InventarioService {

    private final InsumoDAO insumoDAO = new InsumoDAO();

    /**
     * RF06: descuenta del inventario los insumos utilizados durante una
     * atencion, asociandolos a la historia clinica correspondiente.
     * Flujo alternativo 1a: si la cantidad es <= 0, se rechaza la
     * operacion (validado tambien en InsumoDAO).
     *
     * @return true si, tras el consumo, el insumo quedo en alerta de
     *         stock minimo (RF08).
     */
    public boolean registrarConsumo(int idInsumo, int idHistoria, int cantidad) throws SQLException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad consumida debe ser mayor a cero.");
        }
        Insumo insumo = insumoDAO.buscarPorId(idInsumo)
                .orElseThrow(() -> new IllegalArgumentException("No existe un insumo con id " + idInsumo));

        int nuevoStock = insumoDAO.registrarMovimiento(idInsumo, idHistoria, cantidad, TipoMovimiento.CONSUMO);
        return nuevoStock <= insumo.getStockMinimo();
    }

    /** RF07: registra una reposicion de stock (no asociada a una historia clinica). */
    public void registrarReposicion(int idInsumo, int cantidad) throws SQLException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad repuesta debe ser mayor a cero.");
        }
        insumoDAO.registrarMovimiento(idInsumo, null, cantidad, TipoMovimiento.REPOSICION);
    }

    /** RF08: lista los insumos cuyo stock actual esta en el minimo o por debajo. */
    public List<Insumo> consultarAlertasStockMinimo() throws SQLException {
        return insumoDAO.listarAlertasStockMinimo();
    }

    public List<Insumo> listarInsumos() throws SQLException {
        return insumoDAO.listarTodos();
    }
}
