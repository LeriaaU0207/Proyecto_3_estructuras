package com.una.socialuna.view;

import com.una.socialuna.service.ServicioAutenticacion;
import com.una.socialuna.util.AlertaUtil;
import com.una.socialuna.util.Validador;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.UncheckedIOException;

public class RegistroVista extends BorderPane {

    public RegistroVista(ServicioAutenticacion autenticacion, Runnable alVolver) {
        Label titulo = new Label("Crear cuenta");
        titulo.getStyleClass().add("titulo");

        TextField nombre = new TextField();
        nombre.setPromptText("Nombre completo");

        TextField usuario = new TextField();
        usuario.setPromptText("Usuario: Permitido de 3 a 30 caracteres sin espacios");

        CampoContrasena clave =
                new CampoContrasena("Contraseña: mínimo 6 caracteres");

        CampoContrasena repetida =
                new CampoContrasena("Repita la contraseña");

        Button registrar = new Button("Registrarme");
        registrar.getStyleClass().add("primario");
        registrar.setMaxWidth(Double.MAX_VALUE);
        registrar.setDefaultButton(true);

        Button volver = new Button("Volver al inicio de sesión");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(evento -> alVolver.run());

        Runnable guardar = () -> {
            try {
                Validador.validarRegistro(
                        nombre.getText(), usuario.getText(),
                        clave.getText(), repetida.getText());

                // El servicio asigna el ID y genera el hash de la contraseña.
                autenticacion.registrar(
                        nombre.getText(), usuario.getText(), clave.getText());

                clave.clear();
                repetida.clear();

                AlertaUtil.exito(getScene().getWindow(),
                        "Cuenta creada. Ya puede iniciar sesión.");

                alVolver.run();

            } catch (IllegalArgumentException | UncheckedIOException e) {
                AlertaUtil.error(getScene().getWindow(), e);
            }
        };

        registrar.setOnAction(evento -> guardar.run());
        repetida.alPresionarEnter(guardar);

        VBox formulario = new VBox(10,
                titulo,
                new Label("Nombre completo"), nombre,
                new Label("Usuario"), usuario,
                new Label("Contraseña"), clave,
                new Label("Confirmar contraseña"), repetida,
                registrar, volver);

        formulario.getStyleClass().add("tarjeta");
        formulario.setMaxWidth(460);

        VBox centro = new VBox(formulario);
        centro.setAlignment(Pos.TOP_CENTER);
        centro.setStyle("-fx-padding: 24;");

        ScrollPane desplazamiento = new ScrollPane(centro);
        desplazamiento.setFitToWidth(true);

        setCenter(desplazamiento);
    }
}