package ar.edu.vetgest.estructuras;

/**
 * Nodo simplemente enlazado compartido por ListaEnlazada, Cola y Pila.
 * Visibilidad de paquete: es un detalle de implementacion que no se expone.
 */
final class Nodo<T> {

    final T dato;
    Nodo<T> siguiente;

    Nodo(T dato) {
        this.dato = dato;
    }
}
