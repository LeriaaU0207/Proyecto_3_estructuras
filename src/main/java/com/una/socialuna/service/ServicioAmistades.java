package com.una.socialuna.service;

import com.una.socialuna.model.Solicitud;
import com.una.socialuna.model.Usuario;
import com.una.socialuna.util.ListaEnlazada;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

public class ServicioAmistades {

    private final GestorDatos gestor;
    private final ServicioUsuarios usuarios;

    public ServicioAmistades(GestorDatos gestor, ServicioUsuarios usuarios) {
        this.gestor = Objects.requireNonNull(gestor, "El gestor de datos no puede ser null.");
        this.usuarios = Objects.requireNonNull(usuarios, "El servicio de usuarios no puede ser null.");
    }

    public Solicitud enviarSolicitud(int idEmisor, int idReceptor) {
        if (idEmisor == idReceptor) {
            throw new IllegalArgumentException("No puedes enviarte una solicitud a ti mismo.");
        }
        exigirUsuario(idEmisor);
        exigirUsuario(idReceptor);
        if (gestor.getGrafo().sonAmigos(idEmisor, idReceptor)) {
            throw new IllegalArgumentException("Ya son amigos.");
        }
        if (buscarSolicitud(idEmisor, idReceptor) != null) {
            throw new IllegalArgumentException("Ya enviaste una solicitud a este usuario.");
        }
        if (buscarSolicitud(idReceptor, idEmisor) != null) {
            throw new IllegalArgumentException("Ese usuario ya te envió una solicitud. Acéptala desde Solicitudes.");
        }

        Solicitud solicitud = new Solicitud(idEmisor, idReceptor, LocalDateTime.now());
        gestor.getSolicitudes().agregar(solicitud);
        try {
            gestor.guardarSolicitudes();
        } catch (RuntimeException e) {
            gestor.getSolicitudes().eliminar(solicitud);
            throw e;
        }
        return solicitud;
    }

    public boolean aceptar(int idEmisor, int idReceptor) {
        Solicitud solicitud = buscarSolicitud(idEmisor, idReceptor);
        if (solicitud == null) {
            return false;
        }
        gestor.getGrafo().agregarArista(idEmisor, idReceptor);
        gestor.getSolicitudes().eliminar(solicitud);
        gestor.guardarAmistades();
        gestor.guardarSolicitudes();
        return true;
    }

    public boolean rechazar(int idEmisor, int idReceptor) {
        Solicitud solicitud = buscarSolicitud(idEmisor, idReceptor);
        if (solicitud == null) {
            return false;
        }
        gestor.getSolicitudes().eliminar(solicitud);
        gestor.guardarSolicitudes();
        return true;
    }

    public boolean eliminarAmigo(int idUsuario, int idAmigo) {
        boolean eliminada = gestor.getGrafo().eliminarArista(idUsuario, idAmigo);
        if (eliminada) {
            gestor.guardarAmistades();
        }
        return eliminada;
    }

    public boolean sonAmigos(int idUsuario, int idOtro) {
        return gestor.getGrafo().sonAmigos(idUsuario, idOtro);
    }

    public boolean haySolicitudEntre(int idUsuario, int idOtro) {
        return buscarSolicitud(idUsuario, idOtro) != null
                || buscarSolicitud(idOtro, idUsuario) != null;
    }

    public ListaEnlazada<Solicitud> solicitudesPendientes(int idReceptor) {
        ListaEnlazada<Solicitud> pendientes =
                gestor.getSolicitudes().filtrar(s -> s.getIdReceptor() == idReceptor);
        pendientes.ordenar(Comparator.comparing(Solicitud::getFechaHora).reversed());
        return pendientes;
    }

    public int cantidadSolicitudesPendientes(int idReceptor) {
        return solicitudesPendientes(idReceptor).tamano();
    }

    public ListaEnlazada<Usuario> amigosDe(int idUsuario) {
        ListaEnlazada<Usuario> amigos = new ListaEnlazada<>();
        for (Integer idAmigo : gestor.getGrafo().amigosDe(idUsuario)) {
            usuarios.porId(idAmigo).ifPresent(amigos::agregar);
        }
        amigos.ordenar(Comparator.comparing(Usuario::getNombre, String.CASE_INSENSITIVE_ORDER));
        return amigos;
    }

    public ListaEnlazada<Usuario> amigosEnComun(int idUsuario, int idOtro) {
        ListaEnlazada<Usuario> comunes = new ListaEnlazada<>();
        for (Integer idComun : gestor.getGrafo().amigosEnComun(idUsuario, idOtro)) {
            usuarios.porId(idComun).ifPresent(comunes::agregar);
        }
        comunes.ordenar(Comparator.comparing(Usuario::getNombre, String.CASE_INSENSITIVE_ORDER));
        return comunes;
    }

    private Solicitud buscarSolicitud(int idEmisor, int idReceptor) {
        for (Solicitud solicitud : gestor.getSolicitudes()) {
            if (solicitud.getIdEmisor() == idEmisor && solicitud.getIdReceptor() == idReceptor) {
                return solicitud;
            }
        }
        return null;
    }

    private void exigirUsuario(int idUsuario) {
        if (usuarios.porId(idUsuario).isEmpty()) {
            throw new IllegalArgumentException("El usuario no existe.");
        }
    }
}
