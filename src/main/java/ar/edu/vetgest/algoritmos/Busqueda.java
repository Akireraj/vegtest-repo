package ar.edu.vetgest.algoritmos;

import ar.edu.vetgest.estructuras.ListaEnlazada;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/** Algoritmos de busqueda lineal y binaria. */
public final class Busqueda {

    private Busqueda() {
    }

    /**
     * Busqueda binaria sobre una lista ORDENADA por la misma clave.
     * Complejidad O(log n): con 23.000 registros requiere a lo sumo 15
     * comparaciones. Devuelve la posicion encontrada o -1.
     *
     * @param ordenada  lista ordenada segun {@code extractor} y {@code criterio}
     * @param clave     valor buscado (por ejemplo, un DNI)
     * @param extractor obtiene la clave de cada elemento
     */
    public static <T, K> int binaria(List<T> ordenada, K clave,
                                     Function<? super T, ? extends K> extractor,
                                     Comparator<? super K> criterio) {
        int izquierda = 0;
        int derecha = ordenada.size() - 1;
        while (izquierda <= derecha) {
            int medio = (izquierda + derecha) >>> 1;
            int comparacion = criterio.compare(extractor.apply(ordenada.get(medio)), clave);
            if (comparacion == 0) {
                return medio;
            } else if (comparacion < 0) {
                izquierda = medio + 1;     // la clave esta en la mitad derecha
            } else {
                derecha = medio - 1;       // la clave esta en la mitad izquierda
            }
        }
        return -1;
    }

    /** Busqueda lineal: primer elemento que cumple el criterio. O(n). */
    public static <T> Optional<T> lineal(Iterable<T> datos, Predicate<? super T> criterio) {
        for (T dato : datos) {
            if (criterio.test(dato)) {
                return Optional.of(dato);
            }
        }
        return Optional.empty();
    }

    /** Busqueda lineal de todas las coincidencias. O(n). */
    public static <T> ListaEnlazada<T> linealTodos(Iterable<T> datos, Predicate<? super T> criterio) {
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        for (T dato : datos) {
            if (criterio.test(dato)) {
                resultado.agregar(dato);
            }
        }
        return resultado;
    }

    /** Pasa a minusculas y quita tildes, para que "lú" encuentre a "Luna". */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase().trim();
    }
}
