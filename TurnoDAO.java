package com.vetgest.dao;

import com.vetgest.config.ConexionBD;
import com.vetgest.modelo.HistoriaClinica;
import com.vetgest.modelo.Turno;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos para "turno" y, de forma acoplada, para
 * "historia_clinica" (ver nota de diseño en HistoriaClinica.java).
 * Esta union de responsabilidades respeta el listado de DAOs de la
 * seccion 8.1 del informe (solo TurnoDAO e InsumoDAO).
 */
public class TurnoDAO {

    /**
     * RF02: existe superposicion si el mismo veterinario ya tiene un
     * turno NO cancelado en el mismo id_veterinario + fecha_hora.
     * idTurnoExcluir permite ignorar el propio turno al reprogramar
     * (puede ser null).
     */
    public boolean existeSuperposicion(int idVeterinario, LocalDateTime fechaHora, Integer idTurnoExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM turno WHERE id_veterinario = ? AND fecha_hora = ? "
                + "AND estado <> 'CANCELADO'"
                + (idTurnoExcluir != null ? " AND id_turno <> ?" : "");
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVeterinario);
            ps.setTimestamp(2, Timestamp.valueOf(fechaHora));
            if (idTurnoExcluir != null) {
                ps.setInt(3, idTurnoExcluir);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** RF01: registra un nuevo turno en estado RESERVADO. */
    public Turno crear(Turno turno) throws SQLException {
        String sql = "INSERT INTO turno (id_mascota, id_veterinario, fecha_hora, estado) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, turno.getIdMascota());
            ps.setInt(2, turno.getIdVeterinario());
            ps.setTimestamp(3, Timestamp.valueOf(turno.getFechaHora()));
            ps.setString(4, turno.getEstado().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    turno.setIdTurno(rs.getInt(1));
                }
            }
        }
        return turno;
    }

    public Optional<Turno> buscarPorId(int idTurno) throws SQLException {
        String sql = "SELECT * FROM turno WHERE id_turno = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTurno);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    /** RF03: cambia el estado del turno (confirmar / cancelar / atender). */
    public boolean actualizarEstado(int idTurno, Turno.Estado estado) throws SQLException {
        String sql = "UPDATE turno SET estado = ? WHERE id_turno = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, idTurno);
            return ps.executeUpdate() > 0;
        }
    }

    /** RF03: reprograma fecha/hora y vuelve a dejar el turno en RESERVADO. */
    public boolean reprogramar(int idTurno, LocalDateTime nuevaFechaHora) throws SQLException {
        String sql = "UPDATE turno SET fecha_hora = ?, estado = 'RESERVADO' WHERE id_turno = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(nuevaFechaHora));
            ps.setInt(2, idTurno);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Turno> listarPorVeterinarioYFecha(int idVeterinario, LocalDate fecha) throws SQLException {
        String sql = "SELECT * FROM turno WHERE id_veterinario = ? AND DATE(fecha_hora) = ? ORDER BY fecha_hora";
        List<Turno> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVeterinario);
            ps.setDate(2, java.sql.Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /**
     * RF04 + caso de uso "Registrar historia clinica": registra el
     * diagnostico/tratamiento de la atencion y marca el turno como
     * ATENDIDO, dentro de una unica transaccion.
     *
     * @return el id_historia generado (necesario para asociar el
     *         consumo de insumos via InventarioService).
     */
    public int registrarAtencion(int idTurno, String diagnostico, String tratamiento) throws SQLException {
        String sqlHistoria = "INSERT INTO historia_clinica (id_turno, diagnostico, tratamiento, fecha) VALUES (?, ?, ?, ?)";
        String sqlEstado = "UPDATE turno SET estado = 'ATENDIDO' WHERE id_turno = ?";
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                int idHistoria;
                try (PreparedStatement ps = con.prepareStatement(sqlHistoria, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idTurno);
                    ps.setString(2, diagnostico);
                    ps.setString(3, tratamiento);
                    ps.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        idHistoria = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(sqlEstado)) {
                    ps.setInt(1, idTurno);
                    ps.executeUpdate();
                }
                con.commit();
                return idHistoria;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public Optional<HistoriaClinica> buscarHistoriaPorTurno(int idTurno) throws SQLException {
        String sql = "SELECT * FROM historia_clinica WHERE id_turno = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTurno);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HistoriaClinica h = new HistoriaClinica();
                    h.setIdHistoria(rs.getInt("id_historia"));
                    h.setIdTurno(rs.getInt("id_turno"));
                    h.setDiagnostico(rs.getString("diagnostico"));
                    h.setTratamiento(rs.getString("tratamiento"));
                    h.setFecha(rs.getDate("fecha").toLocalDate());
                    return Optional.of(h);
                }
            }
        }
        return Optional.empty();
    }

    private Turno mapear(ResultSet rs) throws SQLException {
        Turno t = new Turno();
        t.setIdTurno(rs.getInt("id_turno"));
        t.setIdMascota(rs.getInt("id_mascota"));
        t.setIdVeterinario(rs.getInt("id_veterinario"));
        t.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
        t.setEstado(Turno.Estado.valueOf(rs.getString("estado")));
        return t;
    }
}
