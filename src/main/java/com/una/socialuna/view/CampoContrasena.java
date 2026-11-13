package com.una.socialuna.view;

import javafx.beans.property.StringProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class CampoContrasena extends VBox {
    private final PasswordField oculto = new PasswordField();
    private final TextField visible = new TextField();

    public CampoContrasena(String indicacion) {
        setSpacing(6);

        oculto.setPromptText(indicacion);
        visible.setPromptText(indicacion);
        oculto.setAccessibleText(indicacion);
        visible.setAccessibleText(indicacion);

        // Ambos controles comparten el texto; solamente uno ocupa espacio.
        visible.textProperty().bindBidirectional(oculto.textProperty());

        CheckBox mostrar = new CheckBox("Mostrar contraseña");

        visible.visibleProperty().bind(mostrar.selectedProperty());
        visible.managedProperty().bind(visible.visibleProperty());

        oculto.visibleProperty().bind(mostrar.selectedProperty().not());
        oculto.managedProperty().bind(oculto.visibleProperty());

        mostrar.selectedProperty().addListener((obs, antes, ahora) -> {
            TextField activo = ahora ? visible : oculto;
            activo.requestFocus();
            activo.positionCaret(activo.getLength());
        });

        getChildren().addAll(new StackPane(oculto, visible), mostrar);
    }

    public String getText() {
        return oculto.getText();
    }

    public StringProperty textProperty() {
        return oculto.textProperty();
    }

    public void clear() {
        oculto.clear();
    }

    public void alPresionarEnter(Runnable accion) {
        oculto.setOnAction(evento -> accion.run());
        visible.setOnAction(evento -> accion.run());
    }
}