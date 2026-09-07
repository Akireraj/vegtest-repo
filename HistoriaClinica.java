package com.vetgest.modelo;

import java.time.LocalDate;

/**
 * Representa un registro de historia clinica (tabla "historia_clinica"),
 * vinculado 1 a 1 con un turno.
 *
 * NOTA DE DISEÑO: la seccion 8.1 del informe lista como clases de modelo
 * unicamente Mascota, Turno e Insumo. Sin embargo, el modelo
 * entidad-relacion (Figura 5) define que "mov_inventario" se vincula al
 * consumo de insumos a traves de "id_historia" (no de "id_turno"), y el
 * caso de uso "Registrar historia clinica" exige que atender un turno
 * genere este registro antes de poder descontar insumos. Por lo tanto,
 * esta clase de apoyo es minima e imprescindible para que el flujo
 * TurnoService.atenderTurno(...) -> InventarioService.registrarConsumo(...)
 * sea consistente con el esquema de base de datos. No se expone como un
 * modulo de negocio independiente (no tiene DAO ni servicio propios): la
 * crea internamente TurnoDAO al registrar la atencion del turno.
 */
public class HistoriaClinica {

    private Integer idHistoria;
    private Integer idTurno;
    private String diagnostico;
    private String tratamiento;
    private LocalDate fecha;

    public HistoriaClinica() {
    }

    public HistoriaClinica(Integer idTurno, String diagnostico, String tratamiento, LocalDate fecha) {
        this.idTurno = idTurno;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.fecha = fecha;
    }

    public Integer getIdHistoria() {
        return idHistoria;
    }

    public void setIdHistoria(Integer idHistoria) {
        this.idHistoria = idHistoria;
    }

    public Integer getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(Integer idTurno) {
        this.idTurno = idTurno;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return String.format("Historia #%d - Turno #%d - Diagnostico: %s", idHistoria, idTurno, diagnostico);
    }
}
