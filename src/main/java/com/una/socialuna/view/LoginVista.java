package com.una.socialuna.view;

import com.una.socialuna.service.ServicioAutenticacion;
import com.una.socialuna.service.SesionActual;
import com.una.socialuna.util.AlertaUtil;
import com.una.socialuna.util.Validador;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginVista extends BorderPane {

    public LoginVista(ServicioAutenticacion autenticacion, SesionActual sesion,
                      Runnable alEntrar, Runnable alRegistrar) {
        Label marcaTitulo = new Label("Social UNA");
        marcaTitulo.getStyleClass().add("marca-titulo");

        Label marcaTexto = new Label(
                "La red social de los estudiantes universitarios.");
        marcaTexto.getStyleClass().add("marca-texto");
        marcaTexto.setWrapText(true);

        VBox marca = new VBox(18,
                marcaTitulo,
                marcaTexto,
                punto("Publicá en tu muro y seguí a tus amigos"),
                punto("Encontrá compañeros por nombre o usuario"),
                punto("Gestioná tus amistades en un solo lugar"));
        marca.getStyleClass().add("marca");
        marca.setPrefWidth(350);
        marca.setMinWidth(350);
        marca.setMaxWidth(350);

        Label titulo = new Label("Iniciar sesión");
        titulo.getStyleClass().add("titulo");

        Label subtitulo = new Label("Ingresá con tu usuario y contraseña");
        subtitulo.getStyleClass().add("subtitulo");

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

        VBox formulario = new VBox(10,
                titulo, subtitulo,
                espacio(),
                new Label("Usuario"), usuario,
                new Label("Contraseña"), clave,
                espacio(),
                entrar, registro);
        formulario.setPadding(new Insets(42));
        formulario.setPrefWidth(420);
        formulario.setMaxWidth(420);
        VBox.setVgrow(formulario, Priority.ALWAYS);

        HBox contenido = new HBox(marca, formulario);
        contenido.getStyleClass().add("tarjeta");
        contenido.setStyle("-fx-padding: 0;");
        contenido.setMaxWidth(770);
        contenido.setMaxHeight(Region.USE_PREF_SIZE);

        StackPane centro = new StackPane(contenido);
        centro.setAlignment(Pos.CENTER);
        centro.setPadding(new Insets(30));
        setCenter(centro);
    }

    private Label punto(String texto) {
        Label etiqueta = new Label("•  " + texto);
        etiqueta.getStyleClass().add("marca-punto");
        etiqueta.setWrapText(true);
        return etiqueta;
    }

    private javafx.scene.layout.Region espacio() {
        javafx.scene.layout.Region region = new javafx.scene.layout.Region();
        region.setMinHeight(6);
        return region;
    }
}
