package ar.edu.vetgest.ui;

import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Lectura validada de datos por consola. Cada metodo repite la pregunta
 * (estructura while) hasta obtener un valor valido y captura las
 * excepciones de conversion (NumberFormatException, DateTimeParseException).
 */
public class Consola {

    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Scanner entrada;
    private final PrintStream salida;
    private final boolean eco;   // repite lo ingresado (util cuando la entrada se redirige desde un archivo)

    public Consola(InputStream in, PrintStream out) {
        this.entrada = new Scanner(in, "UTF-8");
        this.salida = out;
        this.eco = Boolean.getBoolean("vetgest.eco");
    }

    public Consola() {
        this(System.in, System.out);
    }

    // ------------------------------------------------------------------ salida
    public void println(String texto) {
        salida.println(texto);
    }

    public void printf(String formato, Object... args) {
        salida.printf(formato, args);
    }

    public void titulo(String texto) {
        salida.println();
        salida.println("=".repeat(Math.max(60, texto.length() + 4)));
        salida.println("  " + texto);
        salida.println("=".repeat(Math.max(60, texto.length() + 4)));
    }

    public void ok(String mensaje) {
        salida.println("  OK -> " + mensaje);
    }

    public void error(String mensaje) {
        salida.println("  ERROR -> " + mensaje);
    }

    public void aviso(String mensaje) {
        salida.println("  AVISO -> " + mensaje);
    }

    public PrintStream salida() {
        return salida;
    }

    // ------------------------------------------------------------------ entrada
    public String leerLinea(String mensaje) {
        salida.print(mensaje);
        if (!entrada.hasNextLine()) {
            throw new EntradaFinalizadaException();
        }
        String linea = entrada.nextLine().trim();
        if (eco) {
            salida.println(linea);
        }
        return linea;
    }

    public int leerEntero(String mensaje, int minimo, int maximo) {
        while (true) {
            Integer valor = leerEnteroOpcional(mensaje, minimo, maximo);
            if (valor != null) {
                return valor;
            }
            error("Debe ingresar un valor");
        }
    }

    /** Devuelve null si se presiona Enter sin escribir nada. */
    public Integer leerEnteroOpcional(String mensaje, int minimo, int maximo) {
        while (true) {
            String texto = leerLinea(mensaje);
            if (texto.isEmpty()) {
                return null;
            }
            try {
                int valor = Integer.parseInt(texto);
                if (valor < minimo || valor > maximo) {
                    error("Ingrese un numero entre " + minimo + " y " + maximo);
                } else {
                    return valor;
                }
            } catch (NumberFormatException e) {
                error("'" + texto + "' no es un numero entero valido");
            }
        }
    }

    public String leerTexto(String mensaje) {
        String texto;
        do {
            texto = leerLinea(mensaje);
            if (texto.isEmpty()) {
                error("El dato es obligatorio");
            }
        } while (texto.isEmpty());
        return texto;
    }

    public String leerTextoOpcional(String mensaje) {
        String texto = leerLinea(mensaje);
        return texto.isEmpty() ? null : texto;
    }

    public BigDecimal leerDecimalPositivo(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje).replace(',', '.');   // admite coma decimal
            try {
                BigDecimal valor = new BigDecimal(texto);
                if (valor.signum() > 0) {
                    return valor;
                }
                error("El valor debe ser mayor a cero");
            } catch (NumberFormatException e) {
                error("'" + texto + "' no es un numero valido (ejemplo: 12.5)");
            }
        }
    }

    /** Si se presiona Enter devuelve {@code porDefecto} (que puede ser null). */
    public LocalDate leerFecha(String mensaje, LocalDate porDefecto) {
        while (true) {
            String texto = leerLinea(mensaje);
            if (texto.isEmpty()) {
                return porDefecto;
            }
            try {
                return LocalDate.parse(texto, FECHA);
            } catch (DateTimeParseException e) {
                error("Fecha invalida. Use el formato dd/mm/aaaa");
            }
        }
    }

    public LocalTime leerHora(String mensaje, LocalTime porDefecto) {
        while (true) {
            String texto = leerLinea(mensaje);
            if (texto.isEmpty() && porDefecto != null) {
                return porDefecto;
            }
            try {
                return LocalTime.parse(texto, HORA);
            } catch (DateTimeParseException e) {
                error("Hora invalida. Use el formato HH:mm (ejemplo: 09:30)");
            }
        }
    }

    public LocalDateTime leerFechaHora(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje);
            try {
                return LocalDateTime.parse(texto, FECHA_HORA);
            } catch (DateTimeParseException e) {
                error("Use el formato dd/mm/aaaa HH:mm (ejemplo: 05/11/2026 10:30)");
            }
        }
    }

    public boolean confirmar(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje).toLowerCase();
            switch (texto) {
                case "s", "si" -> {
                    return true;
                }
                case "n", "no" -> {
                    return false;
                }
                default -> error("Responda s o n");
            }
        }
    }

    /** Lee un caracter que debe pertenecer a {@code permitidos} (por ejemplo "MH"). */
    public char leerOpcion(String mensaje, String permitidos) {
        while (true) {
            String texto = leerLinea(mensaje).toUpperCase();
            if (texto.length() == 1 && permitidos.indexOf(texto.charAt(0)) >= 0) {
                return texto.charAt(0);
            }
            error("Opcion invalida. Valores permitidos: " + permitidos);
        }
    }
}
