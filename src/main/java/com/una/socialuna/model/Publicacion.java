package com.una.socialuna.model;

import com.una.socialuna.util.ListaEnlazada;

import java.time.LocalDateTime;

/**
 * Representa un mensaje publicado por un usuario en el muro.
 * Además del texto guarda los "me gusta" y los comentarios, que se
 * persisten en archivos CSV separados para no cambiar publicaciones.csv.
 */

public class Publicacion {

    private final int id;
    private final int idAutor;
    private final LocalDateTime fechaHora;
    private String contenido;
    private boolean editada;

    private final ListaEnlazada<Integer> meGusta = new ListaEnlazada<>();
    private final ListaEnlazada<Comentario> comentarios = new ListaEnlazada<>();

    //Recibe el ID, idAutor, fecha y hora de creación, texto y si fue editada
    public Publicacion(int id, int idAutor, LocalDateTime fechaHora,
                       String contenido, boolean editada) {
        this.id = id;
        this.idAutor = idAutor;
        this.fechaHora = fechaHora;
        this.contenido = contenido;
        this.editada = editada;
    }

    public int getId() { return id; }
    public int getIdAutor() { return idAutor; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public boolean isEditada() { return editada; }
    public void setEditada(boolean editada) { this.editada = editada; }

    /**
     * Retorna: la lista de ids de usuarios que dieron "me gusta".
     */
    public ListaEnlazada<Integer> getMeGusta() { return meGusta; }

    /**
     * Retorna: la lista de comentarios de la publicación, en orden de llegada.
     */
    public ListaEnlazada<Comentario> getComentarios() { return comentarios; }

    /**
     * Recibe: el id de un usuario.
     * Retorna: true si ese usuario ya dio "me gusta".
     */
    public boolean leGusta(int idUsuario) {
        return meGusta.contiene(id -> id == idUsuario);
    }

    /**
     * Retorna: la cantidad de "me gusta".
     */
    public int cantidadMeGusta() { return meGusta.tamano(); }

    /**
     * Retorna: la cantidad de comentarios.
     */
    public int cantidadComentarios() { return comentarios.tamano(); }

    @Override
    public String toString() { return "Publicacion#" + id + " de " + idAutor + " (" + fechaHora + "): " + contenido; }

}
