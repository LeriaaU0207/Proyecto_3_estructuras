package com.una.socialuna.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Solicitud {

    private final int idEmisor;
    private final int idReceptor;
    private final LocalDateTime fechaHora;

    public Solicitud(int idEmisor, int idReceptor, LocalDateTime fechaHora) {
        this.idEmisor = idEmisor;
        this.idReceptor = idReceptor;
        this.fechaHora = Objects.requireNonNull(fechaHora, "La fecha de la solicitud no puede ser null.");
    }

    public int getIdEmisor() {
        return idEmisor;
    }

    public int getIdReceptor() {
        return idReceptor;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    @Override
    public String toString() {
        return "Solicitud " + idEmisor + " -> " + idReceptor + " (" + fechaHora + ")";
    }
}
