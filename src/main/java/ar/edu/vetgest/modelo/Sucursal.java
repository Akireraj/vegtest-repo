package ar.edu.vetgest.modelo;

/** Sede de la red PatasSanas (inmutable). */
public class Sucursal {

    private final int id;
    private final String nombre;
    private final String direccion;
    private final String telefono;

    public Sucursal(int id, String nombre, String direccion, String telefono) {
        this.id = id;
        this.nombre = Validaciones.texto(nombre, "nombre de sucursal", 60);
        this.direccion = direccion;
        this.telefono = telefono;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    @Override
    public String toString() {
        return nombre + " (" + direccion + ")";
    }
}
