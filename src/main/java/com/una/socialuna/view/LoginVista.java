package com.una.socialuna.view;

import com.una.socialuna.service.ServicioAutenticacion;
import com.una.socialuna.service.SesionActual;
import com.una.socialuna.util.AlertaUtil;
import com.una.socialuna.util.Validador;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class LoginVista extends BorderPane {

    public LoginVista(ServicioAutenticacion autenticacion, SesionActual sesion,
                      Runnable alEntrar, Runnable alRegistrar) {
        Label titulo = new Label("Social UNA");
        titulo.getStyleClass().add("titulo");

        Label subtitulo = new Label("Conectá con tu comunidad estudiantil");

        TextField usuario = new TextField();
        usuario.setPromptText("Nombre de usuario");

        CampoContrasena clave = new CampoContrasena("Contraseña");

        Button entrar = new Button("Iniciar sesión");
        entrar.getStyleClass().add("primario");
        entrar.setMaxWidth(Double.MAX_VALUE);
        entrar.setDefaultButton(true);

        Button registro = new Button("Crear una cuenta");
        registro.setMaxWidth(Double.MAX_VALUE);
        registro.setOnAction(evento -> alRegistrar.run());

        Runnable iniciar = () -> {
            try {
                Validador.exigirTexto(usuario.getText(), "usuario");
                Validador.exigirTexto(clave.getText(), "contraseña");

                sesion.iniciar(
                        autenticacion.login(usuario.getText(), clave.getText()));

                clave.clear();
                alEntrar.run();

            } catch (IllegalArgumentException e) {
                AlertaUtil.error(getScene().getWindow(), e);
            }
        };

        entrar.setOnAction(evento -> iniciar.run());
        usuario.setOnAction(evento -> iniciar.run());
        clave.alPresionarEnter(iniciar);

        VBox formulario = new VBox(12,
                titulo, subtitulo,
                new Label("Usuario"), usuario,
                new Label("Contraseña"), clave,
                entrar, registro);

        formulario.getStyleClass().add("tarjeta");
        formulario.setMaxSize(420, USE_PREF_SIZE);

        VBox centro = new VBox(formulario);
        centro.setAlignment(Pos.CENTER);
        centro.setStyle("-fx-padding: 24;");

        setCenter(centro);
    }
}
