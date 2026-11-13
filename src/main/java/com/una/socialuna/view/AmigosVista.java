package com.una.socialuna.view;

import com.una.socialuna.model.Usuario;
import com.una.socialuna.service.ServicioAmistades;
import com.una.socialuna.service.SesionActual;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class AmigosVista extends BorderPane {

    private final ServicioAmistades amistades;
    private final SesionActual sesion;

    private final Label resumen = new Label();
    private final ListView<Usuario> lista = new ListView<>();

    public AmigosVista(ServicioAmistades amistades, SesionActual sesion, Runnable alVolver) {
        this.amistades = Objects.requireNonNull(amistades);
        this.sesion = Objects.requireNonNull(sesion);

        setPadding(new Insets(16));
        setTop(construirEncabezado(alVolver));
        setCenter(construirContenido());
        refrescar();
    }

    private HBox construirEncabezado(Runnable alVolver) {
        Label titulo = new Label("Mis amigos");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox encabezado = new HBox(8);
        encabezado.setAlignment(Pos.CENTER_LEFT);
        encabezado.setPadding(new Insets(0, 0, 12, 0));

        if (alVolver != null) {
            Button volver = new Button("Volver");
            volver.setOnAction(evento -> alVolver.run());
            encabezado.getChildren().add(volver);
        }
        encabezado.getChildren().addAll(titulo, espacio);
        return encabezado;
    }

    private VBox construirContenido() {
        lista.setPlaceholder(new Label("Todavía no tienes amigos."));
        lista.setCellFactory(vista -> new CeldaAmigo());
        VBox.setVgrow(lista, Priority.ALWAYS);
        return new VBox(8, resumen, lista);
    }

    private void refrescar() {
        lista.getItems().clear();
        if (sesion.haySesion()) {
            for (Usuario amigo : amistades.amigosDe(sesion.getIdUsuario())) {
                lista.getItems().add(amigo);
            }
        }
        resumen.setText(lista.getItems().size() + " amigo(s).");
    }

    private void eliminarAmigo(Usuario amigo) {
        if (!sesion.haySesion()) {
            return;
        }
        ButtonType eliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar a " + amigo.getNombre() + " de tus amigos?", eliminar, ButtonType.CANCEL);
        confirmacion.setHeaderText("Eliminar amigo");
        confirmacion.showAndWait()
                .filter(boton -> boton == eliminar)
                .ifPresent(boton -> {
                    if (amistades.eliminarAmigo(sesion.getIdUsuario(), amigo.getId())) {
                        refrescar();
                        Alert aviso = new Alert(Alert.AlertType.INFORMATION,
                                "Eliminaste a " + amigo.getNombre() + " de tus amigos.", ButtonType.OK);
                        aviso.setHeaderText("Amigo eliminado");
                        aviso.showAndWait();
                    }
                });
    }

    private class CeldaAmigo extends ListCell<Usuario> {
        @Override
        protected void updateItem(Usuario amigo, boolean vacia) {
            super.updateItem(amigo, vacia);
            if (vacia || amigo == null) {
                setGraphic(null);
                return;
            }

            Label nombre = new Label(amigo.getNombre());
            nombre.setStyle("-fx-font-weight: bold;");
            Label usuario = new Label("@" + amigo.getUsuario());
            Region espacio = new Region();
            HBox.setHgrow(espacio, Priority.ALWAYS);

            Button eliminar = new Button("Eliminar");
            eliminar.setOnAction(evento -> eliminarAmigo(amigo));

            HBox fila = new HBox(8, nombre, usuario, espacio, eliminar);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(4));
            setGraphic(fila);
        }
    }
}
