package com.una.socialuna.view;

import com.una.socialuna.model.Usuario;
import com.una.socialuna.service.ServicioAmistades;
import com.una.socialuna.service.SesionActual;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class PrincipalVista extends BorderPane {

    public PrincipalVista(ServicioAmistades amistades, SesionActual sesion,
                          Consumer<String> navegar, Runnable alSalir) {
        setPadding(new Insets(28));

        Usuario usuario = sesion.getUsuario();
        int amigos = amistades.amigosDe(sesion.getIdUsuario()).tamano();
        int pendientes = amistades.cantidadSolicitudesPendientes(sesion.getIdUsuario());

        Label titulo = new Label("Social UNA");
        titulo.getStyleClass().add("titulo");

        Label subtitulo = new Label("Tu comunidad universitaria");
        subtitulo.getStyleClass().add("subtitulo");

        Label saludo = new Label("Hola, " + usuario.getNombre());
        saludo.getStyleClass().add("autor");
        saludo.setStyle("-fx-font-size: 20px;");

        Label identidad = new Label("@" + usuario.getUsuario() + "  ·  ID " + usuario.getId());
        identidad.getStyleClass().add("secundario");

        Label rol = new Label("Estudiante en Social UNA");
        rol.getStyleClass().add("secundario");

        HBox perfil = new HBox(18,
                Avatar.crear(usuario.getNombre(), true),
                new VBox(4, saludo, identidad, rol));
        perfil.setAlignment(Pos.CENTER_LEFT);

        HBox estadisticas = new HBox(12,
                chip(amigos, "Amigos"),
                chip(pendientes, "Solicitudes pendientes"));

        VBox panelPerfil = new VBox(18, perfil, estadisticas);
        panelPerfil.getStyleClass().add("panel");

        VBox menu = new VBox(10,
                boton("Ver muro y publicar", () -> navegar.accept("muro")),
                boton("Buscar estudiantes", () -> navegar.accept("buscar")),
                boton("Mis amigos", () -> navegar.accept("amigos")),
                boton("Solicitudes (" + pendientes + ")", () -> navegar.accept("solicitudes")),
                botonSalir(alSalir));
        menu.getStyleClass().add("panel");

        VBox contenido = new VBox(20,
                new VBox(2, titulo, subtitulo),
                panelPerfil,
                menu);
        contenido.setMaxWidth(660);

        VBox centro = new VBox(contenido);
        centro.setAlignment(Pos.TOP_CENTER);
        centro.setPadding(new Insets(10, 0, 0, 0));
        setCenter(centro);
    }

    private HBox chip(int numero, String texto) {
        Label valor = new Label(String.valueOf(numero));
        valor.getStyleClass().add("chip-numero");

        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("secundario");

        HBox chip = new HBox(new VBox(2, valor, etiqueta));
        chip.getStyleClass().add("chip");
        chip.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(chip, Priority.ALWAYS);
        return chip;
    }

    private Button boton(String texto, Runnable accion) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("menu");
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setOnAction(evento -> accion.run());
        return boton;
    }

    private Button botonSalir(Runnable accion) {
        Button boton = new Button("Cerrar sesión");
        boton.getStyleClass().add("peligro");
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setOnAction(evento -> accion.run());
        return boton;
    }
}
