package ar.edu.vetgest.dao.jdbc;

import ar.edu.vetgest.dao.TurnoDAO;
import ar.edu.vetgest.excepcion.EntidadNoEncontradaException;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.modelo.EstadoTurno;
import ar.edu.vetgest.modelo.ItemAgenda;
import ar.edu.vetgest.modelo.Turno;
import ar.edu.vetgest.util.ConexionBD;

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

public class TurnoDAOJdbc implements TurnoDAO {

    private static final String SQL_INSERTAR = "INSERT INTO turno (id_mascota, id_veterinario, id_sucursal, "
            + "id_usuario, fecha_hora, duracion_min, motivo, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    /** Misma condicion que el trigger: cada intervalo comienza antes de que termine el otro. */
    private static final String SQL_SUPERPUESTOS = "SELECT COUNT(*) FROM turno "
            + "WHERE id_veterinario = ? AND estado IN ('RESERVADO','CONFIRMADO') "
            + "AND ? < DATE_ADD(fecha_hora, INTERVAL duracion_min MINUTE) "
            + "AND fecha_hora < ? AND id_turno <> ?";

    /** Rango semiabierto [dia, dia+1) para que MySQL use el indice idx_turno_sucursal_fecha. */
    private static final String SQL_AGENDA = "SELECT t.*, m.nombre AS mascota, "
            + "CONCAT(c.apellido, ', ', c.nombre) AS cliente, CONCAT(v.apellido, ', ', v.nombre) AS veterinario "
            + "FROM turno t JOIN mascota m ON m.id_mascota = t.id_mascota "
            + "JOIN cliente c ON c.id_cliente = m.id_cliente "
            + "JOIN veterinario v ON v.id_veterinario = t.id_veterinario "
            + "WHERE t.id_sucursal = ? AND t.fecha_hora >= ? AND t.fecha_hora < ? ORDER BY t.fecha_hora";

    @Override
    public int insertar(Turno t) {
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getIdMascota());
            ps.setInt(2, t.getIdVeterinario());
            ps.setInt(3, t.getIdSucursal());
            ps.setInt(4, t.getIdUsuario());
            ps.setTimestamp(5, Timestamp.valueOf(t.getFechaHora()));
            ps.setInt(6, t.getDuracionMin());
            ps.setString(7, t.getMotivo());
            ps.setString(8, t.getEstado().name());
            ps.executeUpdate();
            int id = CatalogoDAOJdbc.claveGenerada(ps);
            t.asignarId(id);
            return id;
        } catch (SQLException e) {
            throw Jdbc.traducir("registrar el turno", e);   // el trigger devuelve SQLSTATE 45000
        }
    }

    @Override
    public Optional<Turno> buscarPorId(int idTurno) {
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM turno WHERE id_turno = ?")) {
            ps.setInt(1, idTurno);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("buscar el turno", e);
        }
    }

    @Override
    public int contarSuperpuestos(int idVeterinario, LocalDateTime inicio, int minutos, Integer excluirIdTurno) {
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_SUPERPUESTOS)) {
            ps.setInt(1, idVeterinario);
            ps.setTimestamp(2, Timestamp.valueOf(inicio));
            ps.setTimestamp(3, Timestamp.valueOf(inicio.plusMinutes(minutos)));
            ps.setInt(4, excluirIdTurno == null ? 0 : excluirIdTurno);   // los id comienzan en 1
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("verificar la disponibilidad", e);
        }
    }

    @Override
    public void actualizarEstado(int idTurno, EstadoTurno anterior, EstadoTurno nuevo) {
        String sql = "UPDATE turno SET estado = ? WHERE id_turno = ? AND estado = ?";
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nuevo.name());
            ps.setInt(2, idTurno);
            ps.setString(3, anterior.name());
            if (ps.executeUpdate() != 1) {
                throw new ReglaNegocioException("El turno " + idTurno
                        + " fue modificado por otro usuario; vuelva a consultarlo");
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("actualizar el estado del turno", e);
        }
    }

    @Override
    public void actualizarFecha(int idTurno, LocalDateTime nuevaFechaHora) {
        String sql = "UPDATE turno SET fecha_hora = ? WHERE id_turno = ? AND estado IN ('RESERVADO','CONFIRMADO')";
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(nuevaFechaHora));
            ps.setInt(2, idTurno);
            if (ps.executeUpdate() != 1) {
                throw new EntidadNoEncontradaException("turno activo", idTurno);
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("reprogramar el turno", e);
        }
    }

    @Override
    public List<ItemAgenda> agenda(int idSucursal, LocalDate fecha) {
        List<ItemAgenda> agenda = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_AGENDA)) {
            ps.setInt(1, idSucursal);
            ps.setTimestamp(2, Timestamp.valueOf(fecha.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(fecha.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    agenda.add(new ItemAgenda(mapear(rs), rs.getString("mascota"), rs.getString("cliente"),
                            rs.getString("veterinario")));
                }
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("consultar la agenda", e);
        }
        return agenda;
    }

    private static Turno mapear(ResultSet rs) throws SQLException {
        return new Turno(rs.getInt("id_turno"), rs.getInt("id_mascota"), rs.getInt("id_veterinario"),
                rs.getInt("id_sucursal"), rs.getInt("id_usuario"), rs.getTimestamp("fecha_hora").toLocalDateTime(),
                rs.getInt("duracion_min"), rs.getString("motivo"), EstadoTurno.valueOf(rs.getString("estado")));
    }
}
