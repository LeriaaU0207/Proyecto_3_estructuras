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

        setPadding(new Insets(24));
        setTop(construirEncabezado(alVolver));
        setCenter(construirContenido());
        refrescar();
    }

    private HBox construirEncabezado(Runnable alVolver) {
        Label titulo = new Label("Solicitudes de amistad");
        titulo.getStyleClass().add("titulo");

        Label subtitulo = new Label("Respondé quién puede sumarse a tu red");
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
        lista.setPlaceholder(new Label("No tienes solicitudes pendientes."));
        lista.setCellFactory(vista -> new CeldaSolicitud());
        VBox.setVgrow(lista, Priority.ALWAYS);

        resumen.getStyleClass().add("secundario");

        VBox contenido = new VBox(14, resumen, lista);
        contenido.setPadding(new Insets(18, 0, 0, 0));
        return contenido;
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
            nombre.getStyleClass().add("autor");

            Label usuario = new Label(usuarioEmisor(solicitud));
            usuario.getStyleClass().add("secundario");

            Label fecha = new Label(solicitud.getFechaHora().format(FORMATO_FECHA));
            fecha.getStyleClass().add("secundario");

            HBox identidad = new HBox(6, nombre, usuario);
            identidad.setAlignment(Pos.CENTER_LEFT);

            VBox datos = new VBox(2, identidad, fecha);
            HBox.setHgrow(datos, Priority.ALWAYS);

            Region espacio = new Region();
            HBox.setHgrow(espacio, Priority.ALWAYS);

            Button aceptar = new Button("Aceptar");
            aceptar.getStyleClass().add("primario");
            aceptar.setOnAction(evento -> aceptar(solicitud));

            Button rechazar = new Button("Rechazar");
            rechazar.getStyleClass().add("peligro");
            rechazar.setOnAction(evento -> rechazar(solicitud));

            HBox botones = new HBox(8, aceptar, rechazar);
            botones.setAlignment(Pos.CENTER_RIGHT);

            HBox fila = new HBox(12,
                    Avatar.crear(nombreEmisor(solicitud)), datos, espacio, botones);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.getStyleClass().add("fila-tarjeta");
            setGraphic(fila);
        }
    }
}
