package ar.edu.vetgest.ui;

import ar.edu.vetgest.app.EscenarioDemo;
import ar.edu.vetgest.dao.memoria.FabricaDAOMemoria;
import ar.edu.vetgest.modelo.Modulo;

import java.util.ArrayList;
import java.util.List;

/** Menu principal: muestra solo los modulos habilitados para el rol del usuario (RF10). */
public class MenuPrincipal extends Menu {

    private final List<Modulo> habilitados = new ArrayList<>();

    public MenuPrincipal(Consola consola, Sesion sesion) {
        super("VETGEST - MENU PRINCIPAL", consola, sesion);
        for (Modulo m : Modulo.values()) {
            if (sesion.getUsuario().getRol().puedeAcceder(m)) {
                habilitados.add(m);
            }
        }
    }

    @Override
    protected String[] opciones() {
        String[] opciones = new String[habilitados.size() + 1];
        for (int i = 0; i < habilitados.size(); i++) {
            opciones[i] = habilitados.get(i).getDescripcion();
        }
        opciones[habilitados.size()] = "Ejecutar escenario de demostracion";
        return opciones;
    }

    @Override
    protected void ejecutarOpcion(int opcion) {
        if (opcion <= habilitados.size()) {
            Menu submenu = crearSubmenu(habilitados.get(opcion - 1));
            submenu.mostrar();                                   // enlace dinamico: se ejecuta el del submenu real
        } else {
            boolean enMemoria = sesion.getFabrica() instanceof FabricaDAOMemoria;
            if (enMemoria || consola.confirmar("El escenario modifica la base MySQL. Continuar? (s/n): ")) {
                new EscenarioDemo(sesion.getFabrica(), sesion.getReloj(), consola.salida()).ejecutar();
            }
        }
    }

    private Menu crearSubmenu(Modulo modulo) {
        return switch (modulo) {
            case TURNOS -> new MenuTurnos(consola, sesion);
            case SALA_ESPERA -> new MenuSalaEspera(consola, sesion);
            case ATENCION -> new MenuAtencion(consola, sesion);
            case INVENTARIO -> new MenuInventario(consola, sesion);
            case CONSULTAS -> new MenuConsultas(consola, sesion);
        };
    }

    @Override
    protected String textoSalida() {
        return "Salir del sistema";
    }
}
