package com.una.socialuna.service;

import com.una.socialuna.util.CsvUtil;
import com.una.socialuna.util.ListaEnlazada;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Lee y escribe amistades.csv. Cada amistad aparece UNA sola vez.
 * Columnas: idUsuarioA,idUsuarioB  (siempre idUsuarioA < idUsuarioB)
 *
 * Trabaja con pares de ids (int[2]); GestorDatos es quien los convierte a
 * aristas del grafo y viceversa.
 */
public class RepositorioAmistades {

    private static final String[] ENCABEZADO = {"idUsuarioA", "idUsuarioB"};

    private final Path archivo;

    /**
     * Recibe: la ruta del archivo amistades.csv.
     */
    public RepositorioAmistades(Path archivo) {
        this.archivo = archivo;
    }

    /**
     * Retorna: true si el archivo ya existe en disco.
     */
    public boolean existe() {
        return Files.exists(archivo);
    }

    /**
     * Lee todas las amistades del archivo.
     * Retorna: una lista de pares [idA, idB] (vacía si el archivo no existe).
     *
     * @throws IOException si el archivo no se puede leer o tiene datos inválidos
     */
    public ListaEnlazada<int[]> cargar() throws IOException {
        ListaEnlazada<int[]> aristas = new ListaEnlazada<>();
        for (String[] fila : CsvUtil.leerRegistros(archivo)) {
            CsvUtil.exigirColumnas(fila, ENCABEZADO.length, archivo);
            try {
                aristas.agregar(new int[]{
                        Integer.parseInt(fila[0].trim()), Integer.parseInt(fila[1].trim())});
            } catch (NumberFormatException e) {
                throw new IOException("Id inválido en " + archivo.getFileName()
                        + ": " + String.join(" | ", fila), e);
            }
        }
        return aristas;
    }

    /**
     * Guarda las amistades ordenadas por (idA, idB).
     * Recibe: la lista de pares [idA, idB] (por ejemplo, grafo.obtenerAristas()).
     *
     * @throws IOException si no se puede escribir
     */
    public void guardar(ListaEnlazada<int[]> aristas) throws IOException {
        ListaEnlazada<int[]> copia = aristas.copiar();
        copia.ordenar(Comparator.<int[]>comparingInt(par -> Math.min(par[0], par[1]))
                .thenComparingInt(par -> Math.max(par[0], par[1])));
        ListaEnlazada<String[]> filas = new ListaEnlazada<>();
        for (int[] par : copia) {
            int menor = Math.min(par[0], par[1]);
            int mayor = Math.max(par[0], par[1]);
            filas.agregar(new String[]{String.valueOf(menor), String.valueOf(mayor)});
        }
        CsvUtil.escribirArchivo(archivo, ENCABEZADO, filas);
    }
}
