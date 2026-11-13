package com.una.socialuna.service;

import com.una.socialuna.model.Usuario;
import com.una.socialuna.util.HashUtil;

import java.util.Objects;

public class ServicioAutenticacion {

    private static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos.";

    private final GestorDatos gestor;
    private final ServicioUsuarios usuarios;

    public ServicioAutenticacion(GestorDatos gestor, ServicioUsuarios usuarios) {
        this.gestor = Objects.requireNonNull(gestor, "El gestor de datos no puede ser null.");
        this.usuarios = Objects.requireNonNull(usuarios, "El servicio de usuarios no puede ser null.");
    }

    public Usuario registrar(String nombre, String nombreUsuario, String contrasena) {
        String nombreLimpio = exigirTexto(nombre, "El nombre no puede estar vacío.");
        String usuarioLimpio = exigirTexto(nombreUsuario, "El nombre de usuario no puede estar vacío.");
        exigirTexto(contrasena, "La contraseña no puede estar vacía.");

        if (usuarios.existe(usuarioLimpio)) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
        }

        Usuario nuevo = new Usuario(
                gestor.siguienteIdUsuario(),
                nombreLimpio,
                usuarioLimpio,
                HashUtil.hashear(contrasena));

        gestor.getUsuarios().agregar(nuevo);
        gestor.getGrafo().agregarVertice(nuevo.getId());
        try {
            gestor.guardarUsuarios();
        } catch (RuntimeException e) {
            gestor.getGrafo().eliminarVertice(nuevo.getId());
            gestor.getUsuarios().eliminar(nuevo);
            throw e;
        }
        return nuevo;
    }

    public Usuario login(String nombreUsuario, String contrasena) {
        String usuarioLimpio = exigirTexto(nombreUsuario, CREDENCIALES_INVALIDAS);
        if (contrasena == null || contrasena.isEmpty()) {
            throw new IllegalArgumentException(CREDENCIALES_INVALIDAS);
        }

        Usuario encontrado = usuarios.porNombreUsuario(usuarioLimpio)
                .orElseThrow(() -> new IllegalArgumentException(CREDENCIALES_INVALIDAS));

        if (!HashUtil.verificar(contrasena, encontrado.getContrasenaHash())) {
            throw new IllegalArgumentException(CREDENCIALES_INVALIDAS);
        }
        return encontrado;
    }

    private static String exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }
}
