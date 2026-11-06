package com.una.socialuna.util;

/**
 * Nodo de una lista enlazada simple
 * Este guarda un dato y la referencia al siguiente nodo.
 * @param <T> tipo de dato q almacena el nodo
 */

public class Nodo <T> {
    //Visibilidad de paquete: solo ListaEnlazada los manipula directamente
    T dato;
    Nodo<T> siguiente;

    /**
     * Crea un nodo sin siguiente
     * Recibe: el dato que va a guardar el nodo
     */
    public Nodo(T dato) {
        this.dato = dato;
        this.siguiente = null;
    }

    //Retorna el dato almacenado en el nodo
    public T getDato() { return dato; }
    //Retorna el nodo siguiente, o null si es el ultiimo
    public Nodo<T> getSiguiente() { return siguiente; }

}
