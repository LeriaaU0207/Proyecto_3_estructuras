package com.una.socialuna.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Utilidades para leer y escribir archivos CSV.
 *
 * Formato:
 *  - Columnas separadas por coma; la primera línea es el encabezado.
 *  - Los campos que contienen comas, comillas o saltos de línea se escriben
 *    entre comillas dobles, y las comillas internas se duplican ("").
 *  - Codificación UTF-8 y saltos de línea \n.
 *
 * La escritura es segura: se escribe primero un archivo temporal (.tmp) y
 * luego se reemplaza el original, para no dejar un CSV a medias si el
 * programa se cierra durante el guardado.
 */
public final class CsvUtil {

    private CsvUtil() {
    }

    /**
     * Convierte un valor en un campo CSV válido (con comillas si hace falta).
     * Recibe: el texto del campo (null se trata como vacío).
     * Retorna: el campo listo para escribirse en el archivo.
     */
    public static String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        boolean requiereComillas = valor.indexOf(',') >= 0
                || valor.indexOf('"') >= 0
                || valor.indexOf('\n') >= 0
                || valor.indexOf('\r') >= 0;
        if (!requiereComillas) {
            return valor;
        }
        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }

    /**
     * Une varios campos en una sola línea CSV, escapando cada uno.
     * Recibe: los campos de la fila.
     * Retorna: la línea CSV (sin salto de línea final).
     */
    public static String unirLinea(String... campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escapar(campos[i]));
        }
        return sb.toString();
    }

    /**
     * Separa un texto CSV completo en registros y campos. Respeta los campos
     * entre comillas, que pueden contener comas y saltos de línea. Ignora las
     * líneas vacías y el retorno de carro (\r) fuera de comillas.
     * Recibe: el contenido completo del archivo.
     * Retorna: una lista con un arreglo de campos por cada registro
     *          (el encabezado viene incluido como primer registro).
     */
    public static ListaEnlazada<String[]> parsear(String contenido) {
        ListaEnlazada<String[]> registros = new ListaEnlazada<>();
        ListaEnlazada<String> campos = new ListaEnlazada<>();
        StringBuilder campo = new StringBuilder();
        boolean entreComillas = false;
        boolean registroConContenido = false;

        int n = contenido.length();
        for (int i = 0; i < n; i++) {
            char c = contenido.charAt(i);
            if (entreComillas) {
                if (c == '"') {
                    if (i + 1 < n && contenido.charAt(i + 1) == '"') {
                        campo.append('"'); // comilla escapada ("")
                        i++;
                    } else {
                        entreComillas = false;
                    }
                } else {
                    campo.append(c);
                }
            } else if (c == '"') {
                entreComillas = true;
                registroConContenido = true;
            } else if (c == ',') {
                campos.agregar(campo.toString());
                campo.setLength(0);
                registroConContenido = true;
            } else if (c == '\r') {
                // se ignora (archivos con saltos de línea de Windows)
            } else if (c == '\n') {
                if (registroConContenido) {
                    campos.agregar(campo.toString());
                    registros.agregar(aArreglo(campos));
                }
                campos = new ListaEnlazada<>();
                campo.setLength(0);
                registroConContenido = false;
            } else {
                campo.append(c);
                registroConContenido = true;
            }
        }
        // Último registro, si el archivo no termina en salto de línea.
        if (registroConContenido) {
            campos.agregar(campo.toString());
            registros.agregar(aArreglo(campos));
        }
        return registros;
    }

    private static String[] aArreglo(ListaEnlazada<String> campos) {
        String[] arreglo = new String[campos.tamano()];
        int i = 0;
        for (String campo : campos) {
            arreglo[i++] = campo;
        }
        return arreglo;
    }

    /**
     * Lee un archivo CSV y devuelve sus filas de datos (sin el encabezado).
     * Si el archivo no existe, devuelve una lista vacía.
     * Recibe: la ruta del archivo.
     * Retorna: una lista con un arreglo de campos por cada fila de datos.
     *
     * @throws IOException si el archivo existe pero no se puede leer
     */
    public static ListaEnlazada<String[]> leerRegistros(Path archivo) throws IOException {
        ListaEnlazada<String[]> filas = new ListaEnlazada<>();
        if (!Files.exists(archivo)) {
            return filas;
        }
        String contenido = Files.readString(archivo, StandardCharsets.UTF_8);
        if (!contenido.isEmpty() && contenido.charAt(0) == '\uFEFF') {
            contenido = contenido.substring(1); // BOM que agrega Excel
        }
        boolean esEncabezado = true;
        for (String[] registro : parsear(contenido)) {
            if (esEncabezado) {
                esEncabezado = false;
            } else {
                filas.agregar(registro);
            }
        }
        return filas;
    }

    /**
     * Escribe un archivo CSV completo (encabezado + filas), reemplazando el
     * anterior de forma segura. Crea la carpeta si no existe.
     * Recibe: la ruta del archivo, el encabezado y las filas a escribir.
     *
     * @throws IOException si no se puede escribir
     */
    public static void escribirArchivo(Path archivo, String[] encabezado,
                                       Iterable<String[]> filas) throws IOException {
        Path carpeta = archivo.toAbsolutePath().getParent();
        if (carpeta != null) {
            Files.createDirectories(carpeta);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(unirLinea(encabezado)).append('\n');
        for (String[] fila : filas) {
            sb.append(unirLinea(fila)).append('\n');
        }
        Path temporal = archivo.resolveSibling(archivo.getFileName() + ".tmp");
        Files.writeString(temporal, sb.toString(), StandardCharsets.UTF_8);
        try {
            Files.move(temporal, archivo,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Verifica que una fila tenga al menos la cantidad de columnas esperada.
     * Recibe: la fila, las columnas esperadas y el archivo (para el mensaje).
     *
     * @throws IOException si la fila tiene menos columnas de las esperadas
     */
    public static void exigirColumnas(String[] fila, int esperadas, Path archivo)
            throws IOException {
        if (fila.length < esperadas) {
            throw new IOException("Fila inválida en " + archivo.getFileName()
                    + ": se esperaban " + esperadas + " columnas y hay " + fila.length
                    + " -> " + String.join(" | ", fila));
        }
    }
}
