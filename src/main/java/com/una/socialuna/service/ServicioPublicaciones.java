package com.una.socialuna.service;

import com.una.socialuna.model.Publicacion;
import com.una.socialuna.util.ListaEnlazada;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * CRUD de publicaciones y armado del feed (muro).
 * Las publicaciones viven en una ListaEnlazada que se mantiene ordenada de la
 * más reciente a la más antigua: cada publicación nueva se inserta AL INICIO,
 * así el feed no necesita ordenarse cada vez.
 * Después de cada cambio se guarda publicaciones.csv mediante GestorDatos.
 * Si el guardado falla se lanza UncheckedIOException (la vista debe mostrar
 * una alerta).
 */
public class ServicioPublicaciones {

    /** Cantidad máxima de caracteres permitidos en una publicación. */
    public static final int LONGITUD_MAXIMA = 500;

    private final GestorDatos gestor;

    /**
     * Recibe: el gestor de datos compartido por toda la aplicación.
     */
    public ServicioPublicaciones(GestorDatos gestor) {
        this.gestor = gestor;
    }

    /**
     * Crea una publicación nueva y la guarda.
     * Recibe: el ID del autor y el texto del mensaje.
     * Retorna: la publicación creada.
     *
     * @throws IllegalArgumentException si el texto está vacío, supera
     *         LONGITUD_MAXIMA o el autor no existe
     */
    public Publicacion publicar(int idAutor, String contenido) {
        String texto = validarContenido(contenido);
        if (!gestor.getUsuarios().contiene(u -> u.getId() == idAutor)) {
            throw new IllegalArgumentException("El usuario autor no existe.");
        }
        Publicacion nueva = new Publicacion(
                gestor.siguienteIdPublicacion(), idAutor, LocalDateTime.now(), texto, false);
        gestor.getPublicaciones().agregarAlInicio(nueva);
        gestor.guardarPublicaciones();
        return nueva;
    }

    /**
     * Edita el texto de una publicación (solo su autor puede hacerlo). La
     * fecha original se conserva, así que no cambia su posición en el feed;
     * se marca como editada.
     * Recibe: el id de la publicación, el id de quien pide editar y el texto nuevo.
     * Retorna: true si se editó; false si no existe o no es del solicitante.
     *
     * @throws IllegalArgumentException si el texto nuevo es inválido
     */
    public boolean editar(int idPublicacion, int idSolicitante, String nuevoContenido) {
        String texto = validarContenido(nuevoContenido);
        Optional<Publicacion> encontrada = buscarPorId(idPublicacion);
        if (encontrada.isEmpty() || encontrada.get().getIdAutor() != idSolicitante) {
            return false;
        }
        encontrada.get().setContenido(texto);
        encontrada.get().setEditada(true);
        gestor.guardarPublicaciones();
        return true;
    }

    /**
     * Elimina una publicación (solo su autor puede hacerlo).
     * Recibe: el id de la publicación y el id de quien pide eliminarla.
     * Retorna: true si se eliminó; false si no existe o no es del solicitante.
     */
    public boolean eliminar(int idPublicacion, int idSolicitante) {
        Optional<Publicacion> encontrada = buscarPorId(idPublicacion);
        if (encontrada.isEmpty() || encontrada.get().getIdAutor() != idSolicitante) {
            return false;
        }
        gestor.getPublicaciones().eliminarSi(p -> p.getId() == idPublicacion);
        gestor.guardarPublicaciones();
        return true;
    }

    /**
     * Elimina todas las publicaciones de un usuario (para cuando se elimina
     * su cuenta).
     * Recibe: el id del usuario.
     * Retorna: la cantidad de publicaciones eliminadas.
     */
    public int eliminarPublicacionesDe(int idUsuario) {
        int eliminadas = gestor.getPublicaciones().eliminarSi(p -> p.getIdAutor() == idUsuario);
        if (eliminadas > 0) {
            gestor.guardarPublicaciones();
        }
        return eliminadas;
    }

    /**
     * Arma el feed de un usuario: sus publicaciones y las de sus amigos, de la
     * más reciente a la más antigua.
     * Recibe: el id del usuario activo.
     * Retorna: una lista nueva con las publicaciones del feed.
     */
    public ListaEnlazada<Publicacion> obtenerFeed(int idUsuario) {
        return gestor.getPublicaciones().filtrar(p ->
                p.getIdAutor() == idUsuario
                        || gestor.getGrafo().sonAmigos(idUsuario, p.getIdAutor()));
    }

    /**
     * Arma el muro personal de un usuario: solo sus propias publicaciones, de
     * la más reciente a la más antigua.
     * Recibe: el id del usuario.
     * Retorna: una lista nueva con sus publicaciones.
     */
    public ListaEnlazada<Publicacion> obtenerPublicacionesDe(int idUsuario) {
        return gestor.getPublicaciones().filtrar(p -> p.getIdAutor() == idUsuario);
    }

    /**
     * Recibe: el id de la publicación.
     * Retorna: un Optional con la publicación, o vacío si no existe.
     */
    public Optional<Publicacion> buscarPorId(int idPublicacion) {
        return gestor.getPublicaciones().buscar(p -> p.getId() == idPublicacion);
    }

    private String validarContenido(String contenido) {
        if (contenido == null || contenido.trim().isEmpty()) {
            throw new IllegalArgumentException("La publicación no puede estar vacía.");
        }
        String texto = contenido.trim();
        if (texto.length() > LONGITUD_MAXIMA) {
            throw new IllegalArgumentException(
                    "La publicación no puede superar los " + LONGITUD_MAXIMA + " caracteres.");
        }
        return texto;
    }
}
