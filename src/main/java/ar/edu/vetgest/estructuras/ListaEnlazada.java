package ar.edu.vetgest.estructuras;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Lista simplemente enlazada generica.
 * Mantiene referencias a la cabeza y al ultimo nodo, por lo que agregar al
 * final es O(1). Se usa como "tabla" del modo memoria y para las mascotas
 * de cada cliente.
 */
public class ListaEnlazada<T> implements Iterable<T> {

    private Nodo<T> cabeza;
    private Nodo<T> ultimo;
    private int tamanio;

    /** Agrega al final de la lista. O(1). */
    public void agregar(T dato) {
        Objects.requireNonNull(dato, "La lista no admite elementos nulos");
        Nodo<T> nuevo = new Nodo<>(dato);
        if (cabeza == null) {
            cabeza = nuevo;
            ultimo = nuevo;
        } else {
            ultimo.siguiente = nuevo;
            ultimo = nuevo;
        }
        tamanio++;
    }

    /** Agrega al comienzo de la lista. O(1). */
    public void agregarAlInicio(T dato) {
        Objects.requireNonNull(dato, "La lista no admite elementos nulos");
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = cabeza;
        cabeza = nuevo;
        if (ultimo == null) {
            ultimo = nuevo;
        }
        tamanio++;
    }

    /** Devuelve el elemento de la posicion indicada. O(n). */
    public T obtener(int indice) {
        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException("Indice " + indice + " fuera de rango [0, " + tamanio + ")");
        }
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.siguiente;
        }
        return actual.dato;
    }

    /** Busqueda lineal del primer elemento que cumple el criterio. O(n). */
    public Optional<T> buscar(Predicate<? super T> criterio) {
        for (Nodo<T> n = cabeza; n != null; n = n.siguiente) {
            if (criterio.test(n.dato)) {
                return Optional.of(n.dato);
            }
        }
        return Optional.empty();
    }

    /** Devuelve una nueva lista con los elementos que cumplen el criterio. */
    public ListaEnlazada<T> filtrar(Predicate<? super T> criterio) {
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        for (T dato : this) {
            if (criterio.test(dato)) {
                resultado.agregar(dato);
            }
        }
        return resultado;
    }

    /** Elimina el primer elemento que cumple el criterio, reenlazando los nodos. */
    public boolean eliminarPrimero(Predicate<? super T> criterio) {
        Nodo<T> anterior = null;
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (criterio.test(actual.dato)) {
                if (anterior == null) {
                    cabeza = actual.siguiente;
                } else {
                    anterior.siguiente = actual.siguiente;
                }
                if (actual == ultimo) {
                    ultimo = anterior;
                }
                tamanio--;
                return true;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    /** Copia superficial: protege la lista interna de quien la consulta. */
    public ListaEnlazada<T> copia() {
        return filtrar(x -> true);
    }

    public List<T> aLista() {
        List<T> lista = new ArrayList<>(tamanio);
        for (T dato : this) {
            lista.add(dato);
        }
        return lista;
    }

    public static <T> ListaEnlazada<T> de(Iterable<T> origen) {
        ListaEnlazada<T> lista = new ListaEnlazada<>();
        for (T dato : origen) {
            lista.agregar(dato);
        }
        return lista;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = cabeza;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException();
                }
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (Nodo<T> n = cabeza; n != null; n = n.siguiente) {
            sb.append(n.dato);
            if (n.siguiente != null) {
                sb.append(", ");
            }
        }
        return sb.append(']').toString();
    }
}
