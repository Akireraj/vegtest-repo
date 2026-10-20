package ar.edu.vetgest.dao.jdbc;

import ar.edu.vetgest.dao.InventarioDAO;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.modelo.AlertaStock;
import ar.edu.vetgest.modelo.Insumo;
import ar.edu.vetgest.modelo.LoteInsumo;
import ar.edu.vetgest.modelo.MovimientoInventario;
import ar.edu.vetgest.util.ConexionBD;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventarioDAOJdbc implements InventarioDAO {

    private static final String SQL_LOTE = "SELECT l.*, i.nombre AS insumo FROM lote_insumo l "
            + "JOIN insumo i ON i.id_insumo = l.id_insumo";

    /** La condicion final impide dejar stock negativo aunque otro usuario haya consumido en paralelo. */
    private static final String SQL_STOCK = "UPDATE lote_insumo SET cantidad_actual = cantidad_actual + ? "
            + "WHERE id_lote = ? AND cantidad_actual + ? >= 0";
    private static final String SQL_MOV = "INSERT INTO mov_inventario "
            + "(id_lote, id_historia, id_usuario, tipo_mov, cantidad, fecha) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_ALERTAS = "SELECT id_sucursal, sucursal, id_insumo, insumo, stock_vigente, "
            + "stock_minimo FROM v_alerta_stock_minimo";

    @Override
    public List<Insumo> listarInsumos() {
        List<Insumo> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM insumo ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Insumo(rs.getInt("id_insumo"), rs.getString("codigo"), rs.getString("nombre"),
                        rs.getString("unidad"), rs.getInt("stock_minimo")));
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("listar insumos", e);
        }
        return lista;
    }

    @Override
    public List<LoteInsumo> lotesConStock(int idInsumo, int idSucursal) {
        return consultarLotes(SQL_LOTE + " WHERE l.id_insumo = ? AND l.id_sucursal = ? AND l.cantidad_actual > 0 "
                + "ORDER BY l.fecha_vencimiento", idInsumo, idSucursal);
    }

    @Override
    public List<LoteInsumo> lotesDeSucursal(int idSucursal) {
        return consultarLotes(SQL_LOTE + " WHERE l.id_sucursal = ? ORDER BY i.nombre, l.fecha_vencimiento", idSucursal);
    }

    @Override
    public Optional<LoteInsumo> buscarLote(int idLote) {
        List<LoteInsumo> lotes = consultarLotes(SQL_LOTE + " WHERE l.id_lote = ?", idLote);
        return lotes.isEmpty() ? Optional.empty() : Optional.of(lotes.get(0));
    }

    /** Parametros variables (varargs): la misma consulta sirve para 1 o 2 filtros. */
    private List<LoteInsumo> consultarLotes(String sql, int... parametros) {
        List<LoteInsumo> lotes = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setInt(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LoteInsumo l = new LoteInsumo(rs.getInt("id_lote"), rs.getInt("id_insumo"), rs.getInt("id_sucursal"),
                            rs.getString("nro_lote"), rs.getDate("fecha_vencimiento").toLocalDate(),
                            rs.getInt("cantidad_actual"));
                    l.setNombreInsumo(rs.getString("insumo"));
                    lotes.add(l);
                }
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("consultar lotes", e);
        }
        return lotes;
    }

    @Override
    public int insertarLote(LoteInsumo lote) {
        String sql = "INSERT INTO lote_insumo (id_insumo, id_sucursal, nro_lote, fecha_vencimiento, cantidad_actual) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, lote.getIdInsumo());
            ps.setInt(2, lote.getIdSucursal());
            ps.setString(3, lote.getNroLote());
            ps.setDate(4, Date.valueOf(lote.getFechaVencimiento()));
            ps.setInt(5, lote.getCantidadActual());
            ps.executeUpdate();
            int id = CatalogoDAOJdbc.claveGenerada(ps);
            lote.asignarId(id);
            return id;
        } catch (SQLException e) {
            throw Jdbc.traducir("dar de alta el lote", e);
        }
    }

    /**
     * POLIMORFISMO: el mismo codigo registra consumos y reposiciones; cada
     * objeto informa su tipo, su delta y su historia asociada.
     */
    @Override
    public void registrarMovimientos(List<? extends MovimientoInventario> movimientos) {
        try (Connection cn = ConexionBD.obtener()) {
            cn.setAutoCommit(false);
            try (PreparedStatement psStock = cn.prepareStatement(SQL_STOCK);
                 PreparedStatement psMov = cn.prepareStatement(SQL_MOV)) {
                for (MovimientoInventario m : movimientos) {
                    psStock.setInt(1, m.getDelta());
                    psStock.setInt(2, m.getIdLote());
                    psStock.setInt(3, m.getDelta());
                    if (psStock.executeUpdate() != 1) {      // control de concurrencia optimista
                        throw new ReglaNegocioException("Stock insuficiente en el lote " + m.getIdLote()
                                + " (pudo ser consumido por otro usuario)");
                    }
                    psMov.setInt(1, m.getIdLote());
                    if (m.getIdHistoria() == null) {
                        psMov.setNull(2, Types.INTEGER);
                    } else {
                        psMov.setInt(2, m.getIdHistoria());
                    }
                    psMov.setInt(3, m.getIdUsuario());
                    psMov.setString(4, m.getTipo().name());
                    psMov.setInt(5, m.getCantidad());
                    psMov.setTimestamp(6, Timestamp.valueOf(m.getFecha()));
                    psMov.executeUpdate();
                }
                cn.commit();
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("registrar el movimiento de inventario", e);
        }
    }

    @Override
    public List<AlertaStock> alertasStockMinimo() {
        return consultarAlertas(SQL_ALERTAS + " ORDER BY sucursal, insumo");
    }

    @Override
    public Optional<AlertaStock> verificarAlerta(int idInsumo, int idSucursal) {
        List<AlertaStock> alertas = consultarAlertas(SQL_ALERTAS + " WHERE id_insumo = ? AND id_sucursal = ?",
                idInsumo, idSucursal);
        return alertas.isEmpty() ? Optional.empty() : Optional.of(alertas.get(0));
    }

    private List<AlertaStock> consultarAlertas(String sql, int... parametros) {
        List<AlertaStock> alertas = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setInt(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    alertas.add(new AlertaStock(rs.getInt("id_sucursal"), rs.getString("sucursal"),
                            rs.getInt("id_insumo"), rs.getString("insumo"), rs.getInt("stock_vigente"),
                            rs.getInt("stock_minimo")));
                }
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("consultar alertas de stock", e);
        }
        return alertas;
    }
}
