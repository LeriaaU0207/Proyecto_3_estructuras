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

        setPadding(new Insets(16));
        setTop(construirEncabezado(alVolver));
        setCenter(construirContenido());

        campoBusqueda.textProperty().addListener((observable, anterior, actual) -> buscar());
        buscar();
    }

    private HBox construirEncabezado(Runnable alVolver) {
        Label titulo = new Label("Buscar usuarios");
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
        campoBusqueda.setPromptText("Nombre o nombre de usuario");
        campoBusqueda.setPrefWidth(320);
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);

        Button limpiar = new Button("Limpiar");
        limpiar.setOnAction(evento -> {
            campoBusqueda.clear();
            campoBusqueda.requestFocus();
        });

        HBox barra = new HBox(8, campoBusqueda, limpiar);
        barra.setAlignment(Pos.CENTER_LEFT);

        resultados.setPlaceholder(new Label("No se encontraron usuarios."));
        resultados.setCellFactory(lista -> new CeldaResultado());
        VBox.setVgrow(resultados, Priority.ALWAYS);

        return new VBox(8, barra, resumen, resultados);
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
            nombre.setStyle("-fx-font-weight: bold;");
            Label usuarioTexto = new Label("@" + usuario.getUsuario());
            Region espacio = new Region();
            HBox.setHgrow(espacio, Priority.ALWAYS);

            HBox fila = new HBox(8, nombre, usuarioTexto, espacio);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(4));

            int id = idActual();
            if (amistades.sonAmigos(id, usuario.getId())) {
                fila.getChildren().add(estado("Ya son amigos"));
            } else if (amistades.haySolicitudEntre(id, usuario.getId())) {
                fila.getChildren().add(estado("Solicitud pendiente"));
            } else {
                Button enviar = new Button("Enviar solicitud");
                enviar.setOnAction(evento -> enviarSolicitud(usuario));
                fila.getChildren().add(enviar);
            }
            setGraphic(fila);
        }

        private Label estado(String texto) {
            Label etiqueta = new Label(texto);
            etiqueta.setStyle("-fx-text-fill: #666666;");
            return etiqueta;
        }
    }
}
