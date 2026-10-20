package ar.edu.vetgest.estructuras;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstructurasTest {

    @Test
    @DisplayName("Lista enlazada: agregar, obtener, buscar y eliminar en cabeza, medio y final")
    void listaEnlazada() {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        lista.agregar("b");
        lista.agregar("c");
        lista.agregarAlInicio("a");
        lista.agregar("d");
        assertEquals(List.of("a", "b", "c", "d"), lista.aLista());
        assertEquals("c", lista.obtener(2));
        assertEquals("b", lista.buscar(s -> s.startsWith("b")).orElseThrow());
        assertTrue(lista.eliminarPrimero("a"::equals));     // cabeza
        assertTrue(lista.eliminarPrimero("c"::equals));     // medio
        assertTrue(lista.eliminarPrimero("d"::equals));     // final: debe actualizar la referencia al ultimo
        lista.agregar("e");
        assertEquals(List.of("b", "e"), lista.aLista());
        assertFalse(lista.eliminarPrimero("z"::equals));
        assertThrows(IndexOutOfBoundsException.class, () -> lista.obtener(5));
    }

    @Test
    @DisplayName("Cola: primero en entrar, primero en salir")
    void colaFifo() {
        Cola<Integer> cola = new Cola<>();
        cola.encolar(1);
        cola.encolar(2);
        cola.encolar(3);
        assertEquals(1, cola.desencolar());
        cola.encolar(4);
        assertEquals(2, cola.verFrente());
        assertEquals(3, cola.tamanio());
        cola.desencolar();
        cola.desencolar();
        cola.desencolar();
        assertTrue(cola.estaVacia());
        assertThrows(NoSuchElementException.class, cola::desencolar);
    }

    @Test
    @DisplayName("Pila: ultimo en entrar, primero en salir; el recorrido va del tope a la base")
    void pilaLifo() {
        Pila<String> pila = new Pila<>();
        pila.apilar("x");
        pila.apilar("y");
        pila.apilar("z");
        StringBuilder recorrido = new StringBuilder();
        for (String s : pila) {
            recorrido.append(s);
        }
        assertEquals("zyx", recorrido.toString());
        assertEquals("z", pila.desapilar());
        assertEquals("y", pila.verTope());
        assertEquals(2, pila.tamanio());
        pila.desapilar();
        pila.desapilar();
        assertThrows(NoSuchElementException.class, pila::desapilar);
    }
}
