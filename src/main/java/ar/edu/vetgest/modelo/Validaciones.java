package ar.edu.vetgest.modelo;

import ar.edu.vetgest.excepcion.DatoInvalidoException;

/** Validaciones comunes que usan los constructores y setters del modelo. */
final class Validaciones {

    private Validaciones() {
    }

    /** Exige un texto no vacio de hasta {@code maximo} caracteres; devuelve el texto sin espacios extremos. */
    static String texto(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new DatoInvalidoException("El campo '" + campo + "' es obligatorio");
        }
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new DatoInvalidoException("El campo '" + campo + "' admite hasta " + maximo + " caracteres");
        }
        return limpio;
    }

    /** Igual que {@link #texto} pero admite nulo o vacio (devuelve null). */
    static String textoOpcional(String valor, String campo, int maximo) {
        return (valor == null || valor.isBlank()) ? null : texto(valor, campo, maximo);
    }

    static int positivo(int valor, String campo) {
        if (valor <= 0) {
            throw new DatoInvalidoException("El campo '" + campo + "' debe ser mayor a cero");
        }
        return valor;
    }
}
