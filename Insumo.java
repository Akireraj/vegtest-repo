package com.vetgest.modelo;

import java.time.LocalDate;

/**
 * Representa un insumo del inventario (tabla "insumo"). Soporta RF06
 * (descuento de stock), RF07 (reposicion) y RF08 (alerta de stock minimo).
 */
public class Insumo {

    private Integer idInsumo;
    private String nombre;
    private int stockActual;
    private int stockMinimo;
    private LocalDate vencimiento; // puede ser null

    public Insumo() {
    }

    public Insumo(String nombre, int stockActual, int stockMinimo, LocalDate vencimiento) {
        this.nombre = nombre;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.vencimiento = vencimiento;
    }

    public Integer getIdInsumo() {
        return idInsumo;
    }

    public void setIdInsumo(Integer idInsumo) {
        this.idInsumo = idInsumo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public LocalDate getVencimiento() {
        return vencimiento;
    }

    public void setVencimiento(LocalDate vencimiento) {
        this.vencimiento = vencimiento;
    }

    /** RF08: el insumo esta en alerta cuando el stock actual cae al minimo o por debajo. */
    public boolean isAlertaStockMinimo() {
        return stockActual <= stockMinimo;
    }

    @Override
    public String toString() {
        return String.format("#%d - %s - Stock: %d (minimo: %d)%s",
                idInsumo, nombre, stockActual, stockMinimo,
                isAlertaStockMinimo() ? "  [ALERTA STOCK MINIMO]" : "");
    }
}
