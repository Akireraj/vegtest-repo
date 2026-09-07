package com.vetgest.servicio;

import com.vetgest.dao.TurnoDAO;
import com.vetgest.modelo.Turno;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Reglas de negocio del modulo de Turnos. Implementa RF01, RF02, RF03,
 * RF04 y el caso de uso "Registrar turno" / "Registrar historia clinica"
 * del informe.
 */
public class TurnoService {

    private final TurnoDAO turnoDAO = new TurnoDAO();

    /**
     * RF01 + RF02: reserva un turno validando que el veterinario no
     * tenga ya otro turno activo en el mismo horario.
     * Flujo alternativo 3a de la especificacion: si hay superposicion,
     * se rechaza sin registrar el turno.
     */
    public Turno reservarTurno(int idMascota, int idVeterinario, LocalDateTime fechaHora) throws SQLException {
        if (fechaHora == null) {
            throw new IllegalArgumentException("La fecha y hora del turno son obligatorias.");
        }
        if (fechaHora.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se puede reservar un turno en una fecha/hora pasada.");
        }
        if (turnoDAO.existeSuperposicion(idVeterinario, fechaHora, null)) {
            throw new IllegalStateException(
                    "Conflicto de horario: el veterinario #" + idVeterinario
                            + " ya tiene un turno activo el " + fechaHora + ". Elija otro horario.");
        }
        Turno turno = new Turno(idMascota, idVeterinario, fechaHora);
        return turnoDAO.crear(turno);
    }

    /** RF03: confirma un turno que se encuentra RESERVADO. */
    public boolean confirmarTurno(int idTurno) throws SQLException {
        Turno turno = obtenerOFallar(idTurno);
        if (turno.getEstado() != Turno.Estado.RESERVADO) {
            throw new IllegalStateException("Solo se puede confirmar un turno en estado RESERVADO (actual: "
                    + turno.getEstado() + ").");
        }
        return turnoDAO.actualizarEstado(idTurno, Turno.Estado.CONFIRMADO);
    }

    /** RF03: cancela un turno, cualquiera sea su estado previo (salvo ya ATENDIDO). */
    public boolean cancelarTurno(int idTurno) throws SQLException {
        Turno turno = obtenerOFallar(idTurno);
        if (turno.getEstado() == Turno.Estado.ATENDIDO) {
            throw new IllegalStateException("No se puede cancelar un turno que ya fue atendido.");
        }
        return turnoDAO.actualizarEstado(idTurno, Turno.Estado.CANCELADO);
    }

    /** RF03: reprograma un turno, validando nuevamente que no haya superposicion. */
    public boolean reprogramarTurno(int idTurno, LocalDateTime nuevaFechaHora) throws SQLException {
        Turno turno = obtenerOFallar(idTurno);
        if (turno.getEstado() == Turno.Estado.ATENDIDO || turno.getEstado() == Turno.Estado.CANCELADO) {
            throw new IllegalStateException("No se puede reprogramar un turno en estado " + turno.getEstado() + ".");
        }
        if (turnoDAO.existeSuperposicion(turno.getIdVeterinario(), nuevaFechaHora, idTurno)) {
            throw new IllegalStateException(
                    "Conflicto de horario: el veterinario ya tiene un turno activo el " + nuevaFechaHora + ".");
        }
        return turnoDAO.reprogramar(idTurno, nuevaFechaHora);
    }

    /**
     * RF04 + caso de uso "Registrar historia clinica": registra el
     * diagnostico y tratamiento de la atencion y marca el turno como
     * ATENDIDO. Flujo alternativo 1a: exige que el turno este CONFIRMADO.
     *
     * @return el id_historia generado, necesario para registrar el
     *         consumo de insumos asociado a esta atencion.
     */
    public int atenderTurno(int idTurno, String diagnostico, String tratamiento) throws SQLException {
        Turno turno = obtenerOFallar(idTurno);
        if (turno.getEstado() != Turno.Estado.CONFIRMADO) {
            throw new IllegalStateException(
                    "El turno debe estar CONFIRMADO antes de registrar la historia clinica (actual: "
                            + turno.getEstado() + ").");
        }
        return turnoDAO.registrarAtencion(idTurno, diagnostico, tratamiento);
    }

    public Optional<Turno> buscarPorId(int idTurno) throws SQLException {
        return turnoDAO.buscarPorId(idTurno);
    }

    private Turno obtenerOFallar(int idTurno) throws SQLException {
        return turnoDAO.buscarPorId(idTurno)
                .orElseThrow(() -> new IllegalArgumentException("No existe un turno con id " + idTurno));
    }
}
