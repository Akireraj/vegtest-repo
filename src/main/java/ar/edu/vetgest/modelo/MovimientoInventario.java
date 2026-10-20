package ar.edu.vetgest.modelo;

import ar.edu.vetgest.excepcion.StockInsuficienteException;

import java.time.LocalDateTime;

/**
 * ABSTRACCION + POLIMORFISMO: un movimiento de inventario modifica el stock
 * de un lote. Cada subclase define su tipo y el signo de la variacion
 * (getDelta). El DAO JDBC y el DAO en memoria procesan una lista de
 * movimientos sin preguntar de que clase concreta es cada uno.
 */
public abstract class MovimientoInventario {

    private final int idLote;
    private final int idUsuario;
    private final int cantidad;
    private final LocalDateTime fecha;

    protected MovimientoInventario(int idLote, int idUsuario, int cantidad, LocalDateTime fecha) {
        this.idLote = idLote;
        this.idUsuario = idUsuario;
        this.cantidad = Validaciones.positivo(cantidad, "cantidad");   // CHECK ck_mov_cantidad
        this.fecha = (fecha == null) ? LocalDateTime.now() : fecha;
    }

    public abstract TipoMovimiento getTipo();

    /** Variacion que produce en el stock del lote: positiva (ingreso) o negativa (egreso). */
    public abstract int getDelta();

    /** Por defecto un movimiento no se asocia a una historia clinica. */
    public Integer getIdHistoria() {
        return null;
    }

    /** Metodo plantilla (final): calcula el nuevo stock usando el getDelta() de la subclase. */
    public final int aplicarA(int stockActual) {
        int resultado = stockActual + getDelta();
        if (resultado < 0) {
            throw new StockInsuficienteException(stockActual, cantidad);
        }
        return resultado;
    }

    public int getIdLote() {
        return idLote;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    @Override
    public String toString() {
        return String.format("%s lote %d: %+d", getTipo(), idLote, getDelta());
    }
}
