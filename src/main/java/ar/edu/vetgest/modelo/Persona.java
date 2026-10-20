package ar.edu.vetgest.modelo;

import ar.edu.vetgest.excepcion.DatoInvalidoException;

/**
 * ABSTRACCION: representa lo comun a clientes y veterinarios. No puede
 * instanciarse; cada subclase define que tipo de persona es y que detalle
 * muestra (metodos abstractos).
 * ENCAPSULAMIENTO: atributos privados, validados en los setters.
 */
public abstract class Persona {

    private Integer id;
    private String nombre;
    private String apellido;
    private String telefono;

    protected Persona(Integer id, String nombre, String apellido, String telefono) {
        this.id = id;
        setNombre(nombre);
        setApellido(apellido);
        setTelefono(telefono);
    }

    /** Tipo de persona, para listados y bitacora. */
    public abstract String getTipo();

    /** Datos propios de cada subclase. */
    public abstract String getDetalle();

    /** Metodo final: el formato del nombre es igual para todas las subclases. */
    public final String getNombreCompleto() {
        return apellido + ", " + nombre;
    }

    /** Asigna el id generado por la base de datos; solo puede hacerse una vez. */
    public void asignarId(int nuevoId) {
        if (id != null) {
            throw new DatoInvalidoException("El registro ya tiene id " + id);
        }
        this.id = nuevoId;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) {
        this.nombre = Validaciones.texto(nombre, "nombre", 60);
    }

    public String getApellido() {
        return apellido;
    }

    public final void setApellido(String apellido) {
        this.apellido = Validaciones.texto(apellido, "apellido", 60);
    }

    public String getTelefono() {
        return telefono;
    }

    public final void setTelefono(String telefono) {
        this.telefono = Validaciones.textoOpcional(telefono, "telefono", 20);
    }

    /** Usa los metodos abstractos: el resultado depende de la subclase (polimorfismo). */
    @Override
    public String toString() {
        return String.format("[%s] %s - %s", getTipo(), getNombreCompleto(), getDetalle());
    }
}
