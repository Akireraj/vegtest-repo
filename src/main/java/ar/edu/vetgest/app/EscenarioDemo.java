package ar.edu.vetgest.app;

import ar.edu.vetgest.dao.FabricaDAO;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.excepcion.VetGestException;
import ar.edu.vetgest.modelo.AlertaStock;
import ar.edu.vetgest.modelo.Cliente;
import ar.edu.vetgest.modelo.HistoriaClinica;
import ar.edu.vetgest.modelo.Insumo;
import ar.edu.vetgest.modelo.Mascota;
import ar.edu.vetgest.modelo.Persona;
import ar.edu.vetgest.modelo.Turno;
import ar.edu.vetgest.servicio.Bitacora;
import ar.edu.vetgest.servicio.HistoriaClinicaService;
import ar.edu.vetgest.servicio.InventarioService;
import ar.edu.vetgest.servicio.PacienteService;
import ar.edu.vetgest.servicio.ResultadoConsumo;
import ar.edu.vetgest.servicio.SalaEsperaService;
import ar.edu.vetgest.servicio.TurnoService;
import ar.edu.vetgest.util.RelojSimulado;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Escenario end-to-end del proceso TO-BE (Figura 4 de la AP2): reserva,
 * superposicion, confirmacion, deshacer, sala de espera, atencion,
 * consumo FEFO, alertas, busquedas y bitacora. Usa los datos de prueba
 * del script 02 y funciona con cualquiera de las dos fabricas de DAO.
 */
public class EscenarioDemo {

    // identificadores de sql/02_insercion_datos.sql
    private static final int CENTRO = 1;
    private static final int USR_RECEPCION = 1;
    private static final int USR_DRA_GOMEZ = 3;
    private static final int DRA_GOMEZ = 1;
    private static final int DR_PAEZ = 2;
    private static final int TOBY = 1;
    private static final int KIRA = 5;
    private static final int BRUNO = 7;
    private static final int NALA = 8;
    private static final int VACUNA_SEXTUPLE = 2;
    private static final int JERINGA = 5;
    private static final int KETAMINA = 6;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PrintStream out;
    private final RelojSimulado reloj;
    private final TurnoService turnos;
    private final HistoriaClinicaService historias;
    private final InventarioService inventario;
    private final SalaEsperaService sala;
    private final PacienteService pacientes;
    private final Bitacora bitacora;
    private int numeroPaso;

    public EscenarioDemo(FabricaDAO fabrica, Clock base, PrintStream out) {
        this.out = out;
        this.reloj = new RelojSimulado(base);   // reloj propio: adelantarlo no afecta la sesion interactiva
        this.turnos = new TurnoService(fabrica.turnos(), fabrica.catalogo(), reloj);
        this.historias = new HistoriaClinicaService(fabrica.historias(), fabrica.turnos());
        this.inventario = new InventarioService(fabrica.inventario(), reloj);
        this.sala = new SalaEsperaService(fabrica.turnos(), reloj);
        this.pacientes = new PacienteService(fabrica.catalogo());
        this.bitacora = new Bitacora(reloj);
        out.println("Escenario de demostracion sobre: " + fabrica.descripcion());
    }

    public void ejecutar() {
        try {
            recorrerEscenario();
            out.println();
            out.println("Escenario finalizado correctamente.");
        } catch (VetGestException e) {
            out.println("  El escenario se detuvo: " + e.getMessage());
            out.println("  Ejecutelo sobre una base recien cargada (scripts 01 y 02) o en modo --memoria.");
        }
    }

    private void recorrerEscenario() {
        LocalDate dia = proximoDiaHabil(LocalDate.now(reloj).plusDays(1));

        paso("Primer horario libre de la Dra. Gomez y registro del turno (RF01, CU04)");
        Turno t1 = turnos.registrar(NALA, DRA_GOMEZ, CENTRO, USR_RECEPCION,
                primerLibre(DRA_GOMEZ, dia, LocalTime.of(10, 0)), null, "Control y vacuna cachorro");
        ok(t1 + " Nala (Canino: 30 min sugeridos)");

        paso("Intento de turno superpuesto 15 minutos despues (RF02)");
        intentar(() -> turnos.registrar(TOBY, DRA_GOMEZ, CENTRO, USR_RECEPCION, t1.getFechaHora().plusMinutes(15),
                null, "Control"));

        paso("Turno contiguo: comienza justo cuando termina el anterior (valor limite)");
        Turno t2 = turnos.registrar(TOBY, DRA_GOMEZ, CENTRO, USR_RECEPCION, t1.getFin(), null, "Control post vacuna");
        ok(t2 + " Toby");

        paso("Turno con otro veterinario y confirmacion de los tres (RF03)");
        Turno t3 = turnos.registrar(BRUNO, DR_PAEZ, CENTRO, USR_RECEPCION,
                primerLibre(DR_PAEZ, dia, LocalTime.of(10, 0)), null, "Control de piel");
        for (Turno t : List.of(t1, t2, t3)) {
            ok(turnos.confirmar(t.getIdTurno()).toString());
        }

        paso("Reserva cargada por error y deshacer (pila LIFO)");
        Turno erroneo = turnos.registrar(KIRA, DR_PAEZ, CENTRO, USR_RECEPCION,
                primerLibre(DR_PAEZ, dia, LocalTime.of(16, 0)), null, "Carga erronea");
        ok("Reservado " + erroneo);
        ok("Deshacer -> " + turnos.deshacerUltimaReserva());

        paso("Llegada de pacientes a la sala de espera (cola FIFO por veterinario)");
        reloj.ajustarA(dia.atTime(9, 45));
        out.println("  Reloj simulado: " + LocalDateTime.now(reloj).format(FMT));
        for (Turno t : List.of(t1, t3, t2)) {
            int posicion = sala.registrarLlegada(t.getIdTurno(), CENTRO);
            ok(pacientes.mascota(t.getIdMascota()).getNombre() + " en sala, posicion " + posicion
                    + " con " + pacientes.veterinario(t.getIdVeterinario()).getNombreCompleto());
        }
        intentar(() -> sala.registrarLlegada(t1.getIdTurno(), CENTRO));

        paso("La Dra. Gomez llama al siguiente paciente y registra la historia clinica (RF04)");
        Turno llamado = sala.llamarSiguiente(DRA_GOMEZ);
        Mascota paciente = pacientes.mascota(llamado.getIdMascota());
        ok("Llamado: " + paciente.getNombre() + " (" + llamado + ")");
        out.println("  Recomendacion (" + paciente.getEspecie().getNombre() + "): " + paciente.getRecomendacionPreventiva());
        HistoriaClinica h = historias.registrar(new HistoriaClinica(llamado.getIdTurno(), new BigDecimal("12.5"),
                "Paciente sano", "Refuerzo de vacuna sextuple; sedacion leve para extraccion de muestra", null));
        ok("Historia " + h.getIdHistoria() + " registrada; turno " + turnos.obtener(llamado.getIdTurno()));
        out.println("  En espera para la Dra. Gomez: " + sala.enEspera(DRA_GOMEZ).size() + " paciente(s)");

        paso("Consumo de insumos con descuento FEFO (RF06) y alerta de minimo (RF08)");
        consumir(h, JERINGA, 2);
        consumir(h, KETAMINA, 1);
        consumir(h, VACUNA_SEXTUPLE, 1);     // el lote mas proximo esta vencido y el otro vacio
        consumir(h, KETAMINA, 1);            // ya no queda stock vigente

        paso("Alertas de la red ordenadas por criticidad (merge sort)");
        for (AlertaStock a : inventario.alertasPorCriticidad()) {
            out.println("  " + a);
        }

        paso("Busqueda binaria por DNI y busqueda lineal por nombre");
        Optional<Cliente> cliente = pacientes.buscarClientePorDni("29555666");
        ok("DNI 29555666 -> " + cliente.map(Cliente::toString).orElse("no encontrado"));
        ok("DNI 11111111 -> " + pacientes.buscarClientePorDni("11111111").map(Cliente::toString).orElse("no encontrado"));
        for (Mascota m : pacientes.buscarMascotasPorNombre("LU")) {
            ok("Nombre que contiene 'LU' -> " + m);
        }

        paso("Polimorfismo: una lista de Persona con clientes y veterinarios");
        List<Persona> personas = new ArrayList<>();
        personas.add(pacientes.veterinario(DRA_GOMEZ));
        personas.add(cliente.orElseThrow());
        personas.add(pacientes.veterinario(6));
        for (Persona p : personas) {
            out.println("  " + p);
        }

        paso("Bitacora del escenario (pila: lo mas reciente primero)");
        for (String evento : bitacora.ultimos(5)) {
            out.println("  " + evento);
        }
        out.println("  ... " + bitacora.cantidad() + " eventos registrados");
    }

    // ------------------------------------------------------------------ auxiliares
    private void consumir(HistoriaClinica h, int idInsumo, int cantidad) {
        try {
            ResultadoConsumo r = inventario.registrarConsumo(h.getIdHistoria(), idInsumo, CENTRO, cantidad, USR_DRA_GOMEZ);
            ok(nombreInsumo(idInsumo) + " x" + cantidad + " descontado por lote " + r.distribucion());
            if (r.generoAlerta()) {
                out.println("        " + r.alerta());
            }
        } catch (ReglaNegocioException e) {
            out.print("  [" + nombreInsumo(idInsumo) + "]");
            rechazado(e);
        }
    }

    private String nombreInsumo(int idInsumo) {
        for (Insumo i : inventario.insumos()) {
            if (i.getId() == idInsumo) {
                return i.getNombre();
            }
        }
        return "Insumo " + idInsumo;
    }

    private LocalDateTime primerLibre(int idVeterinario, LocalDate dia, LocalTime desde) {
        return turnos.buscarPrimerHorarioLibre(idVeterinario, dia, 30, desde)
                .orElseThrow(() -> new ReglaNegocioException("No hay horarios libres el " + dia));
    }

    private static LocalDate proximoDiaHabil(LocalDate desde) {
        LocalDate dia = desde;
        while (dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
            dia = dia.plusDays(1);
        }
        return dia;
    }

    /** Ejecuta una accion que DEBE ser rechazada por una regla de negocio. */
    private void intentar(Runnable accion) {
        try {
            accion.run();
            out.println("  NO ESPERADO -> la operacion debio ser rechazada");
        } catch (ReglaNegocioException e) {
            rechazado(e);
        }
    }

    private void paso(String titulo) {
        out.println();
        out.println("== " + (++numeroPaso) + ". " + titulo);
    }

    private void ok(String mensaje) {
        out.println("  OK -> " + mensaje);
        bitacora.registrar(mensaje);
    }

    private void rechazado(ReglaNegocioException e) {
        out.println("  RECHAZADO -> " + e.getMessage());
        bitacora.registrar("[" + e.getCodigo() + "] " + e.getMessage());
    }
}
