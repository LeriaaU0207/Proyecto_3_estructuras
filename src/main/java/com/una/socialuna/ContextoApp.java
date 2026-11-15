package com.una.socialuna;

import com.una.socialuna.service.GestorDatos;
import com.una.socialuna.service.ServicioAmistades;
import com.una.socialuna.service.ServicioAutenticacion;
import com.una.socialuna.service.ServicioPublicaciones;
import com.una.socialuna.service.ServicioUsuarios;
import com.una.socialuna.service.SesionActual;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Reúne el gestor de datos y todos los servicios en un solo lugar.
 *
 * Se crea UNA sola vez al iniciar la aplicación y desde acá se pasan los
 * servicios a las vistas (las vistas nunca hacen new de un servicio).
 */
public final class ContextoApp {

    private final GestorDatos gestor;
    private final ServicioUsuarios usuarios;
    private final ServicioAutenticacion autenticacion;
    private final ServicioAmistades amistades;
    private final ServicioPublicaciones publicaciones;
    private final SesionActual sesion;

    private ContextoApp(GestorDatos gestor) {
        this.gestor = gestor;
        this.usuarios = new ServicioUsuarios(gestor);
        this.autenticacion = new ServicioAutenticacion(gestor, usuarios);
        this.amistades = new ServicioAmistades(gestor, usuarios);
        this.publicaciones = new ServicioPublicaciones(gestor);
        this.sesion = new SesionActual();
    }

    /**
     * Carga data/ (con auto-seed desde datos_ejemplo/ si está vacía) y crea
     * todos los servicios.
     *
     * @throws java.io.UncheckedIOException si los CSV no se pueden leer
     */
    public static ContextoApp crear() {
        GestorDatos gestor = new GestorDatos();
        gestor.cargarTodo();
        return new ContextoApp(gestor);
    }

    /**
     * Igual que crear(), pero usando una carpeta de datos distinta (pruebas).
     */
    public static ContextoApp crear(Path carpetaDatos) {
        Objects.requireNonNull(carpetaDatos, "La carpeta de datos no puede ser null.");
        GestorDatos gestor = new GestorDatos(carpetaDatos);
        gestor.cargarTodo();
        return new ContextoApp(gestor);
    }

    public GestorDatos getGestor() {
        return gestor;
    }

    public ServicioUsuarios getUsuarios() {
        return usuarios;
    }

    public ServicioAutenticacion getAutenticacion() {
        return autenticacion;
    }

    public ServicioAmistades getAmistades() {
        return amistades;
    }

    public ServicioPublicaciones getPublicaciones() {
        return publicaciones;
    }

    public SesionActual getSesion() {
        return sesion;
    }

    /**
     * Guarda los cuatro CSV. Se llama al cerrar la aplicación.
     */
    public void guardarTodo() {
        gestor.guardarTodo();
    }
}
