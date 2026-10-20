package ar.edu.vetgest.dao;

import ar.edu.vetgest.modelo.Cliente;
import ar.edu.vetgest.modelo.Mascota;
import ar.edu.vetgest.modelo.Sucursal;
import ar.edu.vetgest.modelo.Usuario;
import ar.edu.vetgest.modelo.Veterinario;

import java.util.List;
import java.util.Optional;

/** Acceso a las tablas maestras: sucursal, usuario, veterinario, cliente y mascota. */
public interface CatalogoDAO {

    List<Sucursal> listarSucursales();

    List<Usuario> listarUsuariosActivos();

    List<Veterinario> listarVeterinarios();

    Optional<Veterinario> buscarVeterinario(int idVeterinario);

    /** Clientes con sus mascotas activas ya asociadas. */
    List<Cliente> listarClientesConMascotas();

    Optional<Mascota> buscarMascota(int idMascota);

    int insertarCliente(Cliente cliente);

    int insertarMascota(Mascota mascota);
}
