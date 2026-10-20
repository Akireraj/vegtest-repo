package ar.edu.vetgest.servicio;

import ar.edu.vetgest.algoritmos.Busqueda;
import ar.edu.vetgest.algoritmos.Ordenamiento;
import ar.edu.vetgest.dao.CatalogoDAO;
import ar.edu.vetgest.estructuras.ListaEnlazada;
import ar.edu.vetgest.excepcion.EntidadNoEncontradaException;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.modelo.Cliente;
import ar.edu.vetgest.modelo.Mascota;
import ar.edu.vetgest.modelo.Veterinario;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Alta y busqueda de clientes y mascotas (actividad "Buscar / registrar cliente y mascota"). */
public class PacienteService {

    private final CatalogoDAO catalogoDAO;

    public PacienteService(CatalogoDAO catalogoDAO) {
        this.catalogoDAO = catalogoDAO;
    }

    /** Busqueda binaria por DNI sobre la lista ordenada con merge sort. */
    public Optional<Cliente> buscarClientePorDni(String dni) {
        List<Cliente> ordenados = Ordenamiento.mergeSort(catalogoDAO.listarClientesConMascotas(),
                Comparator.comparing(Cliente::getDni));
        int posicion = Busqueda.binaria(ordenados, dni.trim(), Cliente::getDni, Comparator.naturalOrder());
        return posicion >= 0 ? Optional.of(ordenados.get(posicion)) : Optional.empty();
    }

    /** Busqueda lineal por coincidencia parcial del nombre, sin distinguir mayusculas ni tildes. */
    public ListaEnlazada<Mascota> buscarMascotasPorNombre(String texto) {
        String buscado = Busqueda.normalizar(texto);
        if (buscado.length() < 2) {
            throw new ReglaNegocioException("Ingrese al menos 2 letras para buscar");
        }
        ListaEnlazada<Mascota> todas = new ListaEnlazada<>();
        for (Cliente c : catalogoDAO.listarClientesConMascotas()) {
            for (Mascota m : c.getMascotas()) {
                todas.agregar(m);
            }
        }
        return Busqueda.linealTodos(todas, m -> Busqueda.normalizar(m.getNombre()).contains(buscado));
    }

    public List<Cliente> clientesOrdenadosPorApellido() {
        return Ordenamiento.mergeSort(catalogoDAO.listarClientesConMascotas(),
                Comparator.comparing(Cliente::getApellido).thenComparing(Cliente::getNombre));
    }

    public Cliente registrarCliente(Cliente cliente) {
        if (buscarClientePorDni(cliente.getDni()).isPresent()) {
            throw new ReglaNegocioException("Ya existe un cliente con DNI " + cliente.getDni());
        }
        catalogoDAO.insertarCliente(cliente);
        return cliente;
    }

    public Mascota registrarMascota(Mascota mascota) {
        catalogoDAO.insertarMascota(mascota);
        return mascota;
    }

    public Mascota mascota(int idMascota) {
        return catalogoDAO.buscarMascota(idMascota)
                .orElseThrow(() -> new EntidadNoEncontradaException("mascota", idMascota));
    }

    public Veterinario veterinario(int idVeterinario) {
        return catalogoDAO.buscarVeterinario(idVeterinario)
                .orElseThrow(() -> new EntidadNoEncontradaException("veterinario", idVeterinario));
    }

    public List<Veterinario> veterinarios() {
        return catalogoDAO.listarVeterinarios();
    }
}
