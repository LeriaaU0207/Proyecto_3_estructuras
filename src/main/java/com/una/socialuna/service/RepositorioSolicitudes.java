package com.una.socialuna.service;

import com.una.socialuna.model.Solicitud;
import com.una.socialuna.util.CsvUtil;
import com.una.socialuna.util.ListaEnlazada;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;

/**
 * Lee y escribe solicitudes.csv (solo solicitudes PENDIENTES).
 * Columnas: idEmisor,idReceptor,fecha   (fecha en formato ISO)
 */
public class RepositorioSolicitudes {

    private static final String[] ENCABEZADO = {"idEmisor", "idReceptor", "fecha"};

    private final Path archivo;

    /**
     * Recibe: la ruta del archivo solicitudes.csv.
     */
    public RepositorioSolicitudes(Path archivo) {
        this.archivo = archivo;
    }

    /**
     * Retorna: true si el archivo ya existe en disco.
     */
    public boolean existe() {
        return Files.exists(archivo);
    }

    /**
     * Lee todas las solicitudes pendientes del archivo.
     * Retorna: una lista de solicitudes (vacía si el archivo no existe).
     *
     * @throws IOException si el archivo no se puede leer o tiene datos inválidos
     */
    public ListaEnlazada<Solicitud> cargar() throws IOException {
        ListaEnlazada<Solicitud> solicitudes = new ListaEnlazada<>();
        for (String[] fila : CsvUtil.leerRegistros(archivo)) {
            CsvUtil.exigirColumnas(fila, ENCABEZADO.length, archivo);
            try {
                solicitudes.agregar(new Solicitud(
                        Integer.parseInt(fila[0].trim()),
                        Integer.parseInt(fila[1].trim()),
                        LocalDateTime.parse(fila[2].trim())));
            } catch (NumberFormatException | DateTimeParseException e) {
                throw new IOException("Dato inválido en " + archivo.getFileName()
                        + ": " + String.join(" | ", fila), e);
            }
        }
        return solicitudes;
    }

    /**
     * Guarda las solicitudes ordenadas por (idEmisor, idReceptor).
     * Recibe: la lista de solicitudes pendientes.
     *
     * @throws IOException si no se puede escribir
     */
    public void guardar(ListaEnlazada<Solicitud> solicitudes) throws IOException {
        ListaEnlazada<Solicitud> copia = solicitudes.copiar();
        copia.ordenar(Comparator.comparingInt(Solicitud::getIdEmisor)
                .thenComparingInt(Solicitud::getIdReceptor));
        ListaEnlazada<String[]> filas = new ListaEnlazada<>();
        for (Solicitud s : copia) {
            filas.agregar(new String[]{
                    String.valueOf(s.getIdEmisor()),
                    String.valueOf(s.getIdReceptor()),
                    s.getFechaHora().toString()});
        }
        CsvUtil.escribirArchivo(archivo, ENCABEZADO, filas);
    }
}
