package ar.edu.vetgest.servicio;

import ar.edu.vetgest.dao.TurnoDAO;
import ar.edu.vetgest.estructuras.Cola;
import ar.edu.vetgest.excepcion.EntidadNoEncontradaException;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.modelo.EstadoTurno;
import ar.edu.vetgest.modelo.Turno;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sala de espera de la sucursal: una Cola FIFO por veterinario. El
 * paciente que llega primero es el primero en ser llamado por su profesional.
 */
public class SalaEsperaService {

    private final TurnoDAO turnoDAO;
    private final Clock reloj;
    private final Map<Integer, Cola<Turno>> colasPorVeterinario = new LinkedHashMap<>();

    public SalaEsperaService(TurnoDAO turnoDAO, Clock reloj) {
        this.turnoDAO = turnoDAO;
        this.reloj = reloj;
    }

    /** Registra la llegada del paciente y devuelve su posicion en la cola de su veterinario. */
    public int registrarLlegada(int idTurno, int idSucursal) {
        Turno turno = turnoDAO.buscarPorId(idTurno).orElseThrow(() -> new EntidadNoEncontradaException("turno", idTurno));
        if (turno.getIdSucursal() != idSucursal) {
            throw new ReglaNegocioException("El turno " + idTurno + " corresponde a otra sucursal");
        }
        if (turno.getEstado() != EstadoTurno.CONFIRMADO) {
            throw new ReglaNegocioException("Solo ingresan a la sala turnos CONFIRMADOS (estado actual: "
                    + turno.getEstado() + ")");
        }
        if (!turno.getFechaHora().toLocalDate().equals(LocalDate.now(reloj))) {
            throw new ReglaNegocioException("El turno " + idTurno + " no es del dia de hoy");
        }
        Cola<Turno> cola = colasPorVeterinario.computeIfAbsent(turno.getIdVeterinario(), k -> new Cola<>());
        if (cola.contiene(t -> t.getIdTurno() == idTurno)) {
            throw new ReglaNegocioException("El paciente del turno " + idTurno + " ya esta en la sala de espera");
        }
        cola.encolar(turno);
        return cola.tamanio();
    }

    /** El veterinario llama al siguiente paciente de su cola (FIFO). */
    public Turno llamarSiguiente(int idVeterinario) {
        Cola<Turno> cola = colasPorVeterinario.get(idVeterinario);
        if (cola == null || cola.estaVacia()) {
            throw new ReglaNegocioException("No hay pacientes esperando al veterinario " + idVeterinario);
        }
        return cola.desencolar();
    }

    public List<Turno> enEspera(int idVeterinario) {
        List<Turno> lista = new ArrayList<>();
        Cola<Turno> cola = colasPorVeterinario.get(idVeterinario);
        if (cola != null) {
            for (Turno t : cola) {
                lista.add(t);
            }
        }
        return lista;
    }

    public List<Integer> veterinariosConEspera() {
        List<Integer> ids = new ArrayList<>();
        for (Map.Entry<Integer, Cola<Turno>> e : colasPorVeterinario.entrySet()) {
            if (!e.getValue().estaVacia()) {
                ids.add(e.getKey());
            }
        }
        return ids;
    }

    public int totalEnEspera() {
        int total = 0;
        for (Cola<Turno> cola : colasPorVeterinario.values()) {
            total += cola.tamanio();
        }
        return total;
    }
}
