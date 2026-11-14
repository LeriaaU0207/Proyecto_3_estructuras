package com.una.socialuna.util;

import com.una.socialuna.service.ServicioPublicaciones;

public final class Validador {
    private Validador() { }

    public static void exigirTexto(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Completá el campo " + campo + ".");
        }
    }

    public static void validarRegistro(String nombre, String usuario,
                                       String clave, String repetida) {
        exigirTexto(nombre, "nombre");
        exigirTexto(usuario, "usuario");
        exigirTexto(clave, "contraseña");
        exigirTexto(repetida, "confirmar contraseña");

        if (nombre.trim().length() > 100) {
            throw new IllegalArgumentException("El nombre admite hasta 100 caracteres.");
        }

        if (!usuario.trim().matches("[\\p{L}\\p{N}_.-]{3,30}")) {
            throw new IllegalArgumentException(
                    "El usuario debe tener al menos de 3 a 30 caracteres. Puede incluir: letras, números, punto, guion o guion bajo.");
        }

        if (clave.length() < 6 || clave.length() > 128) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener entre 6 y 128 caracteres.");
        }

        // La contraseña no la recortamos. Los espacios ya forman parte de ella.
        if (!clave.equals(repetida)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }
    }

    public static void validarPublicacion(String texto) {
        exigirTexto(texto, "publicación");

        if (texto.trim().length() > ServicioPublicaciones.LONGITUD_MAXIMA) {
            throw new IllegalArgumentException("La publicación admite hasta "
                    + ServicioPublicaciones.LONGITUD_MAXIMA + " caracteres.");
        }
    }
}