package ar.edu.vetgest.modelo;

import ar.edu.vetgest.estructuras.ListaEnlazada;
import ar.edu.vetgest.excepcion.DatoInvalidoException;

/** HERENCIA: un cliente ES UNA persona; agrega DNI, email y sus mascotas. */
public class Cliente extends Persona {

    private final String dni;
    private String email;
    private final ListaEnlazada<Mascota> mascotas = new ListaEnlazada<>();

    public Cliente(Integer id, String dni, String nombre, String apellido, String telefono, String email) {
        super(id, nombre, apellido, telefono);            // inicializa la parte heredada
        if (dni == null || !dni.trim().matches("\\d{7,10}")) {
            throw new DatoInvalidoException("El DNI debe tener entre 7 y 10 digitos numericos");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new DatoInvalidoException("El telefono del cliente es obligatorio");
        }
        this.dni = dni.trim();
        setEmail(email);
    }

    /** Sobrecarga del constructor: cliente nuevo, todavia sin id. */
    public Cliente(String dni, String nombre, String apellido, String telefono, String email) {
        this(null, dni, nombre, apellido, telefono, email);
    }

    public void agregarMascota(Mascota mascota) {
        if (getId() != null && mascota.getIdCliente() != getId()) {
            throw new DatoInvalidoException("La mascota " + mascota.getNombre() + " pertenece a otro cliente");
        }
        mascotas.agregar(mascota);
    }

    /** Devuelve una copia: quien consulta no puede alterar la lista interna. */
    public ListaEnlazada<Mascota> getMascotas() {
        return mascotas.copia();
    }

    public String getDni() {
        return dni;
    }

    public String getEmail() {
        return email;
    }

    public final void setEmail(String email) {
        String valor = Validaciones.textoOpcional(email, "email", 100);
        if (valor != null && !valor.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new DatoInvalidoException("El email '" + valor + "' no tiene un formato valido");
        }
        this.email = valor;
    }

    @Override
    public String getTipo() {
        return "Cliente";
    }

    @Override
    public String getDetalle() {
        return String.format("DNI %s, tel. %s, %d mascota(s)", dni, getTelefono(), mascotas.tamanio());
    }
}
