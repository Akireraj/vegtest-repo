package com.vetgest.modelo;

import java.time.LocalDateTime;

/**
 * Representa un turno (tabla "turno"). Implementa RF01 (registrar turno),
 * RF02 (no superposicion) y RF03 (confirmar / reprogramar / cancelar),
 * segun la especificacion del caso de uso "Registrar turno" del informe.
 */
public class Turno {

    public enum Estado {
        RESERVADO, CONFIRMADO, ATENDIDO, CANCELADO
    }

    private Integer idTurno;
    private Integer idMascota;
    private Integer idVeterinario;
    private LocalDateTime fechaHora;
    private Estado estado;

    public Turno() {
    }

    public Turno(Integer idMascota, Integer idVeterinario, LocalDateTime fechaHora) {
        this.idMascota = idMascota;
        this.idVeterinario = idVeterinario;
        this.fechaHora = fechaHora;
        this.estado = Estado.RESERVADO;
    }

    public Integer getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(Integer idTurno) {
        this.idTurno = idTurno;
    }

    public Integer getIdMascota() {
        return idMascota;
    }

    public void setIdMascota(Integer idMascota) {
        this.idMascota = idMascota;
    }

    public Integer getIdVeterinario() {
        return idVeterinario;
    }

    public void setIdVeterinario(Integer idVeterinario) {
        this.idVeterinario = idVeterinario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return String.format("Turno #%d - Mascota #%d - Veterinario #%d - %s - Estado: %s",
                idTurno, idMascota, idVeterinario, fechaHora, estado);
    }
}
