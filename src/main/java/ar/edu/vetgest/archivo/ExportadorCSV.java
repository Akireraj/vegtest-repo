package ar.edu.vetgest.archivo;

import ar.edu.vetgest.excepcion.ArchivoException;
import ar.edu.vetgest.excepcion.DatoInvalidoException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y recupera reportes en archivos CSV (separador punto y coma, UTF-8),
 * que se abren directamente con Excel o LibreOffice en configuracion regional
 * de Argentina.
 * <p>
 * Uso complementario de arreglos y ArrayList: cada fila es un {@code String[]}
 * de tamano fijo (la cantidad de columnas la define el arreglo de encabezados)
 * y el conjunto de filas es un {@code ArrayList<String[]>}, porque su cantidad
 * depende de la consulta.
 */
public final class ExportadorCSV {

    public static final char SEPARADOR = ';';
    private static final char BOM = '\uFEFF';   // marca UTF-8: Excel reconoce las tildes

    private ExportadorCSV() {
    }

    /**
     * Escribe el encabezado y las filas en {@code destino}. Valida todo antes
     * de abrir el archivo, para no dejar un archivo a medio escribir.
     *
     * @return cantidad de filas de datos escritas
     */
    public static int escribir(Path destino, String[] encabezados, List<String[]> filas) {
        if (encabezados == null || encabezados.length == 0) {
            throw new DatoInvalidoException("El reporte debe tener al menos una columna");
        }
        for (int i = 0; i < filas.size(); i++) {
            if (filas.get(i).length != encabezados.length) {
                throw new DatoInvalidoException("La fila " + (i + 1) + " tiene " + filas.get(i).length
                        + " columnas y el encabezado " + encabezados.length);
            }
        }
        try {
            Path carpeta = destino.toAbsolutePath().getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            try (BufferedWriter w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
                w.write(BOM);
                w.write(linea(encabezados));
                w.newLine();
                for (String[] fila : filas) {
                    w.write(linea(fila));
                    w.newLine();
                }
            }
            return filas.size();
        } catch (IOException e) {
            throw new ArchivoException("No se pudo guardar el archivo " + destino + ": " + e.getMessage(), e);
        }
    }

    /** Recupera un CSV escrito por {@link #escribir}: la primera fila del resultado es el encabezado. */
    public static List<String[]> leer(Path origen) {
        List<String[]> filas = new ArrayList<>();
        try (BufferedReader r = Files.newBufferedReader(origen, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = r.readLine()) != null) {
                if (filas.isEmpty() && !linea.isEmpty() && linea.charAt(0) == BOM) {
                    linea = linea.substring(1);
                }
                if (!linea.isBlank()) {
                    filas.add(separar(linea));
                }
            }
        } catch (IOException e) {
            throw new ArchivoException("No se pudo leer el archivo " + origen + ": " + e.getMessage(), e);
        }
        return filas;
    }

    // ------------------------------------------------------------------ formato CSV
    private static String linea(String[] campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                sb.append(SEPARADOR);
            }
            sb.append(escapar(campos[i]));
        }
        return sb.toString();
    }

    /** Encierra entre comillas los campos con separador o comillas, duplicando las comillas internas. */
    private static String escapar(String campo) {
        String texto = (campo == null) ? "" : campo.replace('\n', ' ').replace('\r', ' ');
        if (texto.indexOf(SEPARADOR) >= 0 || texto.indexOf('"') >= 0) {
            return '"' + texto.replace("\"", "\"\"") + '"';
        }
        return texto;
    }

    /** Separa una linea en campos respetando las comillas; devuelve un arreglo del tamano exacto. */
    private static String[] separar(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean entreComillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (entreComillas) {
                if (c == '"' && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    actual.append('"');
                    i++;
                } else if (c == '"') {
                    entreComillas = false;
                } else {
                    actual.append(c);
                }
            } else if (c == '"') {
                entreComillas = true;
            } else if (c == SEPARADOR) {
                campos.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString());
        return campos.toArray(new String[0]);    // ArrayList -> arreglo
    }
}
