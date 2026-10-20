package ar.edu.vetgest.ui;

import ar.edu.vetgest.modelo.EstadoTurno;
import ar.edu.vetgest.modelo.ItemAgenda;
import ar.edu.vetgest.modelo.Turno;

import java.util.ArrayList;
import java.util.List;

/** Sala de espera de la sucursal: colas FIFO por veterinario. */
public class MenuSalaEspera extends Menu {

    public MenuSalaEspera(Consola consola, Sesion sesion) {
        super("SALA DE ESPERA", consola, sesion);
    }

    @Override
    protected String[] opciones() {
        return new String[]{"Registrar llegada de un paciente", "Ver la sala de espera"};
    }

    @Override
    protected void ejecutarOpcion(int opcion) {
        if (opcion == 1) {
            registrarLlegada();
        } else {
            verSala();
        }
    }

    private void registrarLlegada() {
        List<ItemAgenda> confirmados = new ArrayList<>();
        for (ItemAgenda i : sesion.turnos().agenda(sesion.getSucursal().getId(), hoy())) {
            if (i.turno().getEstado() == EstadoTurno.CONFIRMADO) {
                confirmados.add(i);
            }
        }
        consola.println("  Turnos confirmados de hoy:");
        Formato.agenda(consola, confirmados);
        int numero = consola.leerEntero("Numero de turno del paciente que llego: ", 1, Integer.MAX_VALUE);
        int posicion = sesion.salaEspera().registrarLlegada(numero, sesion.getSucursal().getId());
        consola.ok("Paciente en sala. Posicion " + posicion + " en la cola de su veterinario/a");
        registrar("Llegada a sala del turno " + numero);
    }

    private void verSala() {
        if (sesion.salaEspera().totalEnEspera() == 0) {
            consola.println("  La sala de espera esta vacia.");
            return;
        }
        for (int idVet : sesion.salaEspera().veterinariosConEspera()) {
            consola.println("  " + sesion.pacientes().veterinario(idVet).getNombreCompleto() + ":");
            int posicion = 1;
            for (Turno t : sesion.salaEspera().enEspera(idVet)) {
                consola.printf("    %d. %s - %s%n", posicion++, sesion.pacientes().mascota(t.getIdMascota()).getNombre(), t);
            }
        }
    }
}
