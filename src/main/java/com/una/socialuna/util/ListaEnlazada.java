package com.una.socialuna.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Lista enlazada simple genérica, implementada desde cero
 * (sin usar las colecciones de java.util). Manteniendo referencia
 * a la cabeza y a la cola, por lo que agregar al inicio y al final
 * es un comportamiento O(1).
 * Implementa Iterable, puede correr un for-each
 * @param <T> tipo de elementos en la lista.
 */

public class ListaEnlazada<T> implements Iterable<T> {
    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tamano;

    //Crea una lista vacía
    public ListaEnlazada() {
        this.cabeza = null;
        this.cola = null;
        this.tamano = 0;
    }

    //Agregar un elemento al final de la lista
    public void agregar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (cabeza == null) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            cola.siguiente = nuevo;
            cola = nuevo;
        }
        tamano++;
    }

    //Agrega un elemento al inicio de la lista
    public void agregarAlInicio(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        nuevo.siguiente = cabeza;
        cabeza = nuevo;
        if (cola == null) {
            cola = nuevo;
        }
        tamano++;
    }

    /**
     * Obtiene el elemento de una posicion 0(n)
     * Recibe el índice y retorna el elemento en la posicio
     * @throws IndexOutOfBoundsException si el indice no es valido
     */
    public T obtener (int indice) {
        if (indice < 0 || indice >= tamano) {
            throw new IndexOutOfBoundsException(
                    "Indice " + indice + " fuera de rango (tamano " + tamano + ").");
        }
        Nodo<T> actual = cabeza;
        for(int i=0; i<indice; i++) {
            actual = actual.siguiente;
        }
        return actual.dato;
    }

    /**
     * Busca el primer elemento que cumpla una condicion
     * que recibe la condicion y retorna el Optional con el
     * elemento, o vacio si no hay ninguno
     */
    public Optional<T> buscar(Predicate<? super T> condicion) {
        Nodo<T> actual = cabeza;
        while (actual.siguiente != null) {
            if (condicion.test(actual.dato)) {
                return Optional.of(actual.dato);
            }
            actual = actual.siguiente;
        }
        return Optional.empty();
    }

    /**
     * Indica si existe algun elementos que cumpla la condicion
     * Recibe la condicion a evaluar y retorna true si el elemento la cumple
     */
    public boolean contiene(Predicate<? super T> condicion) { return buscar(condicion).isPresent(); }

    /**
     * Elimina todos los elementos que cumplan la condicion 0(n)
     * Recibe la condicion que deben cumplir los elementos a eliminar
     * Retorna la cantidad de elementos eliminados
     */
    public int eliminarSi(Predicate<? super T> condicion) {
        int eliminados = 0;
        Nodo<T> anterior = null;
        Nodo<T> actual = cabeza;
        while (actual!= null) {
            if(condicion.test(actual.dato)) {
                if(anterior == null) {
                    cabeza = actual.siguiente;
                } else {
                    anterior.siguiente = actual.siguiente;
                }
                if(actual == cola) {
                    cola = anterior;
                }
                tamano--;
                eliminados++;
            } else {
                anterior = actual;
            }
            actual = actual.siguiente;
        }
        return eliminados;
    }

    /**
     * Elimina la primera aparcion de un elemento segun equals
     * Recibe el elementos a eliminar y retorna true si lo elimino
     * y retorna false si no estaba en la lista
     */
    public boolean eliminar (T elemento) {
        Nodo<T> anterior = null;
        Nodo<T> actual = cabeza;
        while (actual!=null) {
            boolean iguales = (elemento == null)
                    ? actual.dato == null
                    : elemento.equals(actual.dato);
            if (iguales) {
                if (anterior == null) {
                    cabeza = actual.siguiente;
                } else {
                    anterior.siguiente = actual.siguiente;
                }
                if (actual == cola)  {
                    cola = anterior;
                }
                tamano--;
                return true;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    //Retorna la cantidad de elementos de la lista
    public int tamano() {return tamano;}

    //Retorna true si la lista no tiene elementos
    public boolean estaVacia() {return tamano == 0;}

    //Elimina todos los elementos de la lista
    public void vaciar() {
        cabeza = null;
        cola = null;
        tamano = 0;
    }

    /*
    Crea una lista nueva con los elementos que cumplen la condicion,
    respetando el orden original. La lista original no cambia. Esta recibe
    la condicion que se debe cumplir los elementos y retorna una lista
    nueva con los elementos filtrados
     */
    public ListaEnlazada<T> filtrar (Predicate<? super T> condicion) {
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        for (T elemento : this) {
            if (condicion.test(elemento)) {
                resultado.agregar(elemento);
            }
        }
        return resultado;
    }

    /**
     * Crea una copia superficial de la lista (nodos nuevos, mismos elementos
     *  y retorna una lista nueva con los mismos elementos y en el mismo orden
     */
    public ListaEnlazada<T> copiar() {
        ListaEnlazada<T> copia = new ListaEnlazada<>();
        for (T elemento : this) {
            copia.agregar(elemento);
        }
        return copia;
    }

    /**
     * Ordena la lista con el merge sort sobre los nodos. O(n log n) y estable,
     * aca los elementos iguales conservan su orden relativo
     * Recibe el comparador q define el orden.
     */
    public void ordenar(Comparator<? super T> comparador) {
        if (tamano < 2) {
            return;
        }
        cabeza = ordenarNodos(cabeza, comparador);
        Nodo<T> actual = cabeza;
        while (actual.siguiente != null) {
            actual = actual.siguiente;
        }
        cola = actual;
    }

    public Nodo<T> ordenarNodos(Nodo <T> inicio, Comparator<? super T> comparador) {
        if (inicio == null || inicio.siguiente == null) {
            return inicio;
        }
        //Se busca la mitad con dos punteros (lento y rapido)
        Nodo<T> lento = inicio;
        Nodo<T> rapido = inicio.siguiente;
        while (rapido.siguiente != null) {
            lento = lento.siguiente;
            rapido = rapido.siguiente;
        }
        Nodo<T> segundaMitad = lento.siguiente;
        lento.siguiente = null;
        return mezclar(ordenarNodos(inicio,comparador), ordenarNodos(segundaMitad, comparador), comparador);
    }

    private Nodo<T> mezclar(Nodo<T> a, Nodo<T> b, Comparator<? super T> comparador) {
        Nodo<T> ficticio = new Nodo<>(null);
        Nodo<T> actual = ficticio;
        while (a != null && b != null) {
            if (comparador.compare(a.dato, b.dato) <= 0) {
                actual.siguiente = a;
                a = a.siguiente;
            } else {
                actual.siguiente = b;
                b = b.siguiente;
            }
            actual = actual.siguiente;
        }
        actual.siguiente = (a != null) ? a : b;
        return ficticio.siguiente;
    }

    //Retorna un iterador para recorrer la lista de inicioa fin
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Nodo<T> actual = cabeza;
            @Override
            public boolean hasNext() { return actual != null; }
            @Override
            public T next() {
                if(actual == null) {
                    throw new NoSuchElementException();
                }
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }

    //Retorna una representación en texto
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Nodo<T> actual = cabeza;
        while (actual != null) {
            sb.append(actual.dato);
            if(actual.siguiente != null) {
                sb.append(", ");
            }
            actual = actual.siguiente;
        }
        return sb.append("]").toString();
    }
}
