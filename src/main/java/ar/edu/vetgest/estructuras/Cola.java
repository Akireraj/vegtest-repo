package ar.edu.vetgest.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Cola FIFO (primero en entrar, primero en salir) implementada con nodos
 * enlazados. Encolar y desencolar son O(1). Modela la sala de espera:
 * los pacientes se atienden en el orden en que llegaron.
 */
public class Cola<T> implements Iterable<T> {

    private Nodo<T> frente;
    private Nodo<T> fin;
    private int tamanio;

    public void encolar(T dato) {
        Objects.requireNonNull(dato, "La cola no admite elementos nulos");
        Nodo<T> nuevo = new Nodo<>(dato);
        if (fin == null) {
            frente = nuevo;
        } else {
            fin.siguiente = nuevo;
        }
        fin = nuevo;
        tamanio++;
    }

    /** Quita y devuelve el elemento del frente. Lanza NoSuchElementException si esta vacia. */
    public T desencolar() {
        if (estaVacia()) {
            throw new NoSuchElementException("La cola esta vacia");
        }
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;
        }
        tamanio--;
        return dato;
    }

    public T verFrente() {
        if (estaVacia()) {
            throw new NoSuchElementException("La cola esta vacia");
        }
        return frente.dato;
    }

    public boolean contiene(Predicate<? super T> criterio) {
        for (T dato : this) {
            if (criterio.test(dato)) {
                return true;
            }
        }
        return false;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    public int tamanio() {
        return tamanio;
    }

    /** Recorre desde el frente hacia el final, sin modificar la cola. */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = frente;

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
}
