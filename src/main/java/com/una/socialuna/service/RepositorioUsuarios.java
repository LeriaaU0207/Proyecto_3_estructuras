package com.una.socialuna.service;

import com.una.socialuna.model.Usuario;
import com.una.socialuna.util.CsvUtil;
import com.una.socialuna.util.ListaEnlazada;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Lee y escribe usuarios.csv.
 * Columnas: id, nombre, usuario, contrasenaHash
 */
public class RepositorioUsuarios {

    private static final String[] ENCABEZADO = {"id", "nombre", "usuario", "contrasenaHash"};

    private final Path archivo;

    /**
     * Recibe: la ruta del archivo usuarios.csv.
     */
    public RepositorioUsuarios(Path archivo) {
        this.archivo = archivo;
    }

    /**
     * Retorna: true si el archivo ya existe en disco.
     */
    public boolean existe() {
        return Files.exists(archivo);
    }

    /**
     * Lee todos los usuarios del archivo.
     * Retorna: una lista con los usuarios (vacía si el archivo no existe).
     *
     * @throws IOException si el archivo no se puede leer o tiene datos inválidos
     */
    public ListaEnlazada<Usuario> cargar() throws IOException {
        ListaEnlazada<Usuario> usuarios = new ListaEnlazada<>();
        for (String[] fila : CsvUtil.leerRegistros(archivo)) {
            CsvUtil.exigirColumnas(fila, ENCABEZADO.length, archivo);
            try {
                usuarios.agregar(new Usuario(
                        Integer.parseInt(fila[0].trim()), fila[1], fila[2], fila[3]));
            } catch (NumberFormatException e) {
                throw new IOException("Id inválido en " + archivo.getFileName()
                        + ": " + String.join(" | ", fila), e);
            }
        }
        return usuarios;
    }

    /**
     * Guarda todos los usuarios, ordenados por ID (así Git solo marca las
     * líneas que realmente cambiaron).
     * Recibe: la lista de usuarios a guardar.
     *
     * @throws IOException si no se puede escribir
     */
    public void guardar(ListaEnlazada<Usuario> usuarios) throws IOException {
        ListaEnlazada<Usuario> copia = usuarios.copiar();
        copia.ordenar(Comparator.comparingInt(Usuario::getId));
        ListaEnlazada<String[]> filas = new ListaEnlazada<>();
        for (Usuario u : copia) {
            filas.agregar(new String[]{
                    String.valueOf(u.getId()), u.getNombre(), u.getUsuario(), u.getContrasenaHash()});
        }
        CsvUtil.escribirArchivo(archivo, ENCABEZADO, filas);
    }
}
