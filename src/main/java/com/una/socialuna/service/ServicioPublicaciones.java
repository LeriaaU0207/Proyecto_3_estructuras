package com.una.socialuna.service;

import com.una.socialuna.model.Comentario;
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

    /** Cantidad máxima de caracteres permitidos en un comentario. */
    public static final int LONGITUD_MAXIMA_COMENTARIO = 300;

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
        gestor.guardarMeGusta();
        gestor.guardarComentarios();
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
            gestor.guardarMeGusta();
            gestor.guardarComentarios();
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
     * Da o quita el "me gusta" de un usuario a una publicación.
     * Recibe: el id de la publicación y el id del usuario.
     * Retorna: true si después de la operación el usuario la tiene en "me gusta".
     *
     * @throws IllegalArgumentException si la publicación o el usuario no existen
     */
    public boolean alternarMeGusta(int idPublicacion, int idUsuario) {
        Publicacion publicacion = buscarPorId(idPublicacion)
                .orElseThrow(() -> new IllegalArgumentException("La publicación no existe."));
        if (!gestor.getUsuarios().contiene(u -> u.getId() == idUsuario)) {
            throw new IllegalArgumentException("El usuario no existe.");
        }

        boolean ahoraLeGusta;
        if (publicacion.leGusta(idUsuario)) {
            publicacion.getMeGusta().eliminar(Integer.valueOf(idUsuario));
            ahoraLeGusta = false;
        } else {
            publicacion.getMeGusta().agregar(idUsuario);
            ahoraLeGusta = true;
        }
        gestor.guardarMeGusta();
        return ahoraLeGusta;
    }

    /**
     * Agrega un comentario a una publicación.
     * Recibe: el id de la publicación, el id del autor y el texto.
     * Retorna: el comentario creado.
     *
     * @throws IllegalArgumentException si el texto es inválido, o la
     *         publicación o el usuario no existen
     */
    public Comentario comentar(int idPublicacion, int idAutor, String contenido) {
        return comentar(idPublicacion, idAutor, contenido, 0);
    }

    /**
     * Agrega un comentario o una respuesta dentro de una publicación.
     * Recibe: el id de la publicación, el id del autor, el texto y el id del
     *         comentario al que responde (0 para un comentario raíz).
     * Retorna: el comentario creado.
     *
     * Las respuestas tienen un solo nivel: si se responde a una respuesta, el
     * comentario nuevo queda colgando del comentario raíz.
     *
     * @throws IllegalArgumentException si el texto es inválido, la publicación,
     *         el usuario o el comentario respondido no existen
     */
    public Comentario comentar(int idPublicacion, int idAutor, String contenido,
                               int idComentarioPadre) {
        String texto = validarComentario(contenido);
        Publicacion publicacion = buscarPorId(idPublicacion)
                .orElseThrow(() -> new IllegalArgumentException("La publicación no existe."));
        if (!gestor.getUsuarios().contiene(u -> u.getId() == idAutor)) {
            throw new IllegalArgumentException("El usuario no existe.");
        }

        int padre = 0;
        if (idComentarioPadre > 0) {
            Comentario respondido = publicacion.getComentarios()
                    .buscar(c -> c.getId() == idComentarioPadre)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El comentario al que quieres responder no existe."));
            padre = respondido.esRespuesta()
                    ? respondido.getIdComentarioPadre()
                    : respondido.getId();
        }

        Comentario comentario = new Comentario(
                gestor.siguienteIdComentario(), idPublicacion, idAutor,
                LocalDateTime.now(), texto, padre);
        publicacion.getComentarios().agregar(comentario);
        gestor.guardarComentarios();
        return comentario;
    }

    /**
     * Elimina un comentario propio. Si es un comentario raíz, también elimina
     * sus respuestas (para no dejar respuestas huérfanas).
     * Recibe: el id del comentario y el id de quien pide eliminarlo.
     * Retorna: true si se eliminó; false si no existe o no es su autor.
     */
    public boolean eliminarComentario(int idComentario, int idSolicitante) {
        for (Publicacion publicacion : gestor.getPublicaciones()) {
            Optional<Comentario> encontrado = publicacion.getComentarios()
                    .buscar(c -> c.getId() == idComentario);
            if (encontrado.isPresent()) {
                if (encontrado.get().getIdAutor() != idSolicitante) {
                    return false;
                }
                publicacion.getComentarios().eliminarSi(c ->
                        c.getId() == idComentario
                                || c.getIdComentarioPadre() == idComentario);
                gestor.guardarComentarios();
                return true;
            }
        }
        return false;
    }

    /**
     * Recibe: el id de la publicación y el id de un comentario raíz.
     * Retorna: las respuestas de ese comentario, en orden de llegada.
     */
    public ListaEnlazada<Comentario> respuestasDe(int idPublicacion, int idComentarioPadre) {
        return comentariosDe(idPublicacion)
                .filtrar(c -> c.getIdComentarioPadre() == idComentarioPadre);
    }

    /**
     * Recibe: el id de la publicación.
     * Retorna: sus comentarios en orden de llegada (lista vacía si no existe).
     */
    public ListaEnlazada<Comentario> comentariosDe(int idPublicacion) {
        return buscarPorId(idPublicacion)
                .map(Publicacion::getComentarios)
                .orElseGet(ListaEnlazada::new);
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

    private String validarComentario(String contenido) {
        if (contenido == null || contenido.trim().isEmpty()) {
            throw new IllegalArgumentException("El comentario no puede estar vacío.");
        }
        String texto = contenido.trim();
        if (texto.length() > LONGITUD_MAXIMA_COMENTARIO) {
            throw new IllegalArgumentException(
                    "El comentario no puede superar los "
                            + LONGITUD_MAXIMA_COMENTARIO + " caracteres.");
        }
        return texto;
    }
}
