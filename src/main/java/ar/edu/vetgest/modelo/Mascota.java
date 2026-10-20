package ar.edu.vetgest.modelo;

import ar.edu.vetgest.excepcion.DatoInvalidoException;

import java.time.LocalDate;
import java.time.Period;

/**
 * ABSTRACCION + POLIMORFISMO: cada especie define su duracion de consulta
 * sugerida y su recomendacion preventiva. TurnoService usa
 * getDuracionConsultaMin() sin saber que subclase concreta recibe.
 */
public abstract class Mascota {

    private Integer id;
    private final int idCliente;
    private String nombre;
    private String raza;
    private final char sexo;
    private final LocalDate fechaNacimiento;
    private boolean activa = true;

    protected Mascota(Integer id, int idCliente, String nombre, String raza, char sexo, LocalDate fechaNacimiento) {
        char s = Character.toUpperCase(sexo);
        if (s != 'M' && s != 'H') {
            throw new DatoInvalidoException("El sexo debe ser M (macho) o H (hembra)");
        }
        this.id = id;
        this.idCliente = idCliente;
        this.nombre = Validaciones.texto(nombre, "nombre de la mascota", 40);
        this.raza = Validaciones.textoOpcional(raza, "raza", 40);
        this.sexo = s;
        this.fechaNacimiento = fechaNacimiento;
    }

    /** Metodo de fabrica: crea la subclase que corresponde a la especie. */
    public static Mascota crear(Especie especie, Integer id, int idCliente, String nombre, String raza,
                                char sexo, LocalDate fechaNacimiento) {
        return switch (especie) {
            case CANINO -> new Canino(id, idCliente, nombre, raza, sexo, fechaNacimiento);
            case FELINO -> new Felino(id, idCliente, nombre, raza, sexo, fechaNacimiento);
            case EXOTICO -> new Exotico(id, idCliente, nombre, raza, sexo, fechaNacimiento);
        };
    }

    public abstract Especie getEspecie();

    /** Duracion sugerida del turno en minutos (10 a 240). */
    public abstract int getDuracionConsultaMin();

    public abstract String getRecomendacionPreventiva();

    /** Edad en anios cumplidos, o -1 si no se registro la fecha de nacimiento. */
    public int edadEnAnios(LocalDate hoy) {
        return (fechaNacimiento == null) ? -1 : Period.between(fechaNacimiento, hoy).getYears();
    }

    public void asignarId(int nuevoId) {
        if (id != null) {
            throw new DatoInvalidoException("La mascota ya tiene id " + id);
        }
        this.id = nuevoId;
    }

    public Integer getId() {
        return id;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRaza() {
        return raza;
    }

    public char getSexo() {
        return sexo;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public boolean isActiva() {
        return activa;
    }

    /** Baja logica: la historia clinica se conserva (seccion 4.5 de la AP2). */
    public void darDeBaja() {
        this.activa = false;
    }

    @Override
    public String toString() {
        return String.format("#%d %s (%s, %s, %s)", id, nombre, getEspecie().getNombre(),
                raza == null ? "sin raza" : raza, sexo == 'M' ? "macho" : "hembra");
    }
}
