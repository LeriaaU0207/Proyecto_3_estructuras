package com.una.socialuna.service;

import com.una.socialuna.model.Comentario;
import com.una.socialuna.util.CsvUtil;
import com.una.socialuna.util.ListaEnlazada;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;

/**
 * Lee y escribe comentarios.csv.
 * Columnas: id,idPublicacion,idAutor,fechaHora,contenido,idComentarioPadre
 * (fechaHora en formato ISO; contenido entre comillas si trae comas,
 * comillas o saltos de línea).
 *
 * Compatibilidad: los archivos viejos de 5 columnas (sin idComentarioPadre)
 * se siguen leyendo; en ellos todos los comentarios son raíces (padre = 0).
 */
public class RepositorioComentarios {

    private static final String[] ENCABEZADO =
            {"id", "idPublicacion", "idAutor", "fechaHora", "contenido", "idComentarioPadre"};

    /** Columnas mínimas de una fila (las versiones viejas no traen el padre). */
    private static final int COLUMNAS_MINIMAS = 5;

    private final Path archivo;

    /**
     * Recibe: la ruta del archivo comentarios.csv.
     */
    public RepositorioComentarios(Path archivo) {
        this.archivo = archivo;
    }

    /**
     * Retorna: true si el archivo ya existe en disco.
     */
    public boolean existe() {
        return Files.exists(archivo);
    }

    /**
     * Lee todos los comentarios, ordenados del más viejo al más nuevo.
     * Retorna: una lista de comentarios (vacía si el archivo no existe).
     *
     * @throws IOException si el archivo no se puede leer o tiene datos inválidos
     */
    public ListaEnlazada<Comentario> cargar() throws IOException {
        ListaEnlazada<Comentario> comentarios = new ListaEnlazada<>();
        for (String[] fila : CsvUtil.leerRegistros(archivo)) {
            CsvUtil.exigirColumnas(fila, COLUMNAS_MINIMAS, archivo);
            try {
                int idPadre = 0;
                if (fila.length > COLUMNAS_MINIMAS && !fila[COLUMNAS_MINIMAS].isBlank()) {
                    idPadre = Integer.parseInt(fila[COLUMNAS_MINIMAS].trim());
                }
                comentarios.agregar(new Comentario(
                        Integer.parseInt(fila[0].trim()),
                        Integer.parseInt(fila[1].trim()),
                        Integer.parseInt(fila[2].trim()),
                        LocalDateTime.parse(fila[3].trim()),
                        fila[4],
                        Math.max(0, idPadre)));
            } catch (NumberFormatException | DateTimeParseException e) {
                throw new IOException("Dato inválido en " + archivo.getFileName()
                        + ": " + String.join(" | ", fila), e);
            }
        }
        comentarios.ordenar(Comparator.comparing(Comentario::getFechaHora)
                .thenComparingInt(Comentario::getId));
        return comentarios;
    }

    /**
     * Guarda los comentarios ordenados por id.
     * Recibe: la lista de comentarios a guardar.
     *
     * @throws IOException si no se puede escribir
     */
    public void guardar(ListaEnlazada<Comentario> comentarios) throws IOException {
        ListaEnlazada<Comentario> copia = comentarios.copiar();
        copia.ordenar(Comparator.comparingInt(Comentario::getId));

        ListaEnlazada<String[]> filas = new ListaEnlazada<>();
        for (Comentario c : copia) {
            filas.agregar(new String[]{
                    String.valueOf(c.getId()),
                    String.valueOf(c.getIdPublicacion()),
                    String.valueOf(c.getIdAutor()),
                    c.getFechaHora().toString(),
                    c.getContenido(),
                    String.valueOf(c.getIdComentarioPadre())});
        }
        CsvUtil.escribirArchivo(archivo, ENCABEZADO, filas);
    }
}
