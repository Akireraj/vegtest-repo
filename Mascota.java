package com.vetgest.modelo;

import java.time.LocalDate;

/**
 * Representa una mascota (tabla "mascota" del esquema vetgest_db).
 * Los campos coinciden con el modelo entidad-relacion del informe:
 * id_mascota, id_cliente, nombre, especie, fecha_nac.
 */
public class Mascota {

    private Integer idMascota;
    private Integer idCliente;
    private String nombre;
    private String especie;
    private LocalDate fechaNac;

    public Mascota() {
    }

    public Mascota(Integer idCliente, String nombre, String especie, LocalDate fechaNac) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.especie = especie;
        this.fechaNac = fechaNac;
    }

    public Integer getIdMascota() {
        return idMascota;
    }

    public void setIdMascota(Integer idMascota) {
        this.idMascota = idMascota;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public LocalDate getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(LocalDate fechaNac) {
        this.fechaNac = fechaNac;
    }

    @Override
    public String toString() {
        return String.format("#%d - %s (%s) - Cliente #%d", idMascota, nombre, especie, idCliente);
    }
}
