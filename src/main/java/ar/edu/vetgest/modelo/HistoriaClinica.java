package ar.edu.vetgest.modelo;

import ar.edu.vetgest.excepcion.DatoInvalidoException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Registro de la atencion de un turno (tabla historia_clinica, RF04). */
public class HistoriaClinica {

    private static final BigDecimal PESO_MAXIMO = new BigDecimal("999.99");   // DECIMAL(5,2)

    private Integer idHistoria;
    private final int idTurno;
    private final BigDecimal pesoKg;
    private final String diagnostico;
    private final String tratamiento;
    private final String observaciones;

    public HistoriaClinica(int idTurno, BigDecimal pesoKg, String diagnostico, String tratamiento,
                           String observaciones) {
        if (pesoKg == null || pesoKg.signum() <= 0 || pesoKg.compareTo(PESO_MAXIMO) > 0) {
            throw new DatoInvalidoException("El peso debe ser mayor a 0 y menor a 1000 kg");
        }
        this.idTurno = idTurno;
        this.pesoKg = pesoKg.setScale(2, RoundingMode.HALF_UP);
        this.diagnostico = Validaciones.texto(diagnostico, "diagnostico", 500);
        this.tratamiento = Validaciones.texto(tratamiento, "tratamiento", 500);
        this.observaciones = Validaciones.textoOpcional(observaciones, "observaciones", 500);
    }

    public void asignarId(int id) {
        if (idHistoria != null) {
            throw new DatoInvalidoException("La historia ya tiene id " + idHistoria);
        }
        this.idHistoria = id;
    }

    public Integer getIdHistoria() {
        return idHistoria;
    }

    public int getIdTurno() {
        return idTurno;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public String getObservaciones() {
        return observaciones;
    }
}
