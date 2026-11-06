package com.una.socialuna.model;

import java.time.LocalDateTime;

/**
 * ==== ARCHIVO TEMPORAL (de Leria, para que compile la persistencia) ====
 * Anthony: reemplázalo por tu versión final, manteniendo estos campos y
 * getters (los usa RepositorioSolicitudes). Al reemplazarlo, borra este
 * comentario.
 * Solicitud de amistad PENDIENTE. Al aceptarse pasa a ser una amistad en el
 * grafo; al rechazarse se elimina. Por eso en solicitudes.csv solo hay
 * pendientes.
 */

public class Solicitud {
    private final int idEmisor;
    private final int idReceptor;
    private final LocalDateTime fechaHora;

    // Recibe id de quien envia, id quien recibe y la fecha de envio
    public Solicitud(int idEmisor, int idReceptor, LocalDateTime fechaHora) {
        this.idEmisor = idEmisor;
        this.idReceptor = idReceptor;
        this.fechaHora = fechaHora;
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
