package ar.edu.vetgest.ui;

import ar.edu.vetgest.estructuras.ListaEnlazada;
import ar.edu.vetgest.modelo.Cliente;
import ar.edu.vetgest.modelo.Mascota;
import ar.edu.vetgest.modelo.Persona;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Busquedas, ordenamientos y consultas disponibles para todos los roles. */
public class MenuConsultas extends Menu {

    public MenuConsultas(Consola consola, Sesion sesion) {
        super("CONSULTAS Y BUSQUEDAS", consola, sesion);
    }

    @Override
    protected String[] opciones() {
        return new String[]{
                "Buscar cliente por DNI (busqueda binaria)",
                "Buscar mascota por nombre (busqueda lineal)",
                "Clientes ordenados por apellido (merge sort)",
                "Historial clinico de una mascota (CU07)",
                "Personas de la red: clientes y veterinarios",
                "Bitacora de la sesion (pila: lo mas reciente primero)"};
    }

    @Override
    protected void ejecutarOpcion(int opcion) {
        switch (opcion) {
            case 1 -> buscarCliente();
            case 2 -> buscarMascota();
            case 3 -> sesion.pacientes().clientesOrdenadosPorApellido().forEach(c -> consola.println("  " + c));
            case 4 -> {
                Mascota m = elegirMascota(pedirCliente());
                Formato.historial(consola, sesion.historias().historial(m.getId()));
            }
            case 5 -> personas();
            case 6 -> sesion.bitacora().ultimos(15).forEach(e -> consola.println("  " + e));
            default -> throw new IllegalStateException("Opcion no contemplada: " + opcion);
        }
    }

    private void buscarCliente() {
        String dni = consola.leerTexto("DNI: ");
        Optional<Cliente> cliente = sesion.pacientes().buscarClientePorDni(dni);
        if (cliente.isEmpty()) {
            consola.println("  No se encontro un cliente con DNI " + dni);
            return;
        }
        consola.println("  " + cliente.get());
        for (Mascota m : cliente.get().getMascotas()) {
            consola.println("    - " + m);
        }
    }

    private void buscarMascota() {
        ListaEnlazada<Mascota> encontradas = sesion.pacientes().buscarMascotasPorNombre(consola.leerTexto("Nombre o parte: "));
        consola.println("  " + encontradas.tamanio() + " coincidencia(s)");
        for (Mascota m : encontradas) {
            consola.println("    - " + m + " | " + m.getRecomendacionPreventiva());
        }
    }

    /** Polimorfismo: una sola lista de Persona; cada objeto responde toString() segun su clase real. */
    private void personas() {
        List<Persona> personas = new ArrayList<>(sesion.pacientes().veterinarios());
        personas.addAll(sesion.pacientes().clientesOrdenadosPorApellido());
        for (Persona p : personas) {
            consola.println("  " + p);
        }
    }
}
