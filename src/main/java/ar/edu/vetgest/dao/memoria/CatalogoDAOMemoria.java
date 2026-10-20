package ar.edu.vetgest.dao.memoria;

import ar.edu.vetgest.dao.CatalogoDAO;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.modelo.Cliente;
import ar.edu.vetgest.modelo.Mascota;
import ar.edu.vetgest.modelo.Sucursal;
import ar.edu.vetgest.modelo.Usuario;
import ar.edu.vetgest.modelo.Veterinario;

import java.util.List;
import java.util.Optional;

public class CatalogoDAOMemoria implements CatalogoDAO {

    private final BaseDatosMemoria db;

    public CatalogoDAOMemoria(BaseDatosMemoria db) {
        this.db = db;
    }

    @Override
    public List<Sucursal> listarSucursales() {
        return db.sucursales.aLista();
    }

    @Override
    public List<Usuario> listarUsuariosActivos() {
        return db.usuarios.aLista();
    }

    @Override
    public List<Veterinario> listarVeterinarios() {
        return db.veterinarios.aLista();
    }

    @Override
    public Optional<Veterinario> buscarVeterinario(int idVeterinario) {
        return db.veterinarios.buscar(v -> v.getId() == idVeterinario);
    }

    @Override
    public List<Cliente> listarClientesConMascotas() {
        return db.clientes.aLista();
    }

    @Override
    public Optional<Mascota> buscarMascota(int idMascota) {
        return db.mascotas.buscar(m -> m.getId() == idMascota);
    }

    @Override
    public int insertarCliente(Cliente cliente) {
        if (db.clientes.buscar(c -> c.getDni().equals(cliente.getDni())).isPresent()) {   // UNIQUE(dni)
            throw new ReglaNegocioException("Ya existe un cliente con DNI " + cliente.getDni());
        }
        int id = ++db.secCliente;
        cliente.asignarId(id);
        db.clientes.agregar(cliente);
        return id;
    }

    @Override
    public int insertarMascota(Mascota mascota) {
        Cliente duenio = db.cliente(mascota.getIdCliente());                                // FK id_cliente
        int id = ++db.secMascota;
        mascota.asignarId(id);
        db.mascotas.agregar(mascota);
        duenio.agregarMascota(mascota);
        return id;
    }
}
