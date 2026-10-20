package ar.edu.vetgest.ui;

import ar.edu.vetgest.modelo.AlertaStock;
import ar.edu.vetgest.modelo.Insumo;
import ar.edu.vetgest.modelo.LoteInsumo;

import java.time.LocalDate;
import java.util.List;

/** CU10 reposicion y CU11 alertas y vencimientos (rol STOCK). */
public class MenuInventario extends Menu {

    private static final int DIAS_AVISO_VENCIMIENTO = 30;

    public MenuInventario(Consola consola, Sesion sesion) {
        super("INVENTARIO", consola, sesion);
    }

    @Override
    protected String[] opciones() {
        return new String[]{
                "Stock por lote de la sucursal",
                "Registrar reposicion de un lote existente (CU10)",
                "Ingresar un lote nuevo (CU10)",
                "Alertas de stock minimo de la red, por criticidad (CU11)",
                "Lotes vencidos o que vencen en " + DIAS_AVISO_VENCIMIENTO + " dias"};
    }

    @Override
    protected void ejecutarOpcion(int opcion) {
        switch (opcion) {
            case 1 -> Formato.lotes(consola, sesion.inventario().lotesDeSucursal(idSucursal()), hoy());
            case 2 -> reponer();
            case 3 -> loteNuevo();
            case 4 -> alertas();
            case 5 -> Formato.lotes(consola, sesion.inventario().lotesPorVencer(idSucursal(), DIAS_AVISO_VENCIMIENTO), hoy());
            default -> throw new IllegalStateException("Opcion no contemplada: " + opcion);
        }
    }

    private void reponer() {
        Formato.lotes(consola, sesion.inventario().lotesDeSucursal(idSucursal()), hoy());
        int idLote = consola.leerEntero("Id del lote: ", 1, Integer.MAX_VALUE);
        int cantidad = consola.leerEntero("Cantidad recibida: ", 1, 100_000);
        LoteInsumo lote = sesion.inventario().registrarReposicion(idLote, cantidad, sesion.getUsuario().getId());
        consola.ok("Reposicion registrada. " + lote);
        registrar("Reposicion de " + cantidad + " en lote " + idLote);
    }

    private void loteNuevo() {
        Insumo insumo = elegirInsumo();
        String nroLote = consola.leerTexto("Numero de lote: ");
        LocalDate vence = null;
        while (vence == null) {
            vence = consola.leerFecha("Vencimiento dd/mm/aaaa: ", null);
        }
        int cantidad = consola.leerEntero("Cantidad recibida: ", 1, 100_000);
        LoteInsumo lote = sesion.inventario().ingresarLoteNuevo(insumo.getId(), idSucursal(), nroLote, vence, cantidad,
                sesion.getUsuario().getId());
        consola.ok("Lote ingresado. " + lote);
        registrar("Alta del lote " + nroLote + " de " + insumo.getNombre());
    }

    private void alertas() {
        List<AlertaStock> alertas = sesion.inventario().alertasPorCriticidad();
        if (alertas.isEmpty()) {
            consola.println("  No hay insumos por debajo del minimo.");
        }
        for (AlertaStock a : alertas) {
            consola.println("  " + a);
        }
    }

    private int idSucursal() {
        return sesion.getSucursal().getId();
    }
}
