package ar.edu.vetgest.modelo;

/** Parte de un plan FEFO: cuanto se toma de cada lote. Inmutable. */
public final class ConsumoLote {

    private final int idLote;
    private final int cantidad;

    public ConsumoLote(int idLote, int cantidad) {
        this.idLote = idLote;
        this.cantidad = Validaciones.positivo(cantidad, "cantidad");
    }

    public int getIdLote() {
        return idLote;
    }

    public int getCantidad() {
        return cantidad;
    }

    @Override
    public String toString() {
        return "lote " + idLote + ": " + cantidad;
    }
}
