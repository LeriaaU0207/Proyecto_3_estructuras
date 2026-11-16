package com.una.socialuna.service;

import com.una.socialuna.util.CsvUtil;
import com.una.socialuna.util.ListaEnlazada;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Lee y escribe me_gusta.csv.
 * Columnas: idPublicacion,idUsuario
 *
 * Cada fila representa un "me gusta" de un usuario a una publicación.
 * Se guarda en un archivo aparte para no cambiar el formato de
 * publicaciones.csv y mantener compatibles los datos anteriores.
 */
public class RepositorioMeGusta {

    private static final String[] ENCABEZADO = {"idPublicacion", "idUsuario"};

    private final Path archivo;

    /**
     * Recibe: la ruta del archivo me_gusta.csv.
     */
    public RepositorioMeGusta(Path archivo) {
        this.archivo = archivo;
    }

    /**
     * Retorna: true si el archivo ya existe en disco.
     */
    public boolean existe() {
        return Files.exists(archivo);
    }

    /**
     * Lee todos los "me gusta" del archivo.
     * Retorna: una lista de pares [idPublicacion, idUsuario] (vacía si no existe).
     *
     * @throws IOException si el archivo no se puede leer o tiene datos inválidos
     */
    public ListaEnlazada<int[]> cargar() throws IOException {
        ListaEnlazada<int[]> meGusta = new ListaEnlazada<>();
        for (String[] fila : CsvUtil.leerRegistros(archivo)) {
            CsvUtil.exigirColumnas(fila, ENCABEZADO.length, archivo);
            try {
                meGusta.agregar(new int[]{
                        Integer.parseInt(fila[0].trim()),
                        Integer.parseInt(fila[1].trim())});
            } catch (NumberFormatException e) {
                throw new IOException("Dato inválido en " + archivo.getFileName()
                        + ": " + String.join(" | ", fila), e);
            }
        }
        return meGusta;
    }

    /**
     * Guarda los "me gusta" ordenados por publicación y usuario.
     * Recibe: la lista de pares [idPublicacion, idUsuario].
     *
     * @throws IOException si no se puede escribir
     */
    public void guardar(ListaEnlazada<int[]> meGusta) throws IOException {
        ListaEnlazada<int[]> copia = meGusta.copiar();
        copia.ordenar(Comparator.<int[]>comparingInt(par -> par[0])
                .thenComparingInt(par -> par[1]));

        ListaEnlazada<String[]> filas = new ListaEnlazada<>();
        for (int[] par : copia) {
            filas.agregar(new String[]{
                    String.valueOf(par[0]),
                    String.valueOf(par[1])});
        }
        CsvUtil.escribirArchivo(archivo, ENCABEZADO, filas);
    }
}
