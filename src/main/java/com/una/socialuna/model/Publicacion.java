package com.una.socialuna.model;

import java.time.LocalDateTime;
/**
 * ==== ARCHIVO TEMPORAL (de Leria, para que compile la persistencia) ====
 * Anthony/Boris: pueden reemplazarlo por su versión final. Solo mantengan
 * estos campos y getters, porque los usa RepositorioPublicaciones
 * (publicaciones.csv) y ServicioPublicaciones. Al reemplazarlo, borren este
 * comentario.
 * Representa un mensaje publicado por un usuario en el muro.
 */

public class Publicacion {

    private final int id;
    private final int idAutor;
    private final LocalDateTime fechaHora;
    private String contenido;
    private boolean editada;

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

    @Override
    public String toString() { return "Publicacion#" + id + " de " + idAutor + " (" + fechaHora + "): " + contenido; }

}

