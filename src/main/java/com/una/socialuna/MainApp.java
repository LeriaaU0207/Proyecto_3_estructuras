package com.una.socialuna;

import com.una.socialuna.view.NavegadorEscenas;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.UncheckedIOException;

/**
 * Clase principal de JavaFX: arranca la aplicación.
 *
 * Flujo:
 *  1. Crea el ContextoApp (carga los CSV y arma los servicios).
 *  2. Abre la primera pantalla con NavegadorEscenas.
 *  3. Al cerrar la aplicación (stop) guarda todos los datos por seguridad.
 */
public class MainApp extends Application {

    private ContextoApp contexto;

    @Override
    public void start(Stage escenario) {
        try {
            contexto = ContextoApp.crear();
        } catch (UncheckedIOException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR,
                    "No se pudieron leer los archivos de datos (carpeta data/):\n" + e.getMessage());
            alerta.setHeaderText("Error al iniciar Social UNA");
            alerta.showAndWait();
            Platform.exit();
            return;
        }

        NavegadorEscenas navegador = new NavegadorEscenas(
                escenario,
                contexto.getAutenticacion(),
                contexto.getUsuarios(),
                contexto.getAmistades(),
                contexto.getPublicaciones(),
                contexto.getSesion());

        navegador.iniciar();
    }

    /**
     * Se ejecuta al cerrar la aplicación: guarda todos los datos.
     */
    @Override
    public void stop() {
        if (contexto != null) {
            try {
                contexto.guardarTodo();
            } catch (UncheckedIOException e) {
                System.err.println("No se pudo guardar al cerrar: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
