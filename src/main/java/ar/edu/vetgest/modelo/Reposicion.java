package ar.edu.vetgest.modelo;

import java.time.LocalDateTime;

/** Ingreso de mercaderia a un lote (RF07). */
public class Reposicion extends MovimientoInventario {

    public Reposicion(int idLote, int idUsuario, int cantidad, LocalDateTime fecha) {
        super(idLote, idUsuario, cantidad, fecha);
    }

    @Override
    public TipoMovimiento getTipo() {
        return TipoMovimiento.REPOSICION;
    }

    @Override
    public int getDelta() {
        return getCantidad();
    }
}
