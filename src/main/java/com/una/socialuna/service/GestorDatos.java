package com.una.socialuna.service;

import com.una.socialuna.model.GrafoAmistades;
import com.una.socialuna.model.Publicacion;
import com.una.socialuna.model.Solicitud;
import com.una.socialuna.model.Usuario;
import com.una.socialuna.util.ListaEnlazada;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Punto central de la persistencia. Es DUEÑO de las estructuras de datos en
 * memoria (usuarios, grafo de amistades, solicitudes y publicaciones) y sabe
 * cargarlas desde / guardarlas en los CSV de la carpeta data/.
 *
 * Cómo se usa:
 *  1. Al iniciar la app: new GestorDatos().cargarTodo()  (UNA sola vez, antes
 *     de crear los servicios).
 *  2. Los servicios piden las estructuras con getUsuarios(), getGrafo(), etc.
 *     (siempre al momento de usarlas, sin guardarlas en un campo propio).
 *  3. Después de CADA cambio, el servicio llama al guardarXxx() que
 *     corresponda. Al cerrar la app se llama guardarTodo() por seguridad.
 *
 * Los errores de lectura/escritura se lanzan como UncheckedIOException, para
 * que la interfaz los atrape y muestre una alerta.
 */
public class GestorDatos {

    @FunctionalInterface
    private interface AccionIO {
        void ejecutar() throws IOException;
    }

    private final RepositorioUsuarios repoUsuarios;
    private final RepositorioAmistades repoAmistades;
    private final RepositorioSolicitudes repoSolicitudes;
    private final RepositorioPublicaciones repoPublicaciones;

    private ListaEnlazada<Usuario> usuarios = new ListaEnlazada<>();
    private GrafoAmistades grafo = new GrafoAmistades();
    private ListaEnlazada<Solicitud> solicitudes = new ListaEnlazada<>();
    private ListaEnlazada<Publicacion> publicaciones = new ListaEnlazada<>();

    private int proximoIdUsuario = 1;
    private int proximoIdPublicacion = 1;

    /**
     * Usa la carpeta data/ ubicada en el directorio desde donde se ejecuta la
     * aplicación (la raíz del proyecto en IntelliJ, o la carpeta del .jar).
     */
    public GestorDatos() {
        this(Paths.get(System.getProperty("user.dir"), "data"));
    }

    /**
     * Recibe: la carpeta donde están (o se crearán) los archivos CSV.
     */
    public GestorDatos(Path carpetaDatos) {
        this.repoUsuarios = new RepositorioUsuarios(carpetaDatos.resolve("usuarios.csv"));
        this.repoAmistades = new RepositorioAmistades(carpetaDatos.resolve("amistades.csv"));
        this.repoSolicitudes = new RepositorioSolicitudes(carpetaDatos.resolve("solicitudes.csv"));
        this.repoPublicaciones = new RepositorioPublicaciones(carpetaDatos.resolve("publicaciones.csv"));
    }

    /**
     * Lee los cuatro CSV y reconstruye las estructuras en memoria. Si algún
     * archivo no existe, lo crea con su encabezado. Descarta datos "huérfanos"
     * (amistades, solicitudes o publicaciones de usuarios que ya no existen).
     * También calcula los próximos ids (máximo + 1).
     *
     * @throws UncheckedIOException si algún archivo no se puede leer o está dañado
     */
    public void cargarTodo() {
        try {
            usuarios = repoUsuarios.cargar();

            grafo = new GrafoAmistades();
            for (Usuario u : usuarios) {
                grafo.agregarVertice(u.getId());
            }
            for (int[] par : repoAmistades.cargar()) {
                grafo.agregarArista(par[0], par[1]); // ignora ids que no existan
            }

            solicitudes = repoSolicitudes.cargar();
            solicitudes.eliminarSi(s -> !existeUsuario(s.getIdEmisor())
                    || !existeUsuario(s.getIdReceptor()));

            publicaciones = repoPublicaciones.cargar();
            publicaciones.eliminarSi(p -> !existeUsuario(p.getIdAutor()));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudieron cargar los datos: " + e.getMessage(), e);
        }

        proximoIdUsuario = 1;
        for (Usuario u : usuarios) {
            if (u.getId() >= proximoIdUsuario) {
                proximoIdUsuario = u.getId() + 1;
            }
        }
        proximoIdPublicacion = 1;
        for (Publicacion p : publicaciones) {
            if (p.getId() >= proximoIdPublicacion) {
                proximoIdPublicacion = p.getId() + 1;
            }
        }

        crearArchivosFaltantes();
    }

    private void crearArchivosFaltantes() {
        if (!repoUsuarios.existe()) {
            guardarUsuarios();
        }
        if (!repoAmistades.existe()) {
            guardarAmistades();
        }
        if (!repoSolicitudes.existe()) {
            guardarSolicitudes();
        }
        if (!repoPublicaciones.existe()) {
            guardarPublicaciones();
        }
    }

    private boolean existeUsuario(int id) {
        return usuarios.contiene(u -> u.getId() == id);
    }

    /**
     * Guarda usuarios.csv. Llamar después de registrar, editar o eliminar usuarios.
     *
     * @throws UncheckedIOException si no se puede escribir
     */
    public void guardarUsuarios() {
        ejecutarGuardado("usuarios.csv", () -> repoUsuarios.guardar(usuarios));
    }

    /**
     * Guarda amistades.csv. Llamar después de aceptar o eliminar una amistad.
     *
     * @throws UncheckedIOException si no se puede escribir
     */
    public void guardarAmistades() {
        ejecutarGuardado("amistades.csv", () -> repoAmistades.guardar(grafo.obtenerAristas()));
    }

    /**
     * Guarda solicitudes.csv. Llamar después de enviar, aceptar o rechazar solicitudes.
     *
     * @throws UncheckedIOException si no se puede escribir
     */
    public void guardarSolicitudes() {
        ejecutarGuardado("solicitudes.csv", () -> repoSolicitudes.guardar(solicitudes));
    }

    /**
     * Guarda publicaciones.csv. Llamar después de publicar, editar o borrar.
     *
     * @throws UncheckedIOException si no se puede escribir
     */
    public void guardarPublicaciones() {
        ejecutarGuardado("publicaciones.csv", () -> repoPublicaciones.guardar(publicaciones));
    }

    /**
     * Guarda los cuatro archivos. Se usa al cerrar la aplicación.
     *
     * @throws UncheckedIOException si algún archivo no se puede escribir
     */
    public void guardarTodo() {
        guardarUsuarios();
        guardarAmistades();
        guardarSolicitudes();
        guardarPublicaciones();
    }

    private void ejecutarGuardado(String nombreArchivo, AccionIO accion) {
        try {
            accion.ejecutar();
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "No se pudo guardar " + nombreArchivo + ": " + e.getMessage(), e);
        }
    }

    /**
     * Retorna: el próximo id libre para un usuario (autoincremental; nunca se
     * repite durante la sesión).
     */
    public int siguienteIdUsuario() {
        return proximoIdUsuario++;
    }

    /**
     * Retorna: el próximo id libre para una publicación (autoincremental).
     */
    public int siguienteIdPublicacion() {
        return proximoIdPublicacion++;
    }

    /**
     * Retorna: la lista de usuarios registrados (la estructura real en memoria).
     */
    public ListaEnlazada<Usuario> getUsuarios() {
        return usuarios;
    }

    /**
     * Retorna: el grafo de amistades (la estructura real en memoria).
     */
    public GrafoAmistades getGrafo() {
        return grafo;
    }

    /**
     * Retorna: la lista de solicitudes pendientes (la estructura real en memoria).
     */
    public ListaEnlazada<Solicitud> getSolicitudes() {
        return solicitudes;
    }

    /**
     * Retorna: la lista de publicaciones, de la más reciente a la más antigua
     *          (la estructura real en memoria).
     */
    public ListaEnlazada<Publicacion> getPublicaciones() {
        return publicaciones;
    }
}
