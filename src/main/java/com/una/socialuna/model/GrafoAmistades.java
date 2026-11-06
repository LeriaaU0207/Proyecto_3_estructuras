package com.una.socialuna.model;

import com.una.socialuna.util.ListaEnlazada;

/**
 * ARCHIVO TEMPORAL Para que compile la persistencia
 * Anthony: reemplázalo por tu versión final. Lo único que necesita la
 * persistencia (GestorDatos y RepositorioAmistades) es que sigas ofreciendo:
 * agregarVertice, agregarArista y obtenerAristas. Al reemplazarlo, borra
 * este comentario.
 * Grafo no dirigido de amistades, como lista de adyacencia hecha con
 * ListaEnlazada: cada vértice (usuario) guarda la lista de los ids de sus
 * amigos.
 */
public class GrafoAmistades {

    private static class Vertice {
        final int id;
        final ListaEnlazada<Integer> vecinos = new ListaEnlazada<>();

        Vertice(int id) { this.id = id; }
    }
    private final ListaEnlazada<Vertice> vertices = new ListaEnlazada<>();

    private Vertice buscarVertice(int id) { return vertices.buscar(v -> v.id == id).orElse(null); }

    //Agrega un usuario al grafo, recibe el ID del usuario y retorna true si se grego, false si ya existia
    public boolean agregarVertice(int id) {
        if (buscarVertice(id) != null) {
            return false;
        }
        vertices.agregar(new Vertice(id));
        return true;
    }

    /**
     * Elimina a un usuario del grafo y todas sus amistades
     * Recibe el id del usuario y retorna true si existia y si se elimino
     */
    public boolean eliminarVertice(int id) {
        if (buscarVertice(id) != null) { return false; }
        for (Vertice otro : vertices) {
            otro.vecinos.eliminarSi(vecino -> vecino == id);
        }
        vertices.eliminarSi(v -> v.id == id);
        return true;
    }

    /**
     * Crea una amistad (arista a ambos sentidos) entre 2 usuarios
     * Recibe los ID's de ambos usuarios y retorna true si los creo y
     * false si son el mismo, si alguno no existe o si ya eran amigos
     */
    public boolean agregarArista(int a, int b) {
        if (a == b) { return false; }
        Vertice va = buscarVertice(a);
        Vertice vb = buscarVertice(b);
        if (va == null || vb == null || va.vecinos.contiene(n -> n == b)) {
            return false;
        }
        va.vecinos.agregar(b);
        va.vecinos.agregar(a);
        return true;
    }

    /**
     * Elimina la amistad entre 2 usuarios, recibe ambos ID's y retorna true
     * si eran amigos o se elimina la amistad
     */
    public boolean eliminarArista(int a, int b) {
        Vertice va = buscarVertice(a);
        Vertice vb = buscarVertice(b);
        if (va == null || vb == null) {return false;}

        boolean existia = va.vecinos.eliminarSi(n -> n == b) > 0;
        vb.vecinos.eliminarSi(n -> n == a);
        return existia;
    }

    //Recibe ambos ID's de los usuarios y retorna si son amigos
    public boolean sonAmigos(int a, int b) {
        Vertice va = buscarVertice(a);
        return va != null && va.vecinos.contiene(n -> n == b);
    }

    //Recibe el ID del usuario y retorna una copia de la lista con ids de sus amigos
    public ListaEnlazada<Integer> amigosDe(int id) {
        Vertice v = buscarVertice(id);
        return (v == null) ? new ListaEnlazada<>() : v.vecinos.copiar();
    }

    //Recibe los ids de 2 usuarios y retorna los amigos en comun
    public ListaEnlazada<Integer> amigosEnComun(int a, int b) {
        Vertice va = buscarVertice(a);
        Vertice vb = buscarVertice(b);
        ListaEnlazada<Integer> comunes = new ListaEnlazada<>();
        if (va == null || vb == null) {
            return comunes;
        }
        for(Integer amigo : va.vecinos) {
            if (vb.vecinos.contiene(n -> n.equals(amigo))) {
                comunes.agregar(amigo);
            }
        }
        return comunes;
    }

    //Retorna todas las amistades, cada una y una sola vez, como pares
            // [idMenor, idMayor], lo usa el repositorio para gaurdar el CSV

    public ListaEnlazada<int[]> obtenerAristas() {
        ListaEnlazada<int[]> aristas = new ListaEnlazada<>();
        for (Vertice v : vertices) {
            for (Integer vecino : v.vecinos) {
                if (v.id < vecino) {
                    aristas.agregar(new int[]{v.id, vecino});
                }
            }
        }
        return aristas;
    }
}
