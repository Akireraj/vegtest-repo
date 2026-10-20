package ar.edu.vetgest.algoritmos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlgoritmosTest {

    @Test
    @DisplayName("Merge sort ordena, es estable y no modifica la lista original")
    void mergeSort() {
        List<String> original = List.of("Paz:2", "Gomez:1", "Paz:1", "Diaz:3", "Gomez:2");
        List<String> ordenada = Ordenamiento.mergeSort(original, Comparator.comparing(s -> s.split(":")[0]));
        assertEquals(List.of("Diaz:3", "Gomez:1", "Gomez:2", "Paz:2", "Paz:1"), ordenada);   // estabilidad
        assertEquals("Paz:2", original.get(0));
        assertEquals(List.of(), Ordenamiento.mergeSort(List.<Integer>of(), Comparator.naturalOrder()));
    }

    @Test
    @DisplayName("Insercion ordena en el lugar")
    void insercion() {
        List<Integer> datos = new ArrayList<>(List.of(5, 2, 9, 1, 5, 6));
        Ordenamiento.insercion(datos, Comparator.naturalOrder());
        assertEquals(List.of(1, 2, 5, 5, 6, 9), datos);
    }

    @Test
    @DisplayName("Busqueda binaria encuentra extremos, centro y devuelve -1 si no existe")
    void busquedaBinaria() {
        List<String> dnis = List.of("28999333", "29555666", "30111222", "33777888", "35444555", "40123456");
        Function<String, String> id = Function.identity();
        assertEquals(0, Busqueda.binaria(dnis, "28999333", id, Comparator.naturalOrder()));
        assertEquals(5, Busqueda.binaria(dnis, "40123456", id, Comparator.naturalOrder()));
        assertEquals(3, Busqueda.binaria(dnis, "33777888", id, Comparator.naturalOrder()));
        assertEquals(-1, Busqueda.binaria(dnis, "11111111", id, Comparator.naturalOrder()));
        assertEquals(-1, Busqueda.binaria(List.of(), "1", id, Comparator.naturalOrder()));
    }

    @Test
    @DisplayName("Busqueda lineal ignora mayusculas y tildes")
    void busquedaLineal() {
        List<String> nombres = List.of("Toby", "Luna", "Pancho");
        assertEquals("Luna", Busqueda.lineal(nombres, n -> Busqueda.normalizar(n).contains(Busqueda.normalizar("LÚ")))
                .orElseThrow());
    }
}
