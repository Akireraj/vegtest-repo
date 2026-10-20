package ar.edu.vetgest.modelo;

import java.time.LocalDateTime;

/**
 * Consumo de insumos durante una atencion (RF06). El id de historia es un
 * int primitivo y obligatorio en el constructor: el compilador garantiza la
 * misma regla que el CHECK ck_mov_consumo de la base de datos.
 */
public class Consumo extends MovimientoInventario {

    private final int idHistoria;

    public Consumo(int idLote, int idHistoria, int idUsuario, int cantidad, LocalDateTime fecha) {
        super(idLote, idUsuario, cantidad, fecha);
        this.idHistoria = idHistoria;
    }

    @Override
    public TipoMovimiento getTipo() {
        return TipoMovimiento.CONSUMO;
    }

    @Override
    public int getDelta() {
        return -getCantidad();
    }

    @Override
    public Integer getIdHistoria() {
        return idHistoria;
    }
}
