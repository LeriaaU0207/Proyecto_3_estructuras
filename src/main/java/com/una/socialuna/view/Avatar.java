package com.una.socialuna.view;

import javafx.scene.control.Label;

import java.util.Locale;

/**
 * Genera avatares con las iniciales del usuario y un color estable.
 * No necesita imágenes ni dependencias: es un Label circular.
 */
public final class Avatar {

    private static final String[] COLORES = {
            "#0f9c8d", "#1b4b7a", "#7c3aed", "#db2777", "#ea580c", "#0891b2"
    };

    private Avatar() {
    }

    public static Label crear(String nombre) {
        return crear(nombre, false);
    }

    public static Label crear(String nombre, boolean grande) {
        Label avatar = new Label(iniciales(nombre));
        avatar.getStyleClass().add(grande ? "avatar-grande" : "avatar");
        avatar.setStyle("-fx-background-color: " + color(nombre) + ";");
        return avatar;
    }

    static String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "?";
        }
        String[] partes = nombre.trim().split("\\s+");
        String iniciales = partes[0].substring(0, 1);
        if (partes.length > 1) {
            iniciales += partes[1].substring(0, 1);
        }
        return iniciales.toUpperCase(Locale.ROOT);
    }

    static String color(String nombre) {
        int indice = nombre == null ? 0 : Math.abs(nombre.hashCode()) % COLORES.length;
        return COLORES[indice];
    }
}
