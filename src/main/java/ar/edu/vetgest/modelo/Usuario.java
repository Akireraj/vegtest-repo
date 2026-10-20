package ar.edu.vetgest.modelo;

/** Usuario del sistema (tabla usuario). El hash bcrypt no se carga en memoria. */
public class Usuario {

    private final int id;
    private final int idSucursal;
    private final Integer idVeterinario;   // null si no es veterinario
    private final String username;
    private final Rol rol;

    public Usuario(int id, int idSucursal, Integer idVeterinario, String username, Rol rol) {
        this.id = id;
        this.idSucursal = idSucursal;
        this.idVeterinario = idVeterinario;
        this.username = Validaciones.texto(username, "username", 30);
        this.rol = rol;
    }

    public int getId() {
        return id;
    }

    public int getIdSucursal() {
        return idSucursal;
    }

    public Integer getIdVeterinario() {
        return idVeterinario;
    }

    public boolean esVeterinario() {
        return idVeterinario != null;
    }

    public String getUsername() {
        return username;
    }

    public Rol getRol() {
        return rol;
    }

    @Override
    public String toString() {
        return username + " (" + rol + ")";
    }
}
