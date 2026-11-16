package com.una.socialuna.view;

import com.una.socialuna.model.Comentario;
import com.una.socialuna.model.Publicacion;
import com.una.socialuna.model.Usuario;
import com.una.socialuna.service.ServicioPublicaciones;
import com.una.socialuna.service.ServicioUsuarios;
import com.una.socialuna.service.SesionActual;
import com.una.socialuna.util.AlertaUtil;
import com.una.socialuna.util.Validador;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MuroVista extends BorderPane {
    private final ServicioPublicaciones publicaciones;
    private final ServicioUsuarios usuarios;
    private final SesionActual sesion;

    private final TextArea contenido = new TextArea();
    private final VBox feed = new VBox(16);
    private final CheckBox soloMias = new CheckBox("Solo mis publicaciones");
    private final Button guardar = new Button("Publicar");
    private final Button cancelar = new Button("Cancelar edición");
    private final ScrollPane scroll = new ScrollPane();
    private final Label contadorFeed = new Label();

    private Integer idEdicion;

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public MuroVista(ServicioPublicaciones publicaciones, ServicioUsuarios usuarios,
                     SesionActual sesion, Runnable alVolver) {
        this.publicaciones = publicaciones;
        this.usuarios = usuarios;
        this.sesion = sesion;

        setPadding(new Insets(0));
        setTop(crearBarraSuperior(alVolver));

        Usuario actual = sesion.getUsuario();
        String nombreActual = actual == null ? "Invitado" : actual.getNombre();

        Label tituloCompositor = new Label("Nueva publicación");
        tituloCompositor.getStyleClass().add("autor");

        Label ayudaCompositor = new Label("Comparte algo con tu comunidad");
        ayudaCompositor.getStyleClass().add("secundario");

        HBox cabeceraCompositor = new HBox(12,
                Avatar.crear(nombreActual),
                new VBox(2, tituloCompositor, ayudaCompositor));
        cabeceraCompositor.setAlignment(Pos.CENTER_LEFT);

        contenido.setPromptText("¿Qué quieres compartir?");
        contenido.setWrapText(true);
        contenido.setPrefRowCount(3);

        Label contador = new Label();
        contador.getStyleClass().add("secundario");
        contador.textProperty().bind(Bindings.createStringBinding(
                () -> contenido.getText().trim().length() + "/"
                        + ServicioPublicaciones.LONGITUD_MAXIMA,
                contenido.textProperty()));

        guardar.getStyleClass().add("primario");
        guardar.setOnAction(evento -> guardar());

        cancelar.setOnAction(evento -> limpiar());
        cancelar.managedProperty().bind(cancelar.visibleProperty());
        cancelar.setVisible(false);

        Button actualizar = new Button("Actualizar");
        actualizar.getStyleClass().add("enlace");
        actualizar.setOnAction(evento -> refrescar());

        soloMias.setOnAction(evento -> refrescar());

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox accionesCompositor = new HBox(10, guardar, cancelar, espacio, contador);
        accionesCompositor.setAlignment(Pos.CENTER_LEFT);

        HBox filtros = new HBox(16, soloMias, actualizar);
        filtros.setAlignment(Pos.CENTER_LEFT);

        VBox compositor = new VBox(12,
                cabeceraCompositor,
                contenido,
                accionesCompositor,
                new Separator(),
                filtros);
        compositor.getStyleClass().add("tarjeta-post");
        compositor.setMaxWidth(Double.MAX_VALUE);

        feed.setFillWidth(true);

        VBox columna = new VBox(18, compositor, feed);
        columna.setMaxWidth(620);
        columna.setAlignment(Pos.TOP_CENTER);

        StackPane centro = new StackPane(columna);
        centro.setAlignment(Pos.TOP_CENTER);
        centro.setPadding(new Insets(22, 18, 40, 18));

        scroll.setContent(centro);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("feed-scroll");
        setCenter(scroll);

        refrescar();
    }

    private HBox crearBarraSuperior(Runnable alVolver) {
        Button volver = new Button("← Volver");
        volver.getStyleClass().add("enlace");
        volver.setOnAction(evento -> {
            if (contenido.getText().isBlank()
                    || AlertaUtil.confirmar(getScene().getWindow(),
                    "¿Volver y descartar el texto sin guardar?")) {
                alVolver.run();
            }
        });

        Label logo = new Label("Social UNA");
        logo.getStyleClass().add("logo");

        contadorFeed.getStyleClass().add("chip-contador");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox barra = new HBox(14, volver, logo, contadorFeed, espacio);
        barra.setAlignment(Pos.CENTER_LEFT);

        Usuario actual = sesion.getUsuario();
        if (actual != null) {
            Label usuario = new Label("@" + actual.getUsuario());
            usuario.getStyleClass().add("secundario");
            barra.getChildren().addAll(usuario, Avatar.crear(actual.getNombre()));
        }

        barra.getStyleClass().add("barra-superior");
        return barra;
    }

    private void refrescar() {
        feed.getChildren().clear();

        // El servicio ya devuelve el feed ordenado y filtrado por amistades
        var lista = soloMias.isSelected()
                ? publicaciones.obtenerPublicacionesDe(sesion.getIdUsuario())
                : publicaciones.obtenerFeed(sesion.getIdUsuario());

        actualizarContador(lista);

        if (lista.estaVacia()) {
            Label vacio = new Label("Todavía no hay publicaciones para mostrar.");
            vacio.getStyleClass().add("secundario");

            VBox caja = new VBox(vacio);
            caja.setAlignment(Pos.CENTER);
            caja.setPadding(new Insets(40));
            caja.setMaxWidth(Double.MAX_VALUE);
            caja.getStyleClass().add("tarjeta-post");
            feed.getChildren().add(caja);
            return;
        }

        for (Publicacion publicacion : lista) {
            feed.getChildren().add(crearTarjeta(publicacion));
        }
    }

    /**
     * Muestra en la barra superior cuántas publicaciones hay en el feed y
     * cuántas son nuevas (publicadas en las últimas 24 horas).
     */
    private void actualizarContador(com.una.socialuna.util.ListaEnlazada<Publicacion> lista) {
        int total = lista.tamano();
        int nuevas = 0;
        for (Publicacion publicacion : lista) {
            if (esNueva(publicacion)) {
                nuevas++;
            }
        }
        String texto = total + (total == 1 ? " publicación" : " publicaciones");
        if (nuevas > 0) {
            texto += " · " + nuevas + (nuevas == 1 ? " nueva" : " nuevas");
        }
        contadorFeed.setText(texto);
    }

    private VBox crearTarjeta(Publicacion publicacion) {
        var autor = usuarios.porId(publicacion.getIdAutor());
        String nombre = autor.map(Usuario::getNombre).orElse("Usuario eliminado");
        String arroba = autor.map(u -> "@" + u.getUsuario()).orElse("");

        Label nombreLabel = new Label(nombre);
        nombreLabel.getStyleClass().add("autor");

        Label meta = new Label(arroba + " · "
                + publicacion.getFechaHora().format(FECHA)
                + (publicacion.isEditada() ? " · editada" : ""));
        meta.getStyleClass().add("secundario");

        VBox datosAutor = new VBox(2, nombreLabel, meta);

        HBox cabecera = new HBox(12, Avatar.crear(nombre), datosAutor);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        if (esNueva(publicacion)) {
            Label nueva = new Label("Nueva");
            nueva.getStyleClass().add("chip-nueva");
            cabecera.getChildren().add(nueva);
        }

        Region espacioCabecera = new Region();
        HBox.setHgrow(espacioCabecera, Priority.ALWAYS);
        cabecera.getChildren().add(espacioCabecera);

        // Las publicaciones de amigos nunca muestran editar ni eliminar.
        if (publicacion.getIdAutor() == sesion.getIdUsuario()) {
            Button editar = new Button("Editar");
            editar.getStyleClass().add("enlace");
            editar.setOnAction(evento -> editar(publicacion));

            Button eliminar = new Button("Eliminar");
            eliminar.getStyleClass().add("enlace-peligro");
            eliminar.setOnAction(evento -> eliminar(publicacion));

            cabecera.getChildren().addAll(editar, eliminar);
        }

        Button meGusta = new Button();
        meGusta.getStyleClass().add("boton-like");

        Label cantidadMeGusta = new Label();
        cantidadMeGusta.getStyleClass().add("secundario");
        cantidadMeGusta.setMinWidth(0);
        cantidadMeGusta.setMaxWidth(360);

        Runnable alternarLike = () -> {
            try {
                publicaciones.alternarMeGusta(
                        publicacion.getId(), sesion.getIdUsuario());
                actualizarBotonMeGusta(meGusta, publicacion);
                actualizarCantidadMeGusta(cantidadMeGusta, publicacion);
            } catch (IllegalArgumentException | UncheckedIOException e) {
                AlertaUtil.error(getScene().getWindow(), e);
            }
        };

        actualizarBotonMeGusta(meGusta, publicacion);
        actualizarCantidadMeGusta(cantidadMeGusta, publicacion);
        meGusta.setOnAction(evento -> alternarLike.run());

        Label texto = new Label(publicacion.getContenido());
        texto.setWrapText(true);
        texto.setMinHeight(USE_PREF_SIZE);
        texto.getStyleClass().add("texto-publicacion");
        texto.setTooltip(new Tooltip("Doble clic para dar o quitar me gusta"));
        texto.setOnMouseClicked(evento -> {
            if (evento.getButton() == MouseButton.PRIMARY
                    && evento.getClickCount() == 2) {
                alternarLike.run();
            }
        });

        Label cantidadComentarios = new Label();
        cantidadComentarios.getStyleClass().add("secundario");
        actualizarCantidadComentarios(cantidadComentarios, publicacion);

        Region espacioAcciones = new Region();
        HBox.setHgrow(espacioAcciones, Priority.ALWAYS);

        HBox acciones = new HBox(10, meGusta, cantidadMeGusta,
                espacioAcciones, cantidadComentarios);
        acciones.setAlignment(Pos.CENTER_LEFT);

        VBox comentarios = new VBox(8);
        construirComentarios(publicacion, comentarios, cantidadComentarios);

        TextField campoComentario = new TextField();
        campoComentario.setPromptText("Agrega un comentario...");
        campoComentario.getStyleClass().add("campo-comentario");
        HBox.setHgrow(campoComentario, Priority.ALWAYS);

        Button enviar = new Button("Publicar");
        enviar.getStyleClass().add("enlace");

        Runnable enviarComentario = () -> {
            try {
                publicaciones.comentar(
                        publicacion.getId(), sesion.getIdUsuario(),
                        campoComentario.getText());
                campoComentario.clear();
                construirComentarios(publicacion, comentarios, cantidadComentarios);
            } catch (IllegalArgumentException | UncheckedIOException e) {
                AlertaUtil.error(getScene().getWindow(), e);
            }
        };

        enviar.setOnAction(evento -> enviarComentario.run());
        campoComentario.setOnAction(evento -> enviarComentario.run());

        HBox nuevoComentario = new HBox(8, campoComentario, enviar);
        nuevoComentario.setAlignment(Pos.CENTER_LEFT);

        VBox tarjeta = new VBox(12,
                cabecera,
                texto,
                acciones,
                new Separator(),
                comentarios,
                nuevoComentario);
        tarjeta.getStyleClass().add("tarjeta-post");
        tarjeta.setMaxWidth(Double.MAX_VALUE);
        return tarjeta;
    }

    /**
     * Reconstruye la lista de comentarios de una publicación: primero cada
     * comentario raíz con sus respuestas indentadas y su campo para responder.
     */
    private void construirComentarios(Publicacion publicacion, VBox contenedor,
                                      Label cantidadComentarios) {
        contenedor.getChildren().clear();

        for (Comentario raiz : publicacion.getComentarios()) {
            if (raiz.esRespuesta()) {
                continue;
            }

            VBox bloque = new VBox(6);
            VBox respuestas = new VBox(6);

            TextField campoRespuesta = new TextField();
            campoRespuesta.setPromptText("Responde a " + nombreDe(raiz) + "...");
            campoRespuesta.getStyleClass().add("campo-comentario");
            HBox.setHgrow(campoRespuesta, Priority.ALWAYS);

            Button enviarRespuesta = new Button("Publicar");
            enviarRespuesta.getStyleClass().add("enlace");

            Button cancelarRespuesta = new Button("Cancelar");
            cancelarRespuesta.getStyleClass().add("enlace-responder");

            HBox compositorRespuesta = new HBox(8,
                    campoRespuesta, enviarRespuesta, cancelarRespuesta);
            compositorRespuesta.setAlignment(Pos.CENTER_LEFT);
            compositorRespuesta.setPadding(new Insets(0, 0, 0, 34));
            compositorRespuesta.setVisible(false);
            compositorRespuesta.setManaged(false);

            Runnable enviar = () -> {
                try {
                    publicaciones.comentar(
                            publicacion.getId(), sesion.getIdUsuario(),
                            campoRespuesta.getText(), raiz.getId());
                    construirComentarios(publicacion, contenedor, cantidadComentarios);
                } catch (IllegalArgumentException | UncheckedIOException e) {
                    AlertaUtil.error(getScene().getWindow(), e);
                }
            };

            enviarRespuesta.setOnAction(evento -> enviar.run());
            campoRespuesta.setOnAction(evento -> enviar.run());
            cancelarRespuesta.setOnAction(evento -> {
                campoRespuesta.clear();
                compositorRespuesta.setVisible(false);
                compositorRespuesta.setManaged(false);
            });

            HBox filaRaiz = filaComentario(publicacion, raiz, contenedor,
                    cantidadComentarios, false, () -> {
                        compositorRespuesta.setVisible(true);
                        compositorRespuesta.setManaged(true);
                        campoRespuesta.requestFocus();
                    });

            for (Comentario respuesta : publicacion.getComentarios()) {
                if (respuesta.getIdComentarioPadre() == raiz.getId()) {
                    respuestas.getChildren().add(filaComentario(publicacion,
                            respuesta, contenedor, cantidadComentarios, true, null));
                }
            }

            bloque.getChildren().addAll(filaRaiz, respuestas, compositorRespuesta);
            contenedor.getChildren().add(bloque);
        }

        // Respuestas huérfanas (por ejemplo, un CSV editado a mano): se
        // muestran como comentarios normales para no perder información.
        for (Comentario comentario : publicacion.getComentarios()) {
            if (comentario.esRespuesta()
                    && !publicacion.getComentarios().contiene(
                    c -> c.getId() == comentario.getIdComentarioPadre())) {
                contenedor.getChildren().add(filaComentario(publicacion, comentario,
                        contenedor, cantidadComentarios, false, null));
            }
        }

        actualizarCantidadComentarios(cantidadComentarios, publicacion);
    }

    private HBox filaComentario(Publicacion publicacion, Comentario comentario,
                                VBox contenedor, Label cantidadComentarios,
                                boolean esRespuesta, Runnable alResponder) {
        var autor = usuarios.porId(comentario.getIdAutor());
        String nombre = autor.map(Usuario::getNombre).orElse("Usuario eliminado");

        Text nombreTexto = new Text(nombre + " ");
        nombreTexto.getStyleClass().add("comentario-autor");

        Text contenidoTexto = new Text(comentario.getContenido());
        contenidoTexto.getStyleClass().add("comentario-texto");

        TextFlow flujo = new TextFlow(nombreTexto, contenidoTexto);
        flujo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(flujo, Priority.ALWAYS);

        HBox fila = new HBox(6, flujo);
        fila.setAlignment(Pos.TOP_LEFT);
        if (esRespuesta) {
            fila.getStyleClass().add("comentario-respuesta");
        }

        if (alResponder != null) {
            Button responder = new Button("Responder");
            responder.getStyleClass().add("enlace-responder");
            responder.setOnAction(evento -> alResponder.run());
            fila.getChildren().add(responder);
        }

        if (comentario.getIdAutor() == sesion.getIdUsuario()) {
            Button borrar = new Button("×");
            borrar.getStyleClass().add("boton-icono");
            borrar.setOnAction(evento -> {
                boolean conRespuestas = publicacion.getComentarios().contiene(
                        c -> c.getIdComentarioPadre() == comentario.getId());
                String pregunta = conRespuestas
                        ? "¿Eliminar este comentario y sus respuestas?"
                        : "¿Eliminar este comentario?";
                if (!AlertaUtil.confirmar(getScene().getWindow(), pregunta)) {
                    return;
                }
                try {
                    if (publicaciones.eliminarComentario(
                            comentario.getId(), sesion.getIdUsuario())) {
                        construirComentarios(publicacion, contenedor, cantidadComentarios);
                    }
                } catch (UncheckedIOException e) {
                    AlertaUtil.error(getScene().getWindow(), e);
                }
            });
            fila.getChildren().add(borrar);
        }
        return fila;
    }

    private String nombreDe(Comentario comentario) {
        return usuarios.porId(comentario.getIdAutor())
                .map(Usuario::getNombre)
                .orElse("usuario eliminado");
    }

    private void actualizarBotonMeGusta(Button boton, Publicacion publicacion) {
        boolean gusta = publicacion.leGusta(sesion.getIdUsuario());
        boton.setText(gusta ? "♥ Te gusta" : "♡ Me gusta");
        boton.getStyleClass().remove("activo");
        if (gusta) {
            boton.getStyleClass().add("activo");
        }
    }

    /**
     * Muestra los nombres de quienes dieron "me gusta" (no solo el número),
     * por ejemplo: "Les gusta a Ana, Luis y 2 más".
     */
    private void actualizarCantidadMeGusta(Label etiqueta, Publicacion publicacion) {
        int cantidad = publicacion.cantidadMeGusta();
        if (cantidad == 0) {
            etiqueta.setText("Sin me gusta");
            return;
        }

        List<String> nombres = new ArrayList<>();
        for (Integer idUsuario : publicacion.getMeGusta()) {
            usuarios.porId(idUsuario).ifPresent(u -> nombres.add(u.getNombre()));
        }

        if (nombres.isEmpty()) {
            etiqueta.setText(cantidad + " me gusta");
            return;
        }

        String texto;
        if (nombres.size() == 1) {
            texto = "Le gusta a " + nombres.get(0);
        } else if (nombres.size() == 2) {
            texto = "Les gusta a " + nombres.get(0) + " y " + nombres.get(1);
        } else if (nombres.size() == 3) {
            texto = "Les gusta a " + nombres.get(0) + ", " + nombres.get(1)
                    + " y " + nombres.get(2);
        } else {
            texto = "Les gusta a " + nombres.get(0) + ", " + nombres.get(1)
                    + " y " + (nombres.size() - 2) + " más";
        }
        etiqueta.setText(texto);
    }

    private void actualizarCantidadComentarios(Label etiqueta, Publicacion publicacion) {
        int cantidad = publicacion.cantidadComentarios();
        etiqueta.setText(cantidad + (cantidad == 1 ? " comentario" : " comentarios"));
    }

    /** Retorna: true si la publicación tiene menos de 24 horas de creada. */
    private static boolean esNueva(Publicacion publicacion) {
        return publicacion.getFechaHora().isAfter(LocalDateTime.now().minusHours(24));
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
            scroll.setVvalue(0);

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
        scroll.setVvalue(0);
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
}
