package com.una.socialuna.view;

import com.una.socialuna.model.Solicitud;
import com.una.socialuna.model.Usuario;
import com.una.socialuna.service.ServicioAmistades;
import com.una.socialuna.service.ServicioUsuarios;
import com.una.socialuna.service.SesionActual;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class SolicitudesVista extends BorderPane {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ServicioUsuarios usuarios;
    private final ServicioAmistades amistades;
    private final SesionActual sesion;

    private final Label resumen = new Label();
    private final ListView<Solicitud> lista = new ListView<>();

    public SolicitudesVista(ServicioUsuarios usuarios, ServicioAmistades amistades,
                            SesionActual sesion, Runnable alVolver) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.amistades = Objects.requireNonNull(amistades);
        this.sesion = Objects.requireNonNull(sesion);

        setPadding(new Insets(16));
        setTop(construirEncabezado(alVolver));
        setCenter(construirContenido());
        refrescar();
    }

    private HBox construirEncabezado(Runnable alVolver) {
        Label titulo = new Label("Solicitudes de amistad");
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
        lista.setPlaceholder(new Label("No tienes solicitudes pendientes."));
        lista.setCellFactory(vista -> new CeldaSolicitud());
        VBox.setVgrow(lista, Priority.ALWAYS);
        return new VBox(8, resumen, lista);
    }

    private void refrescar() {
        lista.getItems().clear();
        if (sesion.haySesion()) {
            for (Solicitud solicitud : amistades.solicitudesPendientes(sesion.getIdUsuario())) {
                lista.getItems().add(solicitud);
            }
        }
        resumen.setText(lista.getItems().size() + " solicitud(es) pendiente(s).");
    }

    private void aceptar(Solicitud solicitud) {
        if (amistades.aceptar(solicitud.getIdEmisor(), solicitud.getIdReceptor())) {
            refrescar();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Solicitud aceptada",
                    "Ahora son amigos.");
        }
    }

    private void rechazar(Solicitud solicitud) {
        if (amistades.rechazar(solicitud.getIdEmisor(), solicitud.getIdReceptor())) {
            refrescar();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Solicitud rechazada",
                    "La solicitud fue eliminada.");
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setHeaderText(titulo);
        alerta.showAndWait();
    }

    private String nombreEmisor(Solicitud solicitud) {
        return usuarios.porId(solicitud.getIdEmisor())
                .map(Usuario::getNombre)
                .orElse("Usuario #" + solicitud.getIdEmisor());
    }

    private String usuarioEmisor(Solicitud solicitud) {
        return usuarios.porId(solicitud.getIdEmisor())
                .map(usuario -> "@" + usuario.getUsuario())
                .orElse("");
    }

    private class CeldaSolicitud extends ListCell<Solicitud> {
        @Override
        protected void updateItem(Solicitud solicitud, boolean vacia) {
            super.updateItem(solicitud, vacia);
            if (vacia || solicitud == null) {
                setGraphic(null);
                return;
            }

            Label nombre = new Label(nombreEmisor(solicitud));
            nombre.setStyle("-fx-font-weight: bold;");
            Label usuario = new Label(usuarioEmisor(solicitud));
            Label fecha = new Label(solicitud.getFechaHora().format(FORMATO_FECHA));
            fecha.setStyle("-fx-text-fill: #666666;");

            VBox datos = new VBox(2, nombre, usuario, fecha);
            Region espacio = new Region();
            HBox.setHgrow(espacio, Priority.ALWAYS);

            Button aceptar = new Button("Aceptar");
            aceptar.setOnAction(evento -> aceptar(solicitud));
            Button rechazar = new Button("Rechazar");
            rechazar.setOnAction(evento -> rechazar(solicitud));

            HBox botones = new HBox(8, aceptar, rechazar);
            botones.setAlignment(Pos.CENTER_RIGHT);

            HBox fila = new HBox(8, datos, espacio, botones);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(4));
            setGraphic(fila);
        }
    }
}
