package com.una.socialuna.model;

import com.una.socialuna.util.ListaEnlazada;

public class GrafoAmistades {

    private static class Vertice {
        final int id;
        final ListaEnlazada<Integer> vecinos = new ListaEnlazada<>();

        Vertice(int id) {
            this.id = id;
        }
    }

    private final ListaEnlazada<Vertice> vertices = new ListaEnlazada<>();

    public boolean agregarVertice(int id) {
        if (buscarVertice(id) != null) {
            return false;
        }
        vertices.agregar(new Vertice(id));
        return true;
    }

    public boolean eliminarVertice(int id) {
        Vertice vertice = buscarVertice(id);
        if (vertice == null) {
            return false;
        }
        for (Integer vecino : vertice.vecinos) {
            Vertice otro = buscarVertice(vecino);
            if (otro != null) {
                otro.vecinos.eliminarSi(v -> v == id);
            }
        }
        vertices.eliminarSi(v -> v.id == id);
        return true;
    }

    public boolean existeVertice(int id) {
        return buscarVertice(id) != null;
    }

    public boolean agregarArista(int a, int b) {
        if (a == b) {
            return false;
        }
        Vertice va = buscarVertice(a);
        Vertice vb = buscarVertice(b);
        if (va == null || vb == null) {
            return false;
        }
        if (va.vecinos.contiene(v -> v == b) || vb.vecinos.contiene(v -> v == a)) {
            return false;
        }
        va.vecinos.agregar(b);
        vb.vecinos.agregar(a);
        return true;
    }

    public boolean eliminarArista(int a, int b) {
        Vertice va = buscarVertice(a);
        Vertice vb = buscarVertice(b);
        if (va == null || vb == null) {
            return false;
        }
        boolean existia = va.vecinos.eliminarSi(v -> v == b) > 0;
        vb.vecinos.eliminarSi(v -> v == a);
        return existia;
    }

    public boolean sonAmigos(int a, int b) {
        if (a == b) {
            return false;
        }
        Vertice va = buscarVertice(a);
        return va != null && va.vecinos.contiene(v -> v == b);
    }

    public ListaEnlazada<Integer> amigosDe(int id) {
        Vertice vertice = buscarVertice(id);
        return vertice == null ? new ListaEnlazada<>() : vertice.vecinos.copiar();
    }

    public int cantidadAmigos(int id) {
        Vertice vertice = buscarVertice(id);
        return vertice == null ? 0 : vertice.vecinos.tamano();
    }

    public ListaEnlazada<Integer> amigosEnComun(int a, int b) {
        ListaEnlazada<Integer> comunes = new ListaEnlazada<>();
        Vertice va = buscarVertice(a);
        Vertice vb = buscarVertice(b);
        if (va == null || vb == null) {
            return comunes;
        }
        for (Integer amigo : va.vecinos) {
            if (vb.vecinos.contiene(v -> v == amigo)) {
                comunes.agregar(amigo);
            }
        }
        return comunes;
    }

    public ListaEnlazada<int[]> obtenerAristas() {
        ListaEnlazada<int[]> aristas = new ListaEnlazada<>();
        for (Vertice vertice : vertices) {
            for (Integer vecino : vertice.vecinos) {
                if (vertice.id < vecino) {
                    aristas.agregar(new int[]{vertice.id, vecino});
                }
            }
        }
        return aristas;
    }

    public int cantidadVertices() {
        return vertices.tamano();
    }

    private Vertice buscarVertice(int id) {
        return vertices.buscar(v -> v.id == id).orElse(null);
    }
}
