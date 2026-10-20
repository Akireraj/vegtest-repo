package ar.edu.vetgest.dao.jdbc;

import ar.edu.vetgest.dao.HistoriaClinicaDAO;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.modelo.EntradaHistorial;
import ar.edu.vetgest.modelo.HistoriaClinica;
import ar.edu.vetgest.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HistoriaClinicaDAOJdbc implements HistoriaClinicaDAO {

    private static final String SQL_HISTORIA = "INSERT INTO historia_clinica "
            + "(id_turno, peso_kg, diagnostico, tratamiento, observaciones) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_ATENDIDO = "UPDATE turno SET estado = 'ATENDIDO' "
            + "WHERE id_turno = ? AND estado = 'CONFIRMADO'";
    private static final String SQL_HISTORIAL = "SELECT DATE(t.fecha_hora) AS fecha, s.nombre AS sucursal, "
            + "CONCAT(v.apellido, ', ', v.nombre) AS veterinario, h.peso_kg, h.diagnostico, h.tratamiento "
            + "FROM historia_clinica h JOIN turno t ON t.id_turno = h.id_turno "
            + "JOIN sucursal s ON s.id_sucursal = t.id_sucursal "
            + "JOIN veterinario v ON v.id_veterinario = t.id_veterinario "
            + "WHERE t.id_mascota = ? ORDER BY t.fecha_hora DESC";

    /** Transaccion explicita: la historia y el cambio de estado se confirman juntos o no se confirman. */
    @Override
    public int registrarAtencion(HistoriaClinica h) {
        try (Connection cn = ConexionBD.obtener()) {
            cn.setAutoCommit(false);
            try (PreparedStatement psHistoria = cn.prepareStatement(SQL_HISTORIA, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psTurno = cn.prepareStatement(SQL_ATENDIDO)) {
                psHistoria.setInt(1, h.getIdTurno());
                psHistoria.setBigDecimal(2, h.getPesoKg());
                psHistoria.setString(3, h.getDiagnostico());
                psHistoria.setString(4, h.getTratamiento());
                psHistoria.setString(5, h.getObservaciones());
                psHistoria.executeUpdate();
                int idHistoria = CatalogoDAOJdbc.claveGenerada(psHistoria);

                psTurno.setInt(1, h.getIdTurno());
                if (psTurno.executeUpdate() != 1) {
                    throw new ReglaNegocioException("El turno " + h.getIdTurno() + " ya no esta CONFIRMADO");
                }
                cn.commit();
                h.asignarId(idHistoria);
                return idHistoria;
            } catch (SQLException | RuntimeException e) {
                cn.rollback();          // se deshace tambien el INSERT de la historia
                throw e;
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("registrar la atencion", e);
        }
    }

    @Override
    public boolean existeParaTurno(int idTurno) {
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement("SELECT 1 FROM historia_clinica WHERE id_turno = ?")) {
            ps.setInt(1, idTurno);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("verificar la historia clinica", e);
        }
    }

    @Override
    public List<EntradaHistorial> historialDeMascota(int idMascota) {
        List<EntradaHistorial> historial = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_HISTORIAL)) {
            ps.setInt(1, idMascota);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    historial.add(new EntradaHistorial(rs.getDate("fecha").toLocalDate(), rs.getString("sucursal"),
                            rs.getString("veterinario"), rs.getBigDecimal("peso_kg"), rs.getString("diagnostico"),
                            rs.getString("tratamiento")));
                }
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("consultar el historial", e);
        }
        return historial;
    }
}
