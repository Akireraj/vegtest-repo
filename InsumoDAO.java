package com.vetgest.dao;

import com.vetgest.config.ConexionBD;
import com.vetgest.modelo.Insumo;
import com.vetgest.modelo.TipoMovimiento;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos para "insumo" y, de forma acoplada, para
 * "mov_inventario" (ambas tablas conforman el modulo de Inventario;
 * ver seccion 8.1 del informe, que solo menciona InsumoDAO).
 */
public class InsumoDAO {

    public Optional<Insumo> buscarPorId(int idInsumo) throws SQLException {
        String sql = "SELECT * FROM insumo WHERE id_insumo = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idInsumo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Insumo> listarTodos() throws SQLException {
        String sql = "SELECT * FROM insumo ORDER BY nombre";
        List<Insumo> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** RF08: insumos cuyo stock actual esta en el minimo o por debajo. */
    public List<Insumo> listarAlertasStockMinimo() throws SQLException {
        String sql = "SELECT * FROM insumo WHERE stock_actual <= stock_minimo ORDER BY nombre";
        List<Insumo> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * RF06 / RF07: registra un movimiento de inventario (CONSUMO o
     * REPOSICION) y actualiza el stock del insumo en una unica
     * transaccion. idHistoria puede ser null cuando el movimiento es
     * una reposicion (no proviene de una atencion clinica).
     *
     * @return el stock resultante despues del movimiento.
     */
    public int registrarMovimiento(int idInsumo, Integer idHistoria, int cantidad, TipoMovimiento tipoMov) throws SQLException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        String sqlInsumo = "SELECT stock_actual FROM insumo WHERE id_insumo = ? FOR UPDATE";
        String sqlUpdate = "UPDATE insumo SET stock_actual = ? WHERE id_insumo = ?";
        String sqlMov = "INSERT INTO mov_inventario (id_insumo, id_historia, cantidad, tipo_mov) VALUES (?, ?, ?, ?)";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                int stockActual;
                try (PreparedStatement ps = con.prepareStatement(sqlInsumo)) {
                    ps.setInt(1, idInsumo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("No existe un insumo con id " + idInsumo);
                        }
                        stockActual = rs.getInt("stock_actual");
                    }
                }

                int nuevoStock = tipoMov == TipoMovimiento.CONSUMO
                        ? stockActual - cantidad
                        : stockActual + cantidad;

                if (nuevoStock < 0) {
                    throw new IllegalStateException("Stock insuficiente para registrar el consumo.");
                }

                try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                    ps.setInt(1, nuevoStock);
                    ps.setInt(2, idInsumo);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(sqlMov)) {
                    ps.setInt(1, idInsumo);
                    if (idHistoria != null) {
                        ps.setInt(2, idHistoria);
                    } else {
                        ps.setNull(2, Types.INTEGER);
                    }
                    ps.setInt(3, cantidad);
                    ps.setString(4, tipoMov.name());
                    ps.executeUpdate();
                }

                con.commit();
                return nuevoStock;
            } catch (SQLException | IllegalStateException e) {
                con.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                throw new SQLException(e.getMessage(), e);
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private Insumo mapear(ResultSet rs) throws SQLException {
        Insumo i = new Insumo();
        i.setIdInsumo(rs.getInt("id_insumo"));
        i.setNombre(rs.getString("nombre"));
        i.setStockActual(rs.getInt("stock_actual"));
        i.setStockMinimo(rs.getInt("stock_minimo"));
        Date venc = rs.getDate("vencimiento");
        i.setVencimiento(venc != null ? venc.toLocalDate() : null);
        return i;
    }
}
