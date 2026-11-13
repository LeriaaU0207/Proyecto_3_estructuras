package com.una.socialuna.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.io.UncheckedIOException;

public final class AlertaUtil {
    private AlertaUtil() { }

    private static Alert crear(Window ventana, Alert.AlertType tipo,
                               String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);

        if (ventana != null) alerta.initOwner(ventana);

        alerta.setTitle("Social UNA");
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.getDialogPane().setMinHeight(
                javafx.scene.layout.Region.USE_PREF_SIZE);

        return alerta;
    }

    public static void exito(Window ventana, String mensaje) {
        crear(ventana, Alert.AlertType.INFORMATION,
                "Listo", mensaje).showAndWait();
    }

    public static void error(Window ventana, Exception error) {
        String mensaje = error instanceof UncheckedIOException
                ? "No se logro completar el acceso a los archivos. Revisá los permisos de data/. "
                + "El cambio podría estar en memoria sin haberse guardado en disco."
                : error.getMessage();

        crear(ventana, Alert.AlertType.ERROR,
                "No se logro completar la acción",
                mensaje == null ? "Paso un error inesperado." : mensaje)
                .showAndWait();
    }

    public static boolean confirmar(Window ventana, String mensaje) {
        return crear(ventana, Alert.AlertType.CONFIRMATION,
                "Confirmar", mensaje)
                .showAndWait()
                .filter(ButtonType.OK::equals)
                .isPresent();
    }
}