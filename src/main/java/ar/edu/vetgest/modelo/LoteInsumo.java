package ar.edu.vetgest.modelo;

import ar.edu.vetgest.excepcion.DatoInvalidoException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Lote de un insumo en una sucursal (tabla lote_insumo). */
public class LoteInsumo {

    private Integer idLote;
    private final int idInsumo;
    private final int idSucursal;
    private final String nroLote;
    private final LocalDate fechaVencimiento;
    private int cantidadActual;
    private String nombreInsumo;   // dato descriptivo para listados (no persistido en la tabla)

    public LoteInsumo(Integer idLote, int idInsumo, int idSucursal, String nroLote,
                      LocalDate fechaVencimiento, int cantidadActual) {
        if (fechaVencimiento == null) {
            throw new DatoInvalidoException("La fecha de vencimiento del lote es obligatoria");
        }
        if (cantidadActual < 0) {
            throw new DatoInvalidoException("La cantidad de un lote no puede ser negativa");
        }
        this.idLote = idLote;
        this.idInsumo = idInsumo;
        this.idSucursal = idSucursal;
        this.nroLote = Validaciones.texto(nroLote, "numero de lote", 20);
        this.fechaVencimiento = fechaVencimiento;
        this.cantidadActual = cantidadActual;
    }

    /** Constructor de copia. */
    public LoteInsumo(LoteInsumo otro) {
        this(otro.idLote, otro.idInsumo, otro.idSucursal, otro.nroLote, otro.fechaVencimiento, otro.cantidadActual);
        this.nombreInsumo = otro.nombreInsumo;
    }

    /** Un lote esta vigente hasta el mismo dia de su vencimiento inclusive. */
    public boolean estaVigente(LocalDate hoy) {
        return !fechaVencimiento.isBefore(hoy);
    }

    public long diasParaVencer(LocalDate hoy) {
        return ChronoUnit.DAYS.between(hoy, fechaVencimiento);
    }

    /** Aplica un movimiento polimorfico: el lote no necesita saber si es consumo o reposicion. */
    public void aplicar(MovimientoInventario movimiento) {
        if (movimiento.getIdLote() != idLote) {
            throw new DatoInvalidoException("El movimiento corresponde al lote " + movimiento.getIdLote());
        }
        this.cantidadActual = movimiento.aplicarA(cantidadActual);
    }

    public void asignarId(int id) {
        if (idLote != null) {
            throw new DatoInvalidoException("El lote ya tiene id " + idLote);
        }
        this.idLote = id;
    }

    public Integer getIdLote() {
        return idLote;
    }

    public int getIdInsumo() {
        return idInsumo;
    }

    public int getIdSucursal() {
        return idSucursal;
    }

    public String getNroLote() {
        return nroLote;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public int getCantidadActual() {
        return cantidadActual;
    }

    public String getNombreInsumo() {
        return nombreInsumo == null ? "Insumo #" + idInsumo : nombreInsumo;
    }

    public void setNombreInsumo(String nombreInsumo) {
        this.nombreInsumo = nombreInsumo;
    }

    @Override
    public String toString() {
        return String.format("Lote#%d %s [%s] vence %s, cantidad %d", idLote, getNombreInsumo(), nroLote,
                fechaVencimiento, cantidadActual);
    }
}
