package ar.edu.vetgest.modelo;

import ar.edu.vetgest.excepcion.DatoInvalidoException;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.excepcion.TransicionInvalidaException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Turno de atencion (tabla turno). ENCAPSULAMIENTO: el estado solo cambia a
 * traves de cambiarEstado(), que respeta el diagrama de estados; no existe
 * un setEstado() publico que permita saltear la regla.
 */
public class Turno {

    public static final int DURACION_MINIMA = 10;
    public static final int DURACION_MAXIMA = 240;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Integer idTurno;
    private final int idMascota;
    private final int idVeterinario;
    private final int idSucursal;
    private final int idUsuario;
    private LocalDateTime fechaHora;
    private final int duracionMin;
    private final String motivo;
    private EstadoTurno estado;

    /** Constructor completo: lo usan los DAO al reconstruir un turno guardado. */
    public Turno(Integer idTurno, int idMascota, int idVeterinario, int idSucursal, int idUsuario,
                 LocalDateTime fechaHora, int duracionMin, String motivo, EstadoTurno estado) {
        if (fechaHora == null) {
            throw new DatoInvalidoException("La fecha y hora del turno son obligatorias");
        }
        if (duracionMin < DURACION_MINIMA || duracionMin > DURACION_MAXIMA) {
            throw new DatoInvalidoException("La duracion debe estar entre " + DURACION_MINIMA
                    + " y " + DURACION_MAXIMA + " minutos");
        }
        this.idTurno = idTurno;
        this.idMascota = idMascota;
        this.idVeterinario = idVeterinario;
        this.idSucursal = idSucursal;
        this.idUsuario = idUsuario;
        this.fechaHora = fechaHora.withSecond(0).withNano(0);
        this.duracionMin = duracionMin;
        this.motivo = Validaciones.texto(motivo, "motivo", 120);
        this.estado = (estado == null) ? EstadoTurno.RESERVADO : estado;
    }

    /** Sobrecarga: turno nuevo, que siempre nace RESERVADO y sin id. */
    public Turno(int idMascota, int idVeterinario, int idSucursal, int idUsuario,
                 LocalDateTime fechaHora, int duracionMin, String motivo) {
        this(null, idMascota, idVeterinario, idSucursal, idUsuario, fechaHora, duracionMin, motivo,
                EstadoTurno.RESERVADO);
    }

    /** Constructor de copia: el DAO en memoria entrega copias para no exponer su estado interno. */
    public Turno(Turno otro) {
        this(otro.idTurno, otro.idMascota, otro.idVeterinario, otro.idSucursal, otro.idUsuario,
                otro.fechaHora, otro.duracionMin, otro.motivo, otro.estado);
    }

    public LocalDateTime getFin() {
        return fechaHora.plusMinutes(duracionMin);
    }

    /** Dos intervalos se superponen si cada uno comienza antes de que termine el otro (RF02). */
    public boolean seSuperponeCon(LocalDateTime inicio, int minutos) {
        return fechaHora.isBefore(inicio.plusMinutes(minutos)) && inicio.isBefore(getFin());
    }

    public void cambiarEstado(EstadoTurno nuevo) {
        if (!estado.puedePasarA(nuevo)) {
            throw new TransicionInvalidaException(estado, nuevo);
        }
        this.estado = nuevo;
    }

    public void reprogramar(LocalDateTime nuevaFechaHora) {
        if (!estado.esActivo()) {
            throw new ReglaNegocioException("Solo se reprograman turnos RESERVADOS o CONFIRMADOS (estado actual: "
                    + estado + ")");
        }
        this.fechaHora = nuevaFechaHora.withSecond(0).withNano(0);
    }

    public void asignarId(int id) {
        if (idTurno != null) {
            throw new DatoInvalidoException("El turno ya tiene id " + idTurno);
        }
        this.idTurno = id;
    }

    public Integer getIdTurno() {
        return idTurno;
    }

    public int getIdMascota() {
        return idMascota;
    }

    public int getIdVeterinario() {
        return idVeterinario;
    }

    public int getIdSucursal() {
        return idSucursal;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public int getDuracionMin() {
        return duracionMin;
    }

    public String getMotivo() {
        return motivo;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    @Override
    public String toString() {
        return String.format("Turno#%d [vet=%d, %s, %d min, %s]", idTurno, idVeterinario,
                fechaHora.format(FORMATO), duracionMin, estado);
    }
}
