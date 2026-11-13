package com.una.socialuna.service;

import com.una.socialuna.model.Usuario;

import java.util.Objects;

public class SesionActual {

    private Usuario usuario;

    public void iniciar(Usuario usuario) {
        this.usuario = Objects.requireNonNull(usuario, "El usuario de la sesión no puede ser null.");
    }

    public void cerrar() {
        this.usuario = null;
    }

    public boolean haySesion() {
        return usuario != null;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public int getIdUsuario() {
        if (usuario == null) {
            throw new IllegalStateException("No hay una sesión abierta.");
        }
        return usuario.getId();
    }
}
