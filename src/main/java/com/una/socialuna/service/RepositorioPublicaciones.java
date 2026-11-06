package com.una.socialuna.service;

import com.una.socialuna.model.Publicacion;
import com.una.socialuna.util.CsvUtil;
import com.una.socialuna.util.ListaEnlazada;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;

/**
 * Lee y escribe publicaciones.csv.
 * Columnas: id,idAutor,fechaHora,contenido,editada
 * (fechaHora en formato ISO; contenido entre comillas si trae comas,
 * comillas o saltos de línea).
 */
public class RepositorioPublicaciones {

    private static final String[] ENCABEZADO =
            {"id", "idAutor", "fechaHora", "contenido", "editada"};

    private final Path archivo;

    /**
     * Recibe: la ruta del archivo publicaciones.csv.
     */
    public RepositorioPublicaciones(Path archivo) {
        this.archivo = archivo;
    }

    /**
     * Retorna: true si el archivo ya existe en disco.
     */
    public boolean existe() {
        return Files.exists(archivo);
    }

    /**
     * Lee todas las publicaciones y las deja ordenadas de la más reciente a
     * la más antigua (por fecha; si empatan, por id mayor primero).
     * Retorna: una lista de publicaciones (vacía si el archivo no existe).
     *
     * @throws IOException si el archivo no se puede leer o tiene datos inválidos
     */
    public ListaEnlazada<Publicacion> cargar() throws IOException {
        ListaEnlazada<Publicacion> publicaciones = new ListaEnlazada<>();
        for (String[] fila : CsvUtil.leerRegistros(archivo)) {
            CsvUtil.exigirColumnas(fila, ENCABEZADO.length, archivo);
            try {
                publicaciones.agregar(new Publicacion(
                        Integer.parseInt(fila[0].trim()),
                        Integer.parseInt(fila[1].trim()),
                        LocalDateTime.parse(fila[2].trim()),
                        fila[3],
                        Boolean.parseBoolean(fila[4].trim())));
            } catch (NumberFormatException | DateTimeParseException e) {
                throw new IOException("Dato inválido en " + archivo.getFileName()
                        + ": " + String.join(" | ", fila), e);
            }
        }
        publicaciones.ordenar(Comparator.comparing(Publicacion::getFechaHora)
                .thenComparingInt(Publicacion::getId).reversed());
        return publicaciones;
    }

    /**
     * Guarda las publicaciones ordenadas por id (el orden del archivo no
     * importa: al cargar se reordenan por fecha).
     * Recibe: la lista de publicaciones a guardar.
     *
     * @throws IOException si no se puede escribir
     */
    public void guardar(ListaEnlazada<Publicacion> publicaciones) throws IOException {
        ListaEnlazada<Publicacion> copia = publicaciones.copiar();
        copia.ordenar(Comparator.comparingInt(Publicacion::getId));
        ListaEnlazada<String[]> filas = new ListaEnlazada<>();
        for (Publicacion p : copia) {
            filas.agregar(new String[]{
                    String.valueOf(p.getId()),
                    String.valueOf(p.getIdAutor()),
                    p.getFechaHora().toString(),
                    p.getContenido(),
                    String.valueOf(p.isEditada())});
        }
        CsvUtil.escribirArchivo(archivo, ENCABEZADO, filas);
    }
}
