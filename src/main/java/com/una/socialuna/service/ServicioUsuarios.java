package com.una.socialuna.service;

import com.una.socialuna.model.Usuario;
import com.una.socialuna.util.ListaEnlazada;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class ServicioUsuarios {

    private final GestorDatos gestor;

    public ServicioUsuarios(GestorDatos gestor) {
        this.gestor = Objects.requireNonNull(gestor, "El gestor de datos no puede ser null.");
    }

    public ListaEnlazada<Usuario> listar() {
        return gestor.getUsuarios();
    }

    public Optional<Usuario> porId(int id) {
        for (Usuario usuario : gestor.getUsuarios()) {
            if (usuario.getId() == id) {
                return Optional.of(usuario);
            }
        }
        return Optional.empty();
    }

    public Optional<Usuario> porNombreUsuario(String nombreUsuario) {
        if (nombreUsuario == null) {
            return Optional.empty();
        }
        String buscado = nombreUsuario.trim();
        if (buscado.isEmpty()) {
            return Optional.empty();
        }
        for (Usuario usuario : gestor.getUsuarios()) {
            if (usuario.getUsuario().equalsIgnoreCase(buscado)) {
                return Optional.of(usuario);
            }
        }
        return Optional.empty();
    }

    public boolean existe(String nombreUsuario) {
        return porNombreUsuario(nombreUsuario).isPresent();
    }

    public boolean existeId(int id) {
        return porId(id).isPresent();
    }

    public ListaEnlazada<Usuario> buscar(String texto) {
        ListaEnlazada<Usuario> resultado = new ListaEnlazada<>();
        if (texto == null || texto.isBlank()) {
            return resultado;
        }
        String buscado = texto.trim().toLowerCase(Locale.ROOT);
        for (Usuario usuario : gestor.getUsuarios()) {
            boolean coincideUsuario = usuario.getUsuario().toLowerCase(Locale.ROOT).contains(buscado);
            boolean coincideNombre = usuario.getNombre().toLowerCase(Locale.ROOT).contains(buscado);
            if (coincideUsuario || coincideNombre) {
                resultado.agregar(usuario);
            }
        }
        return resultado;
    }
}
