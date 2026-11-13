package com.una.socialuna.view;

import com.una.socialuna.model.Publicacion;
import com.una.socialuna.service.ServicioPublicaciones;
import com.una.socialuna.service.ServicioUsuarios;
import com.una.socialuna.service.SesionActual;
import com.una.socialuna.util.AlertaUtil;
import com.una.socialuna.util.Validador;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;

public class MuroVista extends BorderPane {
    private final ServicioPublicaciones publicaciones;
    private final ServicioUsuarios usuarios;
    private final SesionActual sesion;

    private final TextArea contenido = new TextArea();
    private final ListView<Publicacion> lista = new ListView<>();
    private final CheckBox soloMias = new CheckBox("Solo mis publicaciones");
    private final Button guardar = new Button("Publicar");
    private final Button cancelar = new Button("Cancelar edición");

    private Integer idEdicion;

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public MuroVista(ServicioPublicaciones publicaciones, ServicioUsuarios usuarios,
                     SesionActual sesion, Runnable alVolver) {
        this.publicaciones = publicaciones;
        this.usuarios = usuarios;
        this.sesion = sesion;

        setPadding(new Insets(20));

        Label titulo = new Label("Muro de la comunidad");
        titulo.getStyleClass().add("titulo");

        Button volver = new Button("Volver");
        volver.setOnAction(evento -> {
            if (contenido.getText().isBlank()
                    || AlertaUtil.confirmar(getScene().getWindow(),
                    "¿Volver y descartar el texto sin guardar?")) {
                alVolver.run();
            }
        });

        setTop(new HBox(16, volver, titulo));

        contenido.setPromptText("¿Qué querés compartir?");
        contenido.setWrapText(true);
        contenido.setPrefRowCount(3);

        Label contador = new Label();
        contador.textProperty().bind(Bindings.createStringBinding(
                () -> contenido.getText().trim().length() + "/"
                        + ServicioPublicaciones.LONGITUD_MAXIMA,
                contenido.textProperty()));

        guardar.getStyleClass().add("primario");
        guardar.setOnAction(evento -> guardar());

        cancelar.setOnAction(evento -> limpiar());
        cancelar.managedProperty().bind(cancelar.visibleProperty());
        cancelar.setVisible(false);

        Button actualizar = new Button("Actualizar muro");
        actualizar.setOnAction(evento -> refrescar());
        soloMias.setOnAction(evento -> refrescar());

        lista.setPlaceholder(
                new Label("Todavía no hay publicaciones para mostrar."));

        lista.setCellFactory(vista -> new CeldaPublicacion());
        VBox.setVgrow(lista, Priority.ALWAYS);

        VBox centro = new VBox(12,
                contenido,
                new HBox(12, guardar, cancelar, contador),
                new HBox(16, soloMias, actualizar),
                lista);

        centro.setPadding(new Insets(18, 0, 0, 0));
        setCenter(centro);

        refrescar();
    }

    private void refrescar() {
        lista.getItems().clear();

        // El servicio ya devuelve el feed ordenado y filtrado por amistades
        var feed = soloMias.isSelected()
                ? publicaciones.obtenerPublicacionesDe(sesion.getIdUsuario())
                : publicaciones.obtenerFeed(sesion.getIdUsuario());

        for (Publicacion publicacion : feed) {
            lista.getItems().add(publicacion);
        }
    }

    private void guardar() {
        try {
            Validador.validarPublicacion(contenido.getText());

            if (idEdicion == null) {
                publicaciones.publicar(
                        sesion.getIdUsuario(), contenido.getText());
            } else {
                if (!AlertaUtil.confirmar(getScene().getWindow(),
                        "¿Guardar los cambios de esta publicación?")) {
                    return;
                }

                boolean editada = publicaciones.editar(
                        idEdicion, sesion.getIdUsuario(), contenido.getText());

                if (!editada) {
                    throw new IllegalArgumentException(
                            "La publicación no existe o no te pertenece.");
                }
            }

            limpiar();
            refrescar();

        } catch (IllegalArgumentException | UncheckedIOException e) {
            refrescar();
            AlertaUtil.error(getScene().getWindow(), e);
        }
    }

    private void editar(Publicacion publicacion) {
        if (!contenido.getText().isBlank()
                && !AlertaUtil.confirmar(getScene().getWindow(),
                "¿Descartar el texto actual y editar esta publicación?")) {
            return;
        }

        idEdicion = publicacion.getId();
        contenido.setText(publicacion.getContenido());
        guardar.setText("Guardar cambios");
        cancelar.setVisible(true);
        contenido.requestFocus();
    }

    private void eliminar(Publicacion publicacion) {
        if (!AlertaUtil.confirmar(getScene().getWindow(),
                "¿Deseas eliminar esta publicación?")) {
            return;
        }

        try {
            boolean eliminada = publicaciones.eliminar(
                    publicacion.getId(), sesion.getIdUsuario());

            if (!eliminada) {
                throw new IllegalArgumentException(
                        "La publicación no existe o no te pertenece.");
            }

            if (idEdicion != null && idEdicion == publicacion.getId()) {
                limpiar();
            }

            refrescar();

        } catch (IllegalArgumentException | UncheckedIOException e) {
            refrescar();
            AlertaUtil.error(getScene().getWindow(), e);
        }
    }

    private void limpiar() {
        idEdicion = null;
        contenido.clear();
        guardar.setText("Publicar");
        cancelar.setVisible(false);
    }

    private class CeldaPublicacion extends ListCell<Publicacion> {

        @Override
        protected void updateItem(Publicacion publicacion, boolean vacia) {
            super.updateItem(publicacion, vacia);

            setText(null);
            setGraphic(null);

            if (vacia || publicacion == null) return;

            String autor = usuarios.porId(publicacion.getIdAutor())
                    .map(u -> u.getNombre() + " (@" + u.getUsuario() + ")")
                    .orElse("Usuario eliminado");

            Label nombre = new Label(autor);
            nombre.setWrapText(true);
            nombre.getStyleClass().add("autor");

            Label fecha = new Label(
                    publicacion.getFechaHora().format(FECHA)
                            + (publicacion.isEditada() ? " · editada" : ""));

            fecha.getStyleClass().add("secundario");

            Label texto = new Label(publicacion.getContenido());
            texto.setWrapText(true);
            texto.setMinHeight(USE_PREF_SIZE);

            VBox tarjeta = new VBox(8, nombre, fecha, texto);
            tarjeta.getStyleClass().add("publicación");
            tarjeta.prefWidthProperty().bind(widthProperty().subtract(28));
            tarjeta.setMaxWidth(Double.MAX_VALUE);

            // Las publicaciones de amigos nunca muestran editar ni eliminar.
            if (publicacion.getIdAutor() == sesion.getIdUsuario()) {
                Button editar = new Button("Editar");
                editar.setOnAction(evento -> editar(publicacion));

                Button eliminar = new Button("Eliminar");
                eliminar.getStyleClass().add("peligro");
                eliminar.setOnAction(evento -> eliminar(publicacion));

                tarjeta.getChildren().add(new HBox(8, editar, eliminar));
            }

            setGraphic(tarjeta);
        }
    }
}