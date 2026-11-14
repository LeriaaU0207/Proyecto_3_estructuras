package com.una.socialuna.view;

import com.una.socialuna.service.ServicioAmistades;
import com.una.socialuna.service.SesionActual;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class PrincipalVista extends BorderPane {

    public PrincipalVista(ServicioAmistades amistades, SesionActual sesion,
                          Consumer<String> navegar, Runnable alSalir) {
        Label titulo = new Label("Social UNA");
        titulo.getStyleClass().add("titulo");

        Label saludo = new Label("Hola, " + sesion.getUsuario().getNombre());
        saludo.setWrapText(true);

        Label identidad = new Label(
                "@" + sesion.getUsuario().getUsuario()
                        + "  ·  ID: " + sesion.getIdUsuario());

        int pendientes =
                amistades.cantidadSolicitudesPendientes(sesion.getIdUsuario());

        Label resumen = new Label(
                amistades.amigosDe(sesion.getIdUsuario()).tamano()
                        + " amigos · " + pendientes + " solicitudes pendientes");

        VBox menu = new VBox(14,
                titulo, saludo, identidad, resumen,
                boton("Ver muro y publicar", () -> navegar.accept("muro")),
                boton("Buscar estudiantes", () -> navegar.accept("buscar")),
                boton("Mis amigos", () -> navegar.accept("amigos")),
                boton("Solicitudes (" + pendientes + ")",
                        () -> navegar.accept("solicitudes")),
                boton("Cerrar sesión", alSalir));

        menu.getStyleClass().add("tarjeta");
        menu.setMaxSize(520, USE_PREF_SIZE);

        VBox centro = new VBox(menu);
        centro.setAlignment(Pos.CENTER);
        centro.setStyle("-fx-padding: 24;");

        setCenter(centro);
    }

    private Button boton(String texto, Runnable accion) {
        Button boton = new Button(texto);
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setOnAction(evento -> accion.run());
        return boton;
    }
}