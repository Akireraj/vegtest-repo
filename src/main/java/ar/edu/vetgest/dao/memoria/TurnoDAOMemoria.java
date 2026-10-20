package ar.edu.vetgest.dao.memoria;

import ar.edu.vetgest.dao.TurnoDAO;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.modelo.EstadoTurno;
import ar.edu.vetgest.modelo.ItemAgenda;
import ar.edu.vetgest.modelo.Mascota;
import ar.edu.vetgest.modelo.Turno;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TurnoDAOMemoria implements TurnoDAO {

    private final BaseDatosMemoria db;

    public TurnoDAOMemoria(BaseDatosMemoria db) {
        this.db = db;
    }

    @Override
    public int insertar(Turno turno) {
        // emula el trigger trg_turno_no_superposicion (segunda linea de defensa)
        if (turno.getEstado().esActivo() && contarSuperpuestos(turno.getIdVeterinario(), turno.getFechaHora(),
                turno.getDuracionMin(), null) > 0) {
            throw new ReglaNegocioException("Superposicion de turno para el veterinario (RF02) [rechazado por la base de datos]");
        }
        int id = ++db.secTurno;
        turno.asignarId(id);
        db.turnos.agregar(new Turno(turno));   // se guarda una copia
        return id;
    }

    @Override
    public Optional<Turno> buscarPorId(int idTurno) {
        return db.turnos.buscar(t -> t.getIdTurno() == idTurno).map(Turno::new);
    }

    @Override
    public int contarSuperpuestos(int idVeterinario, LocalDateTime inicio, int minutos, Integer excluirIdTurno) {
        int cantidad = 0;
        for (Turno t : db.turnos) {
            boolean excluido = excluirIdTurno != null && t.getIdTurno().equals(excluirIdTurno);
            if (!excluido && t.getIdVeterinario() == idVeterinario && t.getEstado().esActivo()
                    && t.seSuperponeCon(inicio, minutos)) {
                cantidad++;
            }
        }
        return cantidad;
    }

    @Override
    public void actualizarEstado(int idTurno, EstadoTurno anterior, EstadoTurno nuevo) {
        Turno guardado = db.turno(idTurno);
        if (guardado.getEstado() != anterior) {
            throw new ReglaNegocioException("El turno " + idTurno + " fue modificado por otro usuario; vuelva a consultarlo");
        }
        guardado.cambiarEstado(nuevo);
    }

    @Override
    public void actualizarFecha(int idTurno, LocalDateTime nuevaFechaHora) {
        db.turno(idTurno).reprogramar(nuevaFechaHora);
    }

    @Override
    public List<ItemAgenda> agenda(int idSucursal, LocalDate fecha) {
        List<ItemAgenda> agenda = new ArrayList<>();
        for (Turno t : db.turnos) {
            if (t.getIdSucursal() == idSucursal && t.getFechaHora().toLocalDate().equals(fecha)) {
                Mascota m = db.mascota(t.getIdMascota());
                agenda.add(new ItemAgenda(new Turno(t), m.getNombre(),
                        db.cliente(m.getIdCliente()).getNombreCompleto(),
                        db.veterinario(t.getIdVeterinario()).getNombreCompleto()));
            }
        }
        return agenda;   // TurnoService la ordena con merge sort
    }
}
