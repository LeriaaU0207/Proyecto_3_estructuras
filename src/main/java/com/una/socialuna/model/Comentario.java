package com.una.socialuna.model;

import java.time.LocalDateTime;

/**
 * Comentario que un usuario escribe dentro de una publicación.
 * Vive dentro de la publicación (no tiene vida propia en el muro).
 *
 * Si idComentarioPadre es mayor que 0, el comentario es una RESPUESTA a otro
 * comentario de la misma publicación. Las respuestas solo tienen un nivel:
 * responder a una respuesta se guarda como respuesta del comentario raíz.
 */
public class Comentario {

    private final int id;
    private final int idPublicacion;
    private final int idAutor;
    private final LocalDateTime fechaHora;
    private final String contenido;
    private final int idComentarioPadre;

    public Comentario(int id, int idPublicacion, int idAutor,
                      LocalDateTime fechaHora, String contenido) {
        this(id, idPublicacion, idAutor, fechaHora, contenido, 0);
    }

    public Comentario(int id, int idPublicacion, int idAutor,
                      LocalDateTime fechaHora, String contenido, int idComentarioPadre) {
        this.id = id;
        this.idPublicacion = idPublicacion;
        this.idAutor = idAutor;
        this.fechaHora = fechaHora;
        this.contenido = contenido;
        this.idComentarioPadre = idComentarioPadre;
    }

    public int getId() { return id; }
    public int getIdPublicacion() { return idPublicacion; }
    public int getIdAutor() { return idAutor; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getContenido() { return contenido; }

    /** Retorna: el id del comentario al que responde, o 0 si es un comentario raíz. */
    public int getIdComentarioPadre() { return idComentarioPadre; }

    /** Retorna: true si este comentario es una respuesta a otro comentario. */
    public boolean esRespuesta() { return idComentarioPadre > 0; }

    @Override
    public String toString() {
        return "Comentario#" + id + " de " + idAutor + " en publicacion " + idPublicacion
                + (esRespuesta() ? " (respuesta a #" + idComentarioPadre + ")" : "")
                + " (" + fechaHora + "): " + contenido;
    }
}
