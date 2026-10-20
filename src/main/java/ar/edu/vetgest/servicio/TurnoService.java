package ar.edu.vetgest.servicio;

import ar.edu.vetgest.algoritmos.Ordenamiento;
import ar.edu.vetgest.dao.CatalogoDAO;
import ar.edu.vetgest.dao.TurnoDAO;
import ar.edu.vetgest.estructuras.Pila;
import ar.edu.vetgest.excepcion.EntidadNoEncontradaException;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.modelo.EstadoTurno;
import ar.edu.vetgest.modelo.ItemAgenda;
import ar.edu.vetgest.modelo.Mascota;
import ar.edu.vetgest.modelo.Turno;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Reglas de negocio de la agenda: RF01, RF02 (CU04) y RF03. */
public class TurnoService {

    public static final LocalTime APERTURA = LocalTime.of(8, 0);
    public static final LocalTime CIERRE = LocalTime.of(20, 0);
    private static final int PASO_BUSQUEDA_MIN = 15;

    private final TurnoDAO turnoDAO;
    private final CatalogoDAO catalogoDAO;
    private final Clock reloj;
    private final Pila<Integer> reservasDeLaSesion = new Pila<>();

    public TurnoService(TurnoDAO turnoDAO, CatalogoDAO catalogoDAO, Clock reloj) {
        this.turnoDAO = turnoDAO;
        this.catalogoDAO = catalogoDAO;
        this.reloj = reloj;
    }

    /** Sobrecarga de constructor: usa el reloj del sistema. */
    public TurnoService(TurnoDAO turnoDAO, CatalogoDAO catalogoDAO) {
        this(turnoDAO, catalogoDAO, Clock.systemDefaultZone());
    }

    /** RF01 + RF02: registra el turno solo si el veterinario esta libre en ese intervalo. */
    public Turno registrar(Turno turno) {
        Mascota mascota = mascotaActiva(turno.getIdMascota());
        catalogoDAO.buscarVeterinario(turno.getIdVeterinario())
                .orElseThrow(() -> new EntidadNoEncontradaException("veterinario", turno.getIdVeterinario()));
        validarHorario(turno.getFechaHora(), turno.getDuracionMin());
        validarDisponibilidad(turno.getIdVeterinario(), turno.getFechaHora(), turno.getDuracionMin(), null);
        turnoDAO.insertar(turno);                 // asigna el id generado
        reservasDeLaSesion.apilar(turno.getIdTurno());
        return turno;
    }

    /**
     * Sobrecarga de metodo: si no se indica duracion, se usa la sugerida
     * por la especie. getDuracionConsultaMin() se resuelve en tiempo de
     * ejecucion segun sea Canino, Felino o Exotico (polimorfismo).
     */
    public Turno registrar(int idMascota, int idVeterinario, int idSucursal, int idUsuario,
                           LocalDateTime fechaHora, Integer duracionMin, String motivo) {
        int duracion = (duracionMin != null) ? duracionMin : mascotaActiva(idMascota).getDuracionConsultaMin();
        return registrar(new Turno(idMascota, idVeterinario, idSucursal, idUsuario, fechaHora, duracion, motivo));
    }

    public Turno confirmar(int idTurno) {
        return cambiarEstado(idTurno, EstadoTurno.CONFIRMADO);
    }

    public Turno cancelar(int idTurno) {
        return cambiarEstado(idTurno, EstadoTurno.CANCELADO);
    }

    public Turno marcarAusente(int idTurno) {
        Turno turno = obtener(idTurno);
        if (turno.getFechaHora().isAfter(LocalDateTime.now(reloj))) {
            throw new ReglaNegocioException("No se puede marcar AUSENTE un turno que todavia no comenzo");
        }
        return cambiarEstado(idTurno, EstadoTurno.AUSENTE);
    }

    /** CU03: valida el nuevo horario excluyendo al propio turno de la verificacion. */
    public Turno reprogramar(int idTurno, LocalDateTime nuevaFechaHora) {
        Turno turno = obtener(idTurno);
        if (!turno.getEstado().esActivo()) {
            throw new ReglaNegocioException("Un turno " + turno.getEstado() + " no puede reprogramarse");
        }
        validarHorario(nuevaFechaHora, turno.getDuracionMin());
        validarDisponibilidad(turno.getIdVeterinario(), nuevaFechaHora, turno.getDuracionMin(), idTurno);
        turno.reprogramar(nuevaFechaHora);
        turnoDAO.actualizarFecha(idTurno, turno.getFechaHora());
        return turno;
    }

    /**
     * Deshace la ultima reserva de la sesion usando la Pila (LIFO). Si esa
     * reserva ya fue confirmada o cancelada por otra via, se descarta y se
     * prueba con la anterior.
     */
    public Turno deshacerUltimaReserva() {
        while (!reservasDeLaSesion.estaVacia()) {
            int idTurno = reservasDeLaSesion.desapilar();
            Optional<Turno> turno = turnoDAO.buscarPorId(idTurno);
            if (turno.isPresent() && turno.get().getEstado() == EstadoTurno.RESERVADO) {
                return cancelar(idTurno);
            }
        }
        throw new ReglaNegocioException("No hay reservas de esta sesion que puedan deshacerse");
    }

    /** CU04, flujo alternativo: propone el primer horario libre del dia a partir de una hora. */
    public Optional<LocalDateTime> buscarPrimerHorarioLibre(int idVeterinario, LocalDate dia, int duracion,
                                                            LocalTime desde) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDateTime candidato = dia.atTime(desde.isBefore(APERTURA) ? APERTURA : desde);
        while (!candidato.toLocalTime().plusMinutes(duracion).isAfter(CIERRE)
                && candidato.toLocalDate().equals(dia)) {
            if (candidato.isAfter(ahora) && turnoDAO.contarSuperpuestos(idVeterinario, candidato, duracion, null) == 0) {
                return Optional.of(candidato);
            }
            candidato = candidato.plusMinutes(PASO_BUSQUEDA_MIN);
        }
        return Optional.empty();
    }

    /** Agenda ordenada por hora y, ante igual hora, por veterinario (merge sort estable). */
    public List<ItemAgenda> agenda(int idSucursal, LocalDate fecha) {
        return Ordenamiento.mergeSort(turnoDAO.agenda(idSucursal, fecha),
                Comparator.comparing((ItemAgenda i) -> i.turno().getFechaHora()).thenComparing(ItemAgenda::veterinario));
    }

    public Turno obtener(int idTurno) {
        return turnoDAO.buscarPorId(idTurno).orElseThrow(() -> new EntidadNoEncontradaException("turno", idTurno));
    }

    public int reservasDeshacibles() {
        return reservasDeLaSesion.tamanio();
    }

    // ------------------------------------------------------------------ reglas privadas
    private Turno cambiarEstado(int idTurno, EstadoTurno nuevo) {
        Turno turno = obtener(idTurno);
        EstadoTurno anterior = turno.getEstado();
        turno.cambiarEstado(nuevo);                         // valida la transicion (diagrama de estados)
        turnoDAO.actualizarEstado(idTurno, anterior, nuevo);
        return turno;
    }

    private Mascota mascotaActiva(int idMascota) {
        Mascota mascota = catalogoDAO.buscarMascota(idMascota)
                .orElseThrow(() -> new EntidadNoEncontradaException("mascota", idMascota));
        if (!mascota.isActiva()) {
            throw new ReglaNegocioException("La mascota " + mascota.getNombre() + " esta dada de baja");
        }
        return mascota;
    }

    private void validarHorario(LocalDateTime inicio, int duracion) {
        if (!inicio.isAfter(LocalDateTime.now(reloj))) {
            throw new ReglaNegocioException("No se pueden registrar turnos en una fecha u hora pasada");
        }
        if (inicio.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new ReglaNegocioException("Las clinicas no atienden los domingos");
        }
        LocalTime desde = inicio.toLocalTime();
        LocalTime hasta = desde.plusMinutes(duracion);
        if (desde.isBefore(APERTURA) || hasta.isAfter(CIERRE) || hasta.isBefore(desde)) {
            throw new ReglaNegocioException("El turno debe quedar dentro del horario de atencion (" + APERTURA
                    + " a " + CIERRE + ")");
        }
    }

    private void validarDisponibilidad(int idVeterinario, LocalDateTime inicio, int duracion, Integer excluir) {
        if (turnoDAO.contarSuperpuestos(idVeterinario, inicio, duracion, excluir) > 0) {
            throw new ReglaNegocioException("El veterinario ya tiene un turno activo en ese horario (RF02)");
        }
    }
}
