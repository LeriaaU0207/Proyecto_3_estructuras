package com.una.socialuna.view;

import com.una.socialuna.service.*;
import com.una.socialuna.util.AlertaUtil;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Objects;

public class NavegadorEscenas {
    private final Stage escenario;
    private final Scene escena;

    private final ServicioAutenticacion autenticacion;
    private final ServicioUsuarios usuarios;
    private final ServicioAmistades amistades;
    private final ServicioPublicaciones publicaciones;
    private final SesionActual sesion;

    public NavegadorEscenas(Stage escenario,
                            ServicioAutenticacion autenticacion,
                            ServicioUsuarios usuarios,
                            ServicioAmistades amistades,
                            ServicioPublicaciones publicaciones,
                            SesionActual sesion) {
        this.escenario = escenario;
        this.autenticacion = autenticacion;
        this.usuarios = usuarios;
        this.amistades = amistades;
        this.publicaciones = publicaciones;
        this.sesion = sesion;

        escena = new Scene(new BorderPane(), 960, 720);

        var css = Objects.requireNonNull(
                getClass().getResource("/estilos.css"),
                "Falta src/main/resources/estilos.css");

        escena.getStylesheets().add(css.toExternalForm());

        escenario.setScene(escena);
        escenario.setMinWidth(760);
        escenario.setMinHeight(640);
    }

    public void iniciar() {
        irA("login");
        escenario.show();
    }

    public void irA(String destino) {
        // Sin sesión solamente se permite entrar a login o registro.
        if (!sesion.haySesion()
                && !destino.equals("login")
                && !destino.equals("registro")) {
            destino = "login";

        } else if (sesion.haySesion()
                && (destino.equals("login") || destino.equals("registro"))) {
            destino = "principal";
        }

        Runnable volver = () -> irA("principal");

        Parent vista = switch (destino) {
            case "login" -> new LoginVista(
                    autenticacion, sesion,
                    () -> irA("principal"),
                    () -> irA("registro"));

            case "registro" -> new RegistroVista(
                    autenticacion, () -> irA("login"));

            case "principal" -> new PrincipalVista(
                    amistades, sesion, this::irA, this::cerrarSesion);

            case "muro" -> new MuroVista(
                    publicaciones, usuarios, sesion, volver);

            case "buscar" -> new BuscarVista(
                    usuarios, amistades, sesion, volver);

            case "amigos" -> new AmigosVista(
                    amistades, sesion, volver);

            case "solicitudes" -> new SolicitudesVista(
                    usuarios, amistades, sesion, volver);

            default -> throw new IllegalArgumentException(
                    "Vista desconocida: " + destino);
        };

        // Se reutiliza la ventana, pero se reconstruyen las vistas
        // para actualizar los datos al entrar
        escena.setRoot(vista);
        escenario.setTitle("Social UNA · " + destino);
    }

    private void cerrarSesion() {
        if (AlertaUtil.confirmar(escenario, "¿Quieres cerrar tu sesión?")) {
            sesion.cerrar();
            irA("login");
        }
    }
}