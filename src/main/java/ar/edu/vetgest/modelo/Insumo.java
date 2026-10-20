package ar.edu.vetgest.modelo;

/** Insumo del catalogo (tabla insumo). El stock no se guarda aqui: se calcula por lotes. */
public class Insumo {

    private final int id;
    private final String codigo;
    private final String nombre;
    private final String unidad;
    private final int stockMinimo;

    public Insumo(int id, String codigo, String nombre, String unidad, int stockMinimo) {
        this.id = id;
        this.codigo = Validaciones.texto(codigo, "codigo", 15);
        this.nombre = Validaciones.texto(nombre, "nombre del insumo", 80);
        this.unidad = Validaciones.texto(unidad, "unidad", 15);
        this.stockMinimo = Math.max(0, stockMinimo);
    }

    public int getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUnidad() {
        return unidad;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (" + unidad + ")";
    }
}
