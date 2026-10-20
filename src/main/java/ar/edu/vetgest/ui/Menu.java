package ar.edu.vetgest.ui;

import ar.edu.vetgest.excepcion.VetGestException;
import ar.edu.vetgest.modelo.Cliente;
import ar.edu.vetgest.modelo.Especie;
import ar.edu.vetgest.modelo.Insumo;
import ar.edu.vetgest.modelo.Mascota;
import ar.edu.vetgest.modelo.Veterinario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ABSTRACCION + HERENCIA: todos los menus comparten el mismo ciclo
 * (mostrar opciones, leer, ejecutar, repetir hasta 0). Ese ciclo es un
 * metodo plantilla final; cada submenu solo define sus opciones y que
 * hacer con cada una (metodos abstractos, resueltos por polimorfismo).
 */
public abstract class Menu {

    protected final Consola consola;
    protected final Sesion sesion;
    private final String titulo;

    protected Menu(String titulo, Consola consola, Sesion sesion) {
        this.titulo = titulo;
        this.consola = consola;
        this.sesion = sesion;
    }

    protected abstract String[] opciones();

    protected abstract void ejecutarOpcion(int opcion);

    protected String textoSalida() {
        return "Volver al menu anterior";
    }

    public final void mostrar() {
        int opcion;
        do {
            consola.titulo(titulo + "  |  " + sesion.encabezado());
            String[] opciones = opciones();
            for (int i = 0; i < opciones.length; i++) {
                consola.printf("  %d. %s%n", i + 1, opciones[i]);
            }
            consola.println("  0. " + textoSalida());
            opcion = consola.leerEntero("Opcion: ", 0, opciones.length);
            if (opcion != 0) {
                ejecutarConControl(opcion);
            }
        } while (opcion != 0);
    }

    /** Punto unico de captura: un error de negocio no termina el programa, se informa y se sigue. */
    private void ejecutarConControl(int opcion) {
        try {
            ejecutarOpcion(opcion);
        } catch (VetGestException e) {
            consola.error(e.getMessage());
            sesion.bitacora().registrar("[" + e.getCodigo() + "] " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ ayudas heredadas por los submenus
    protected LocalDate hoy() {
        return LocalDate.now(sesion.getReloj());
    }

    protected void registrar(String evento) {
        sesion.bitacora().registrar(evento);
    }

    /** Busca el cliente por DNI (busqueda binaria) y, si no existe, ofrece darlo de alta. */
    protected Cliente pedirCliente() {
        String dni = consola.leerTexto("DNI del cliente: ");
        Optional<Cliente> encontrado = sesion.pacientes().buscarClientePorDni(dni);
        if (encontrado.isPresent()) {
            consola.println("  " + encontrado.get());
            return encontrado.get();
        }
        if (!consola.confirmar("  No existe un cliente con DNI " + dni + ". Registrarlo ahora? (s/n): ")) {
            throw new OperacionCanceladaException("Operacion cancelada: cliente inexistente");
        }
        return altaCliente(dni);
    }

    protected Cliente altaCliente(String dni) {
        String nombre = consola.leerTexto("  Nombre: ");
        String apellido = consola.leerTexto("  Apellido: ");
        String telefono = consola.leerTexto("  Telefono: ");
        String email = consola.leerTextoOpcional("  Email (opcional): ");
        Cliente cliente = sesion.pacientes().registrarCliente(new Cliente(dni, nombre, apellido, telefono, email));
        consola.ok("Cliente registrado: " + cliente);
        registrar("Alta de cliente " + cliente.getNombreCompleto() + " (id " + cliente.getId() + ")");
        return cliente;
    }

    protected Mascota altaMascota(Cliente cliente) {
        String nombre = consola.leerTexto("  Nombre de la mascota: ");
        int especie = consola.leerEntero("  Especie (1 Canino, 2 Felino, 3 Exotico): ", 1, 3);
        String raza = consola.leerTextoOpcional("  Raza (opcional): ");
        char sexo = consola.leerOpcion("  Sexo (M/H): ", "MH");
        LocalDate nacimiento = consola.leerFecha("  Fecha de nacimiento dd/mm/aaaa (opcional): ", null);
        Mascota mascota = Mascota.crear(Especie.desdeId(especie), null, cliente.getId(), nombre, raza, sexo, nacimiento);
        sesion.pacientes().registrarMascota(mascota);
        consola.ok("Mascota registrada: " + mascota);
        registrar("Alta de mascota " + mascota.getNombre() + " (id " + mascota.getId() + ")");
        return mascota;
    }

    protected Mascota elegirMascota(Cliente cliente) {
        List<Mascota> mascotas = cliente.getMascotas().aLista();
        if (mascotas.isEmpty()) {
            consola.println("  El cliente no tiene mascotas registradas.");
            return altaMascota(cliente);
        }
        for (int i = 0; i < mascotas.size(); i++) {
            consola.printf("    %d) %s%n", i + 1, mascotas.get(i));
        }
        return mascotas.get(consola.leerEntero("  Mascota: ", 1, mascotas.size()) - 1);
    }

    protected Veterinario elegirVeterinario() {
        List<Veterinario> deLaSucursal = new ArrayList<>();
        for (Veterinario v : sesion.pacientes().veterinarios()) {
            if (v.getIdSucursal() == sesion.getSucursal().getId()) {
                deLaSucursal.add(v);
            }
        }
        for (int i = 0; i < deLaSucursal.size(); i++) {
            consola.printf("    %d) %s%n", i + 1, deLaSucursal.get(i));
        }
        return deLaSucursal.get(consola.leerEntero("  Veterinario/a: ", 1, deLaSucursal.size()) - 1);
    }

    protected Insumo elegirInsumo() {
        List<Insumo> insumos = sesion.inventario().insumos();
        for (int i = 0; i < insumos.size(); i++) {
            consola.printf("    %d) %s%n", i + 1, insumos.get(i));
        }
        return insumos.get(consola.leerEntero("  Insumo: ", 1, insumos.size()) - 1);
    }
}
