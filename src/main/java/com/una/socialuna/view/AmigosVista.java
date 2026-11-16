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

        setPadding(new Insets(24));
        setTop(construirEncabezado(alVolver));
        setCenter(construirContenido());
        refrescar();
    }

    private HBox construirEncabezado(Runnable alVolver) {
        Label titulo = new Label("Mis amigos");
        titulo.getStyleClass().add("titulo");

        Label subtitulo = new Label("Las personas que forman tu red");
        subtitulo.getStyleClass().add("subtitulo");

        HBox encabezado = new HBox(16);
        encabezado.setAlignment(Pos.CENTER_LEFT);

        if (alVolver != null) {
            Button volver = new Button("← Volver");
            volver.setOnAction(evento -> alVolver.run());
            encabezado.getChildren().add(volver);
        }
        encabezado.getChildren().add(new VBox(2, titulo, subtitulo));
        return encabezado;
    }

    private VBox construirContenido() {
        lista.setPlaceholder(new Label("Todavía no tienes amigos."));
        lista.setCellFactory(vista -> new CeldaAmigo());
        VBox.setVgrow(lista, Priority.ALWAYS);

        resumen.getStyleClass().add("secundario");

        VBox contenido = new VBox(14, resumen, lista);
        contenido.setPadding(new Insets(18, 0, 0, 0));
        return contenido;
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
            nombre.getStyleClass().add("autor");

            Label usuario = new Label("@" + amigo.getUsuario());
            usuario.getStyleClass().add("secundario");

            HBox identidad = new HBox(6, nombre, usuario);
            identidad.setAlignment(Pos.CENTER_LEFT);

            VBox datos = new VBox(2, identidad);
            HBox.setHgrow(datos, Priority.ALWAYS);

            Region espacio = new Region();
            HBox.setHgrow(espacio, Priority.ALWAYS);

            Button eliminar = new Button("Eliminar");
            eliminar.getStyleClass().add("peligro");
            eliminar.setOnAction(evento -> eliminarAmigo(amigo));

            HBox fila = new HBox(12,
                    Avatar.crear(amigo.getNombre()), datos, espacio, eliminar);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.getStyleClass().add("fila-tarjeta");
            setGraphic(fila);
        }
    }
}
