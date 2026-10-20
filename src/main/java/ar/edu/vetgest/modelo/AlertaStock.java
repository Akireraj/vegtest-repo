package ar.edu.vetgest.modelo;

/** Insumo cuyo stock vigente en una sucursal es igual o inferior al minimo (RF08). */
public class AlertaStock {

    private final int idSucursal;
    private final String sucursal;
    private final int idInsumo;
    private final String insumo;
    private final int stockVigente;
    private final int stockMinimo;

    public AlertaStock(int idSucursal, String sucursal, int idInsumo, String insumo, int stockVigente,
                       int stockMinimo) {
        this.idSucursal = idSucursal;
        this.sucursal = sucursal;
        this.idInsumo = idInsumo;
        this.insumo = insumo;
        this.stockVigente = stockVigente;
        this.stockMinimo = stockMinimo;
    }

    /** Proporcion del minimo que cubre el stock actual: 0 = agotado, 1 = justo en el minimo. */
    public double getCobertura() {
        return stockMinimo == 0 ? 1.0 : (double) stockVigente / stockMinimo;
    }

    public int getIdSucursal() {
        return idSucursal;
    }

    public String getSucursal() {
        return sucursal;
    }

    public int getIdInsumo() {
        return idInsumo;
    }

    public String getInsumo() {
        return insumo;
    }

    public int getStockVigente() {
        return stockVigente;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    @Override
    public String toString() {
        return String.format("[ALERTA] %-14s %-26s stock %3d / minimo %3d (%3.0f%%)", sucursal, insumo,
                stockVigente, stockMinimo, getCobertura() * 100);
    }
}
