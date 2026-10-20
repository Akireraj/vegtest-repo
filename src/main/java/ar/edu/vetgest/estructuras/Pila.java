package ar.edu.vetgest.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Pila LIFO (ultimo en entrar, primero en salir) implementada con nodos
 * enlazados. Apilar y desapilar son O(1). Se usa para "deshacer la ultima
 * reserva" y para la bitacora de la sesion (lo mas reciente primero).
 */
public class Pila<T> implements Iterable<T> {

    private Nodo<T> tope;
    private int tamanio;

    public void apilar(T dato) {
        Objects.requireNonNull(dato, "La pila no admite elementos nulos");
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = tope;
        tope = nuevo;
        tamanio++;
    }

    public T desapilar() {
        if (estaVacia()) {
            throw new NoSuchElementException("La pila esta vacia");
        }
        T dato = tope.dato;
        tope = tope.siguiente;
        tamanio--;
        return dato;
    }

    public T verTope() {
        if (estaVacia()) {
            throw new NoSuchElementException("La pila esta vacia");
        }
        return tope.dato;
    }

    public boolean estaVacia() {
        return tope == null;
    }

    public int tamanio() {
        return tamanio;
    }

    /** Recorre desde el tope (lo mas reciente) hacia la base. */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = tope;

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
