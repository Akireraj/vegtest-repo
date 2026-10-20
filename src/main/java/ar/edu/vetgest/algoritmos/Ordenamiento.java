package ar.edu.vetgest.algoritmos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Algoritmos de ordenamiento implementados sin usar Collections.sort,
 * con fines didacticos y de control del criterio.
 */
public final class Ordenamiento {

    private Ordenamiento() {
        // clase utilitaria: no se instancia
    }

    /**
     * Merge sort (divide y venceras). Complejidad O(n log n) en todos los
     * casos y ESTABLE: los elementos iguales conservan su orden relativo,
     * lo que permite ordenar por un criterio secundario y luego por uno
     * principal. Devuelve una lista nueva y no modifica la original.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> mergeSort(List<T> lista, Comparator<? super T> criterio) {
        Object[] datos = lista.toArray();
        if (datos.length > 1) {
            ordenar(datos, new Object[datos.length], 0, datos.length - 1, (Comparator<Object>) criterio);
        }
        List<T> resultado = new ArrayList<>(datos.length);
        for (Object o : datos) {
            resultado.add((T) o);
        }
        return resultado;
    }

    private static void ordenar(Object[] a, Object[] aux, int inicio, int fin, Comparator<Object> criterio) {
        if (inicio >= fin) {
            return;                                   // caso base: 0 o 1 elemento
        }
        int medio = (inicio + fin) >>> 1;
        ordenar(a, aux, inicio, medio, criterio);     // mitad izquierda
        ordenar(a, aux, medio + 1, fin, criterio);    // mitad derecha
        mezclar(a, aux, inicio, medio, fin, criterio);
    }

    private static void mezclar(Object[] a, Object[] aux, int inicio, int medio, int fin, Comparator<Object> criterio) {
        int i = inicio;
        int j = medio + 1;
        int k = inicio;
        while (i <= medio && j <= fin) {
            // ante igualdad se toma el de la izquierda: garantiza estabilidad
            if (criterio.compare(a[j], a[i]) < 0) {
                aux[k++] = a[j++];
            } else {
                aux[k++] = a[i++];
            }
        }
        while (i <= medio) {
            aux[k++] = a[i++];
        }
        while (j <= fin) {
            aux[k++] = a[j++];
        }
        System.arraycopy(aux, inicio, a, inicio, fin - inicio + 1);
    }

    /**
     * Ordenamiento por insercion, en el lugar. O(n^2) en el peor caso pero
     * O(n) si la lista ya esta casi ordenada y muy eficiente para listas
     * chicas: se usa para ordenar los lotes de un insumo (FEFO), que en una
     * sucursal rara vez superan la decena.
     */
    public static <T> void insercion(List<T> lista, Comparator<? super T> criterio) {
        for (int i = 1; i < lista.size(); i++) {
            T actual = lista.get(i);
            int j = i - 1;
            while (j >= 0 && criterio.compare(lista.get(j), actual) > 0) {
                lista.set(j + 1, lista.get(j));     // desplaza a la derecha
                j--;
            }
            lista.set(j + 1, actual);
        }
    }
}
