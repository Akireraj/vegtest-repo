package ar.edu.vetgest.dao;

import ar.edu.vetgest.modelo.EstadoTurno;
import ar.edu.vetgest.modelo.ItemAgenda;
import ar.edu.vetgest.modelo.Turno;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Persistencia de turnos (RF01 a RF03). */
public interface TurnoDAO {

    /** Inserta el turno, le asigna el id generado y lo devuelve. */
    int insertar(Turno turno);

    Optional<Turno> buscarPorId(int idTurno);

    /** Turnos activos del veterinario que se superponen con [inicio, inicio + minutos). */
    int contarSuperpuestos(int idVeterinario, LocalDateTime inicio, int minutos, Integer excluirIdTurno);

    /** Cambia el estado solo si el turno sigue en {@code anterior} (control de concurrencia optimista). */
    void actualizarEstado(int idTurno, EstadoTurno anterior, EstadoTurno nuevo);

    void actualizarFecha(int idTurno, LocalDateTime nuevaFechaHora);

    List<ItemAgenda> agenda(int idSucursal, LocalDate fecha);
}
