package ar.edu.vetgest.servicio;

import ar.edu.vetgest.estructuras.Pila;

import java.time.Clock;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Registro de las operaciones de la sesion. Usa una Pila: se consulta de lo mas reciente a lo mas antiguo. */
public class Bitacora {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Pila<String> eventos = new Pila<>();
    private final Clock reloj;

    public Bitacora(Clock reloj) {
        this.reloj = reloj;
    }

    public void registrar(String evento) {
        eventos.apilar(LocalTime.now(reloj).format(HORA) + "  " + evento);
    }

    /** Ultimos {@code cantidad} eventos, el mas reciente primero (recorrido LIFO). */
    public List<String> ultimos(int cantidad) {
        List<String> resultado = new ArrayList<>();
        for (String e : eventos) {
            if (resultado.size() == cantidad) {
                break;
            }
            resultado.add(e);
        }
        return resultado;
    }

    public int cantidad() {
        return eventos.tamanio();
    }
}
