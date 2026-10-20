package ar.edu.vetgest.servicio;

import ar.edu.vetgest.modelo.AlertaStock;
import ar.edu.vetgest.modelo.ConsumoLote;

import java.util.List;

/** Resultado de registrar un consumo: como se distribuyo entre lotes (FEFO) y la alerta, si la hubo. */
public record ResultadoConsumo(List<ConsumoLote> distribucion, AlertaStock alerta) {

    public boolean generoAlerta() {
        return alerta != null;
    }
}
