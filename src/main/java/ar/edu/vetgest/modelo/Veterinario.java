package ar.edu.vetgest.modelo;

/** HERENCIA: un veterinario ES UNA persona; agrega matricula, especialidad y sede. */
public class Veterinario extends Persona {

    private final String matricula;
    private String especialidad;
    private final int idSucursal;

    public Veterinario(Integer id, int idSucursal, String matricula, String nombre, String apellido,
                       String especialidad) {
        super(id, nombre, apellido, null);   // la tabla veterinario no registra telefono
        this.matricula = Validaciones.texto(matricula, "matricula", 15);
        this.idSucursal = idSucursal;
        setEspecialidad(especialidad);
    }

    public String getMatricula() {
        return matricula;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public final void setEspecialidad(String especialidad) {
        String valor = Validaciones.textoOpcional(especialidad, "especialidad", 60);
        this.especialidad = (valor == null) ? "Clinica general" : valor;
    }

    public int getIdSucursal() {
        return idSucursal;
    }

    @Override
    public String getTipo() {
        return "Veterinario/a";
    }

    @Override
    public String getDetalle() {
        return "Mat. " + matricula + " - " + especialidad;
    }
}
