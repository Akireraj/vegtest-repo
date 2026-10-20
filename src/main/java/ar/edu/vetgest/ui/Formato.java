package ar.edu.vetgest.ui;

import ar.edu.vetgest.modelo.EntradaHistorial;
import ar.edu.vetgest.modelo.ItemAgenda;
import ar.edu.vetgest.modelo.LoteInsumo;
import ar.edu.vetgest.modelo.Turno;

import java.time.LocalDate;
import java.util.List;

/** Impresion tabular de listados en la consola. */
final class Formato {

    private Formato() {
    }

    static String recortar(String texto, int maximo) {
        if (texto == null) {
            return "-";
        }
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1) + ".";
    }

    static void agenda(Consola c, List<ItemAgenda> items) {
        if (items.isEmpty()) {
            c.println("  (no hay turnos para esa fecha)");
            return;
        }
        c.printf("  %-4s %-5s %-4s %-16s %-8s %-18s %-24s %s%n", "Nro", "Hora", "Min", "Veterinario/a", "Mascota",
                "Cliente", "Motivo", "Estado");
        for (ItemAgenda i : items) {
            Turno t = i.turno();
            c.printf("  %-4d %-5s %-4d %-16s %-8s %-18s %-24s %s%n", t.getIdTurno(),
                    t.getFechaHora().toLocalTime().format(Consola.HORA), t.getDuracionMin(),
                    recortar(i.veterinario(), 16), recortar(i.mascota(), 8), recortar(i.cliente(), 18),
                    recortar(t.getMotivo(), 24), t.getEstado());
        }
    }

    static void lotes(Consola c, List<LoteInsumo> lotes, LocalDate hoy) {
        if (lotes.isEmpty()) {
            c.println("  (sin lotes)");
            return;
        }
        c.printf("  %-4s %-26s %-9s %-11s %6s  %s%n", "Id", "Insumo", "Lote", "Vence", "Cant.", "Situacion");
        for (LoteInsumo l : lotes) {
            String situacion;
            long dias = l.diasParaVencer(hoy);
            if (dias < 0) {
                situacion = "VENCIDO";
            } else if (dias <= 30) {
                situacion = "vence en " + dias + " dias";
            } else {
                situacion = "vigente";
            }
            c.printf("  %-4d %-26s %-9s %-11s %6d  %s%n", l.getIdLote(), recortar(l.getNombreInsumo(), 26),
                    l.getNroLote(), l.getFechaVencimiento().format(Consola.FECHA), l.getCantidadActual(), situacion);
        }
    }

    static void historial(Consola c, List<EntradaHistorial> historial) {
        if (historial.isEmpty()) {
            c.println("  (la mascota no registra atenciones)");
            return;
        }
        for (EntradaHistorial h : historial) {
            c.printf("  %s | %-13s | %-16s | %6s kg | %s -> %s%n", h.fecha().format(Consola.FECHA), h.sucursal(),
                    recortar(h.veterinario(), 16), h.pesoKg(), h.diagnostico(), h.tratamiento());
        }
    }
}
