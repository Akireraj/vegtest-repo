package ar.edu.vetgest.ui;

import ar.edu.vetgest.modelo.Cliente;
import ar.edu.vetgest.modelo.Mascota;
import ar.edu.vetgest.modelo.Turno;
import ar.edu.vetgest.modelo.Veterinario;
import ar.edu.vetgest.servicio.Exportacion;
import ar.edu.vetgest.servicio.GrillaDisponibilidad;
import ar.edu.vetgest.servicio.TurnoService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/** CU01 a CU05: alta de pacientes, gestion de la agenda, grilla de disponibilidad y exportacion (rol RECEPCION). */
public class MenuTurnos extends Menu {

    public MenuTurnos(Consola consola, Sesion sesion) {
        super("TURNOS Y PACIENTES", consola, sesion);
    }

    @Override
    protected String[] opciones() {
        return new String[]{
                "Registrar cliente y/o mascota",
                "Registrar turno (CU01 + CU04)",
                "Confirmar turno (CU02)",
                "Reprogramar turno (CU03)",
                "Cancelar turno (CU02)",
                "Marcar turno como ausente",
                "Deshacer la ultima reserva de la sesion",
                "Agenda del dia de la sucursal (CU05)",
                "Grilla de disponibilidad del dia (arreglos)",
                "Exportar la agenda del dia a CSV (archivo)"};
    }

    @Override
    protected void ejecutarOpcion(int opcion) {
        switch (opcion) {
            case 1 -> altaClienteYMascota();
            case 2 -> registrarTurno();
            case 3 -> mostrarResultado("confirmado", sesion.turnos().confirmar(leerNumeroTurno()));
            case 4 -> reprogramar();
            case 5 -> mostrarResultado("cancelado", sesion.turnos().cancelar(leerNumeroTurno()));
            case 6 -> mostrarResultado("marcado como ausente", sesion.turnos().marcarAusente(leerNumeroTurno()));
            case 7 -> mostrarResultado("cancelado (deshacer)", sesion.turnos().deshacerUltimaReserva());
            case 8 -> agenda();
            case 9 -> grilla();
            case 10 -> exportarAgenda();
            default -> throw new IllegalStateException("Opcion no contemplada: " + opcion);
        }
    }

    private void altaClienteYMascota() {
        String dni = consola.leerTexto("DNI del cliente: ");
        Optional<Cliente> existente = sesion.pacientes().buscarClientePorDni(dni);
        Cliente cliente;
        if (existente.isPresent()) {
            cliente = existente.get();
            consola.println("  Cliente existente: " + cliente);
            existente.get().getMascotas().forEach(m -> consola.println("    - " + m));
        } else {
            cliente = altaCliente(dni);
        }
        while (consola.confirmar("Registrar una mascota para " + cliente.getNombre() + "? (s/n): ")) {
            altaMascota(cliente);
        }
    }

    private void registrarTurno() {
        Cliente cliente = pedirCliente();
        Mascota mascota = elegirMascota(cliente);
        Veterinario veterinario = elegirVeterinario();
        LocalDate dia = consola.leerFecha("Fecha dd/mm/aaaa [Enter = manana]: ", hoy().plusDays(1));

        int sugerida = mascota.getDuracionConsultaMin();          // polimorfismo segun la especie
        Integer elegida = consola.leerEnteroOpcional("Duracion en minutos [Enter = " + sugerida + ", sugerida para "
                + mascota.getEspecie().getNombre() + "]: ", Turno.DURACION_MINIMA, Turno.DURACION_MAXIMA);
        int duracion = (elegida == null) ? sugerida : elegida;

        Optional<LocalDateTime> libre = sesion.turnos().buscarPrimerHorarioLibre(veterinario.getId(), dia, duracion,
                TurnoService.APERTURA);
        String sugerencia = libre.map(f -> " [Enter = primer horario libre " + f.toLocalTime() + "]")
                .orElse(" (no hay horarios libres ese dia)");
        LocalTime hora = consola.leerHora("Hora HH:mm" + sugerencia + ": ", libre.map(LocalDateTime::toLocalTime).orElse(null));
        String motivo = consola.leerTexto("Motivo de la consulta: ");

        Turno turno = sesion.turnos().registrar(mascota.getId(), veterinario.getId(), sesion.getSucursal().getId(),
                sesion.getUsuario().getId(), dia.atTime(hora), duracion, motivo);
        consola.ok("Turno registrado: " + turno + " para " + mascota.getNombre() + " con "
                + veterinario.getNombreCompleto());
        registrar("Registro de " + turno);
    }

    private void reprogramar() {
        int numero = leerNumeroTurno();
        consola.println("  Actual: " + sesion.turnos().obtener(numero));
        LocalDateTime nueva = consola.leerFechaHora("Nueva fecha y hora (dd/mm/aaaa HH:mm): ");
        mostrarResultado("reprogramado", sesion.turnos().reprogramar(numero, nueva));
    }

    private void agenda() {
        LocalDate fecha = consola.leerFecha("Fecha dd/mm/aaaa [Enter = hoy]: ", hoy());
        consola.println("  Agenda de " + sesion.getSucursal().getNombre() + " del " + fecha.format(Consola.FECHA) + ":");
        Formato.agenda(consola, sesion.turnos().agenda(sesion.getSucursal().getId(), fecha));
    }

    private void grilla() {
        LocalDate fecha = consola.leerFecha("Fecha dd/mm/aaaa [Enter = hoy]: ", hoy());
        GrillaDisponibilidad grilla = sesion.turnos().grilla(sesion.getSucursal().getId(), fecha);
        consola.println("  Ocupacion de " + sesion.getSucursal().getNombre() + " el " + fecha.format(Consola.FECHA)
                + " (# ocupado, . libre; cada caracter = " + GrillaDisponibilidad.MINUTOS_FRANJA + " min)");
        Formato.grilla(consola, grilla);
        if (consola.confirmar("Ver los horarios libres de un veterinario/a? (s/n): ")) {
            Veterinario vet = elegirVeterinario();
            int duracion = consola.leerEntero("Duracion en minutos: ", Turno.DURACION_MINIMA, Turno.DURACION_MAXIMA);
            List<LocalTime> libres = sesion.turnos().horariosLibres(grilla, vet.getId(), duracion);
            consola.println("  " + libres.size() + " horario(s) libre(s) de " + duracion + " min para "
                    + vet.getNombreCompleto() + ":");
            Formato.horarios(consola, libres);
        }
    }

    private void exportarAgenda() {
        LocalDate fecha = consola.leerFecha("Fecha dd/mm/aaaa [Enter = hoy]: ", hoy());
        Exportacion e = sesion.reportes().exportarAgenda(sesion.getSucursal(), fecha);
        consola.ok("Agenda guardada en " + e.archivo().toAbsolutePath() + " (" + e.filas() + " turno(s))");
        registrar("Exportacion de la agenda del " + fecha.format(Consola.FECHA) + " a " + e.archivo().getFileName());
    }

    private int leerNumeroTurno() {
        return consola.leerEntero("Numero de turno: ", 1, Integer.MAX_VALUE);
    }

    private void mostrarResultado(String accion, Turno turno) {
        consola.ok("Turno " + accion + ": " + turno);
        registrar("Turno " + turno.getIdTurno() + " " + accion);
    }
}
