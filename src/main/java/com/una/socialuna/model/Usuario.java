package com.una.socialuna.model;

/**
 * ARCHIVO TEMPORAL PARA PODER COMPILAR LA PERSISTENCIA
 * Boris y Tony pueden reemplazarlo con su version final. Hay que mantener
 * los setters y getters.
 * Este representa a un estudiante registrado en la red social
 */
public class Usuario {

    private  final int id;
    private String nombre;
    private String usuario;
    private String contrasenaHash;

    //recibe id, nombre completo, nombre de usuario y contraseña ya hasheada
    public  Usuario(int id, String nombre, String usuario, String contrasenaHash) {
        this.id = id;
        this.nombre = nombre;
        this.usuario = usuario;
        this.contrasenaHash = contrasenaHash;
    }

    public int getId() {return  id; }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Usuario)) {
            return false;
        }
        return id == ((Usuario) otro).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return nombre + " (@" + usuario + ")";
    }
}
