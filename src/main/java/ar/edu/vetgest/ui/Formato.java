package ar.edu.vetgest.ui;

import ar.edu.vetgest.modelo.EntradaHistorial;
import ar.edu.vetgest.modelo.ItemAgenda;
import ar.edu.vetgest.modelo.LoteInsumo;
import ar.edu.vetgest.modelo.EstadoTurno;
import ar.edu.vetgest.modelo.Turno;
import ar.edu.vetgest.modelo.Veterinario;
import ar.edu.vetgest.servicio.GrillaDisponibilidad;

import java.time.LocalDate;
import java.time.LocalTime;
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

    /** Grilla veterinario x franja: '#' ocupado, '.' libre; la cabecera marca cada hora. */
    static void grilla(Consola c, GrillaDisponibilidad g) {
        StringBuilder cabecera = new StringBuilder();
        for (int franja = 0; franja < GrillaDisponibilidad.FRANJAS; franja += 60 / GrillaDisponibilidad.MINUTOS_FRANJA) {
            cabecera.append(String.format("%02d  ", GrillaDisponibilidad.horaDeFranja(franja).getHour()));
        }
        c.printf("  %-18s %s %s%n", "", cabecera, "Ocup.");
        Veterinario[] filas = g.getVeterinarios();
        for (int i = 0; i < filas.length; i++) {
            c.printf("  %-18s %s %3d%%%n", recortar(filas[i].getNombreCompleto(), 18), g.filaComoTexto(i),
                    g.porcentajeOcupacion(i));
        }
        int[] porEstado = g.getTurnosPorEstado();
        EstadoTurno[] estados = EstadoTurno.values();
        StringBuilder resumen = new StringBuilder("  Turnos del dia por estado:");
        for (int i = 0; i < estados.length; i++) {
            resumen.append(i == 0 ? " " : " | ").append(estados[i]).append(' ').append(porEstado[i]);
        }
        c.println(resumen.toString());
    }

    /** Horarios en filas de ocho, para que la lista quepa en la consola. */
    static void horarios(Consola c, List<LocalTime> horarios) {
        if (horarios.isEmpty()) {
            c.println("  (no hay horarios libres)");
            return;
        }
        StringBuilder linea = new StringBuilder("  ");
        for (int i = 0; i < horarios.size(); i++) {
            linea.append(horarios.get(i).format(Consola.HORA)).append(' ');
            if ((i + 1) % 8 == 0 || i == horarios.size() - 1) {
                c.println(linea.toString());
                linea.setLength(2);
            }
        }
    }

    /** Muestra un reporte recuperado de un archivo; el ancho de cada columna se calcula en un int[]. */
    static void tabla(Consola c, List<String[]> filas) {
        if (filas.isEmpty()) {
            c.println("  (archivo vacio)");
            return;
        }
        int columnas = filas.get(0).length;
        int[] anchos = new int[columnas];
        for (String[] fila : filas) {
            for (int j = 0; j < columnas && j < fila.length; j++) {
                anchos[j] = Math.min(24, Math.max(anchos[j], fila[j].length()));
            }
        }
        for (String[] fila : filas) {
            StringBuilder sb = new StringBuilder("  ");
            for (int j = 0; j < columnas; j++) {
                String celda = j < fila.length ? recortar(fila[j], anchos[j]) : "";
                sb.append(String.format("%-" + anchos[j] + "s", celda)).append(j < columnas - 1 ? " | " : "");
            }
            c.println(sb.toString());
        }
    }
}
