package ar.edu.vetgest.servicio;

import ar.edu.vetgest.modelo.EstadoTurno;
import ar.edu.vetgest.modelo.ItemAgenda;
import ar.edu.vetgest.modelo.Turno;
import ar.edu.vetgest.modelo.Veterinario;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ocupacion de la agenda de una sucursal en un dia, como matriz
 * veterinario x franja de 15 minutos (08:00 a 20:00 = 48 franjas).
 * <p>
 * Uso complementario de arreglos y ArrayList:
 * <ul>
 *   <li>la agenda llega de la base como {@code ArrayList<ItemAgenda>}, porque
 *       la cantidad de turnos del dia no se conoce de antemano;</li>
 *   <li>la grilla es un {@code boolean[][]}: la cantidad de veterinarios y de
 *       franjas es fija una vez construida y se accede por indice en O(1);</li>
 *   <li>el contador por estado es un {@code int[]} indexado por el ordinal del
 *       enumerado EstadoTurno;</li>
 *   <li>los horarios libres vuelven a ser un {@code ArrayList<LocalTime>},
 *       porque su cantidad depende de la ocupacion.</li>
 * </ul>
 */
public final class GrillaDisponibilidad {

    public static final int MINUTOS_FRANJA = 15;
    public static final int FRANJAS =
            (int) (Duration.between(TurnoService.APERTURA, TurnoService.CIERRE).toMinutes() / MINUTOS_FRANJA);

    private final LocalDate dia;
    private final Veterinario[] veterinarios;                                     // filas de la grilla
    private final boolean[][] ocupado;                                            // [fila][franja]
    private final int[] turnosPorEstado = new int[EstadoTurno.values().length];   // [estado.ordinal()]

    public GrillaDisponibilidad(LocalDate dia, List<Veterinario> veterinarios, List<ItemAgenda> agenda) {
        this.dia = dia;
        this.veterinarios = veterinarios.toArray(new Veterinario[0]);   // ArrayList -> arreglo de tamano fijo
        this.ocupado = new boolean[this.veterinarios.length][FRANJAS];   // todas las celdas nacen en false (libre)
        for (ItemAgenda item : agenda) {
            Turno t = item.turno();
            turnosPorEstado[t.getEstado().ordinal()]++;
            int fila = filaDe(t.getIdVeterinario());
            if (fila >= 0 && t.getEstado().esActivo() && t.getFechaHora().toLocalDate().equals(dia)) {
                marcar(fila, t.getFechaHora().toLocalTime(), t.getDuracionMin());
            }
        }
    }

    /** Marca como ocupadas todas las franjas que toca el turno, aunque no empiece en un multiplo de 15. */
    private void marcar(int fila, LocalTime inicio, int minutos) {
        int desdeMin = minutosDesdeApertura(inicio);
        int primera = Math.max(0, desdeMin / MINUTOS_FRANJA);
        int ultima = Math.min(FRANJAS, (desdeMin + minutos + MINUTOS_FRANJA - 1) / MINUTOS_FRANJA);  // techo
        for (int franja = primera; franja < ultima; franja++) {
            ocupado[fila][franja] = true;
        }
    }

    /** Busqueda lineal del veterinario en el arreglo de filas; -1 si no esta. */
    public int filaDe(int idVeterinario) {
        for (int i = 0; i < veterinarios.length; i++) {
            if (veterinarios[i].getId() == idVeterinario) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Horarios de inicio en los que el veterinario tiene libres todas las
     * franjas que necesita la duracion indicada. La validacion definitiva
     * la hace CU04 al registrar el turno (servicio + trigger).
     */
    public List<LocalTime> horariosLibres(int idVeterinario, int duracionMin) {
        List<LocalTime> libres = new ArrayList<>();
        int fila = filaDe(idVeterinario);
        if (fila < 0) {
            return libres;
        }
        int necesarias = (duracionMin + MINUTOS_FRANJA - 1) / MINUTOS_FRANJA;
        for (int inicio = 0; inicio + necesarias <= FRANJAS; inicio++) {
            boolean alcanza = true;
            for (int k = inicio; k < inicio + necesarias && alcanza; k++) {
                alcanza = !ocupado[fila][k];
            }
            if (alcanza) {
                libres.add(horaDeFranja(inicio));
            }
        }
        return libres;
    }

    public boolean estaOcupada(int fila, int franja) {
        return ocupado[fila][franja];
    }

    /** Porcentaje de la jornada ocupado por turnos activos. */
    public int porcentajeOcupacion(int fila) {
        int ocupadas = 0;
        for (boolean celda : ocupado[fila]) {
            if (celda) {
                ocupadas++;
            }
        }
        return Math.round(ocupadas * 100f / FRANJAS);
    }

    /** Representacion de una fila: '#' ocupado y '.' libre, un caracter por franja. */
    public String filaComoTexto(int fila) {
        char[] celdas = new char[FRANJAS];
        for (int franja = 0; franja < FRANJAS; franja++) {
            celdas[franja] = ocupado[fila][franja] ? '#' : '.';
        }
        return new String(celdas);
    }

    public static LocalTime horaDeFranja(int franja) {
        return TurnoService.APERTURA.plusMinutes((long) franja * MINUTOS_FRANJA);
    }

    private static int minutosDesdeApertura(LocalTime hora) {
        return (int) Duration.between(TurnoService.APERTURA, hora).toMinutes();
    }

    public LocalDate getDia() {
        return dia;
    }

    public int cantidadFilas() {
        return veterinarios.length;
    }

    /** Copia defensiva: quien la recibe no puede alterar las filas de la grilla. */
    public Veterinario[] getVeterinarios() {
        return veterinarios.clone();
    }

    /** Copia defensiva del contador por estado (indice = EstadoTurno.ordinal()). */
    public int[] getTurnosPorEstado() {
        return Arrays.copyOf(turnosPorEstado, turnosPorEstado.length);
    }
}
