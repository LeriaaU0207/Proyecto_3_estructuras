package com.una.socialuna;

import com.una.socialuna.service.GestorDatos;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.UncheckedIOException;

/**
 * Clase principal de JavaFX: arranca la aplicación.
 *
 * Flujo:
 *  1. Crea el GestorDatos y carga los CSV (si falla, muestra una alerta y cierra).
 *  2. Muestra la primera pantalla.
 *  3. Al cerrar la aplicación (stop) guarda todos los datos por seguridad.
 *
 * ==== LA VENTANA DE ESTE ARCHIVO ES TEMPORAL (de Leria) ====
 * Boris: reemplaza SOLO el bloque marcado como TEMPORAL dentro de start() por
 * la llamada a tu NavegadorEscenas (por ejemplo,
 * NavegadorEscenas.Iniciar(escenario, gestor)). El resto (carga y guardado)
 * se queda igual.
 */
public class MainApp extends Application {

    private GestorDatos gestor;

    /**
     * Recibe: la ventana principal que crea JavaFX.
     */
    @Override
    public void start(Stage escenario) {
        gestor = new GestorDatos();
        try {
            gestor.cargarTodo();
        } catch (UncheckedIOException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR,
                    "No se pudieron leer los archivos de datos (carpeta data/):\n" + e.getMessage());
            alerta.setHeaderText("Error al iniciar Social UNA");
            alerta.showAndWait();
            Platform.exit();
            return;
        }

        // ===== INICIO BLOQUE TEMPORAL (Boris: reemplazar por NavegadorEscenas) =====
        Label titulo = new Label("Social UNA");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        Label resumen = new Label(textoResumen());
        Button botonGuardar = new Button("Guardar ahora");
        botonGuardar.setOnAction(evento -> {
            gestor.guardarTodo();
            resumen.setText(textoResumen() + "\n(guardado)");
        });
        Label aviso = new Label("Pantalla temporal: la persistencia está cargada y lista.");

        VBox raiz = new VBox(12, titulo, resumen, botonGuardar, aviso);
        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(24));
        escenario.setTitle("Social UNA");
        escenario.setScene(new Scene(raiz, 460, 260));
        escenario.show();
        // ===== FIN BLOQUE TEMPORAL =====
    }

    private String textoResumen() {
        return "Usuarios: " + gestor.getUsuarios().tamano()
                + "   |   Publicaciones: " + gestor.getPublicaciones().tamano()
                + "   |   Solicitudes pendientes: " + gestor.getSolicitudes().tamano();
    }

    /**
     * Se ejecuta al cerrar la aplicación: guarda todos los datos.
     */
    @Override
    public void stop() {
        if (gestor != null) {
            try {
                gestor.guardarTodo();
            } catch (UncheckedIOException e) {
                System.err.println("No se pudo guardar al cerrar: " + e.getMessage());
            }
        }
    }

    /**
     * Recibe: los argumentos de la línea de comandos (no se usan).
     */
    public static void main(String[] args) {
        launch(args);
    }
}
