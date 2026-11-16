package com.una.socialuna.view;

import com.una.socialuna.model.Usuario;
import com.una.socialuna.service.ServicioAmistades;
import com.una.socialuna.service.ServicioUsuarios;
import com.una.socialuna.service.SesionActual;
import com.una.socialuna.util.ListaEnlazada;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class BuscarVista extends BorderPane {

    private final ServicioUsuarios usuarios;
    private final ServicioAmistades amistades;
    private final SesionActual sesion;

    private final TextField campoBusqueda = new TextField();
    private final Label resumen = new Label();
    private final ListView<Usuario> resultados = new ListView<>();

    public BuscarVista(ServicioUsuarios usuarios, ServicioAmistades amistades,
                       SesionActual sesion, Runnable alVolver) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.amistades = Objects.requireNonNull(amistades);
        this.sesion = Objects.requireNonNull(sesion);

        setPadding(new Insets(24));
        setTop(construirEncabezado(alVolver));
        setCenter(construirContenido());

        campoBusqueda.textProperty().addListener((observable, anterior, actual) -> buscar());
        buscar();
    }

    private HBox construirEncabezado(Runnable alVolver) {
        Label titulo = new Label("Buscar usuarios");
        titulo.getStyleClass().add("titulo");

        Label subtitulo = new Label("Encontrá compañeros por nombre o usuario");
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
        campoBusqueda.setPromptText("Nombre o nombre de usuario");
        campoBusqueda.setPrefWidth(320);
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);

        Button limpiar = new Button("Limpiar");
        limpiar.setOnAction(evento -> {
            campoBusqueda.clear();
            campoBusqueda.requestFocus();
        });

        HBox barra = new HBox(10, campoBusqueda, limpiar);
        barra.setAlignment(Pos.CENTER_LEFT);

        VBox buscador = new VBox(barra);
        buscador.getStyleClass().add("panel");

        resumen.getStyleClass().add("secundario");

        resultados.setPlaceholder(new Label("No se encontraron usuarios."));
        resultados.setCellFactory(lista -> new CeldaResultado());
        VBox.setVgrow(resultados, Priority.ALWAYS);

        VBox contenido = new VBox(14, buscador, resumen, resultados);
        contenido.setPadding(new Insets(18, 0, 0, 0));
        return contenido;
    }

    private void buscar() {
        int id = idActual();
        ListaEnlazada<Usuario> encontrados = campoBusqueda.getText().isBlank()
                ? usuarios.listar()
                : usuarios.buscar(campoBusqueda.getText());

        resultados.getItems().clear();
        for (Usuario usuario : encontrados) {
            if (usuario.getId() != id) {
                resultados.getItems().add(usuario);
            }
        }
        resumen.setText(resultados.getItems().size() + " usuario(s) encontrado(s).");
    }

    private void enviarSolicitud(Usuario destino) {
        if (!sesion.haySesion()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin sesión", "Debes iniciar sesión para enviar solicitudes.");
            return;
        }
        try {
            amistades.enviarSolicitud(sesion.getIdUsuario(), destino.getId());
            mostrarAlerta(Alert.AlertType.INFORMATION, "Solicitud enviada",
                    "Le enviaste una solicitud a " + destino.getNombre() + ".");
            resultados.refresh();
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "No se pudo enviar", e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setHeaderText(titulo);
        alerta.showAndWait();
    }

    private int idActual() {
        return sesion.haySesion() ? sesion.getIdUsuario() : -1;
    }

    private class CeldaResultado extends ListCell<Usuario> {
        @Override
        protected void updateItem(Usuario usuario, boolean vacia) {
            super.updateItem(usuario, vacia);
            if (vacia || usuario == null) {
                setGraphic(null);
                return;
            }

            Label nombre = new Label(usuario.getNombre());
            nombre.getStyleClass().add("autor");

            Label usuarioTexto = new Label("@" + usuario.getUsuario());
            usuarioTexto.getStyleClass().add("secundario");

            HBox identidad = new HBox(6, nombre, usuarioTexto);
            identidad.setAlignment(Pos.CENTER_LEFT);

            VBox datos = new VBox(2, identidad);
            HBox.setHgrow(datos, Priority.ALWAYS);

            Region espacio = new Region();
            HBox.setHgrow(espacio, Priority.ALWAYS);

            HBox fila = new HBox(12, Avatar.crear(usuario.getNombre()), datos, espacio);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.getStyleClass().add("fila-tarjeta");

            int id = idActual();
            if (amistades.sonAmigos(id, usuario.getId())) {
                fila.getChildren().add(estado("Ya son amigos", "estado-ok"));
            } else if (amistades.haySolicitudEntre(id, usuario.getId())) {
                fila.getChildren().add(estado("Solicitud pendiente", "estado-pendiente"));
            } else {
                Button enviar = new Button("Enviar solicitud");
                enviar.getStyleClass().add("primario");
                enviar.setOnAction(evento -> enviarSolicitud(usuario));
                fila.getChildren().add(enviar);
            }
            setGraphic(fila);
        }

        private Label estado(String texto, String clase) {
            Label etiqueta = new Label(texto);
            etiqueta.getStyleClass().addAll("etiqueta-estado", clase);
            return etiqueta;
        }
    }
}
