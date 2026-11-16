# README — Leria

## Social UNA — Persistencia y publicaciones

**Red social estudiantil · JavaFX · Persistencia en CSV · Noviembre 2026**

Este documento resume el módulo de **persistencia, estructuras de datos y
publicaciones** de Social UNA: cómo se guardan los datos, cómo se cargan al
iniciar y cómo funcionan el feed, los me gusta y los comentarios. El proyecto es
un trabajo en equipo con Anthony y Boris.

---

## 1. Mi parte en el equipo

Mi responsabilidad fue la base de datos “de archivos” y la lógica de
publicaciones:

- Estructuras propias: lista enlazada y nodo.
- Lectura y escritura de archivos CSV.
- `GestorDatos`: dueño de las estructuras en memoria.
- Repositorios de usuarios, amistades, solicitudes, publicaciones, comentarios y
  me gusta.
- Auto-seed de `datos_ejemplo/` a `data/`.
- CRUD de publicaciones y armado del feed.
- Persistencia de me gusta, comentarios y respuestas.

---

## 2. Archivos principales

| Archivo | Responsabilidad |
|---|---|
| `GestorDatos` | Carga, guarda y mantiene las estructuras en memoria. |
| `CsvUtil` | Lectura y escritura CSV con comillas y escritura atómica. |
| `ListaEnlazada<T>` | Lista enlazada propia, sin colecciones de Java. |
| `Nodo<T>` | Nodo de la lista enlazada. |
| `RepositorioUsuarios` | Lee y escribe `usuarios.csv`. |
| `RepositorioAmistades` | Lee y escribe `amistades.csv`. |
| `RepositorioSolicitudes` | Lee y escribe `solicitudes.csv`. |
| `RepositorioPublicaciones` | Lee y escribe `publicaciones.csv`. |
| `RepositorioComentarios` | Lee y escribe `comentarios.csv`; migra de 5 a 6 columnas. |
| `RepositorioMeGusta` | Lee y escribe `me_gusta.csv`. |
| `ServicioPublicaciones` | Publicar, editar, eliminar, feed, me gusta, comentarios y respuestas. |
| `Publicacion` | Modelo de una publicación con sus interacciones. |
| `Comentario` | Modelo de comentario o respuesta. |
| `Usuario` | Modelo de usuario. |
| `ContextoApp` | Arma `GestorDatos` y los servicios una sola vez. |
| `datos_ejemplo/` | Datos iniciales que se copian a `data/`. |

---

## 3. Estructuras de datos propias

### Lista enlazada

Se usa para usuarios, publicaciones, solicitudes y comentarios. No se usan
`ArrayList` ni `LinkedList` de Java.

- `agregar` inserta al final.
- `agregarAlInicio` inserta al principio.
- `buscar` recorre hasta encontrar y devuelve `Optional`.
- `filtrar` devuelve una lista nueva con los elementos que cumplen una condición.
- `eliminarSi` borra todos los que cumplan la condición.
- `ordenar` ordena con el comparador que reciba.
- `copiar` permite guardar sin modificar el orden original.

### Feed

Las publicaciones se guardan en una `ListaEnlazada<Publicacion>` ordenada de la
más reciente a la más antigua. Cada publicación nueva se inserta **al inicio**,
así el feed no necesita ordenarse cada vez.

---

## 4. Persistencia en CSV

Formato de los archivos (primera fila = encabezado):

```text
usuarios.csv        id,nombre,usuario,contrasenaHash
amistades.csv       idUsuarioA,idUsuarioB
solicitudes.csv     idEmisor,idReceptor,fecha
publicaciones.csv   id,idAutor,fechaHora,contenido,editada
comentarios.csv     id,idPublicacion,idAutor,fechaHora,contenido,idComentarioPadre
me_gusta.csv        idPublicacion,idUsuario
```

- Las fechas van en ISO-8601 (`2026-11-01T09:00`).
- Los campos con comas, comillas o saltos de línea se escriben entre comillas.
- Cada guardado escribe un archivo `.tmp` y luego reemplaza el original
  (escritura atómica).
- Después de cada cambio, el servicio guarda el CSV que corresponda.
- Si un CSV está dañado, el repositorio lanza `IOException` con el número de fila
  y `GestorDatos` la convierte en `UncheckedIOException` para que la vista muestre
  una alerta.

### Auto-seed

Al iniciar, `GestorDatos.cargarTodo()` revisa si `data/` tiene usuarios. Si está
vacía y existe `datos_ejemplo/` al lado, copia los seis CSV. A partir de ahí, la
aplicación trabaja y guarda siempre sobre `data/`.

---

## 5. Publicaciones, me gusta y comentarios

### Publicaciones

- `publicar` valida el texto y crea la publicación con la fecha actual.
- `editar` solo permite cambiar publicaciones propias y marca `editada = true`.
- `eliminar` solo permite borrar publicaciones propias.
- Al borrar una publicación se eliminan también sus me gusta y comentarios.

### Me gusta

- Se guardan en `me_gusta.csv` como pares `idPublicacion,idUsuario`.
- `alternarMeGusta` agrega el par si no existe y lo quita si ya existe.
- El CSV se mantiene separado para no modificar el formato de
  `publicaciones.csv`.

### Comentarios y respuestas

- Se guardan en `comentarios.csv`.
- La columna `idComentarioPadre` indica el comentario respondido:
  - `0` = comentario raíz.
  - Un id mayor que `0` = respuesta.
- Las respuestas tienen **un solo nivel**: responder a una respuesta se guarda
  como respuesta del comentario raíz.
- Al borrar un comentario raíz se borran también sus respuestas.
- El siguiente id de comentario se calcula como el máximo existente + 1.

### Compatibilidad

Los archivos viejos de `comentarios.csv` con 5 columnas se siguen leyendo. En
esos archivos todos los comentarios quedan como raíces (`idComentarioPadre = 0`)
y se migran a 6 columnas en el siguiente guardado.

---

## 6. Cómo probé la persistencia

- **`PruebaSocial`**: me gusta, comentarios, respuestas, validaciones, borrado en
  cascada, persistencia al reabrir y compatibilidad con CSV viejos de 5 columnas.
- **`PruebaSeed`**: auto-seed de los seis CSV, usuarios, amistades, solicitudes,
  publicaciones, me gusta y comentarios de ejemplo.
- **`PruebaFeed`**: orden del feed, publicaciones propias y de amigos, edición y
  borrado, persistencia al reabrir.
- **Prueba de reapertura**: se cierra la app, se crea un `GestorDatos` nuevo y se
  comprueba que todo sigue igual.

---

## 7. Preguntas probables de la defensa

**¿Por qué no usan una base de datos?**
Porque el proyecto pide persistencia en CSV y estructuras propias. Los
repositorios aíslan el formato, así que cambiar a base de datos solo tocaría esa
capa.

**¿Cómo se evita perder datos si falla un guardado?**
Cada guardado escribe primero un `.tmp` y luego reemplaza el archivo original.
Así no se deja el CSV a medio escribir.

**¿Cómo se arma el feed?**
`ServicioPublicaciones.obtenerFeed` filtra las publicaciones del usuario activo y
de sus amigos, y las devuelve de la más reciente a la más antigua.

**¿Dónde se guardan los me gusta y los comentarios?**
En archivos separados: `me_gusta.csv` y `comentarios.csv`. Así no se cambia el
formato de `publicaciones.csv`.

**¿Qué pasa si se borra un comentario que tiene respuestas?**
Se eliminan también sus respuestas para no dejar datos huérfanos.

**¿Qué pasa con un `comentarios.csv` viejo?**
Se lee igual; los comentarios quedan como raíces y el archivo se migra a 6
columnas cuando se vuelve a guardar.

---

## 8. Enlaces del equipo

- [README de Anthony](README.md)
- [README de Boris](README-Boris.md)
- [Manual técnico](docs/manual-tecnico.md)
- [Hoja de defensa](docs/hoja-de-defensa.md)

---

*Leria · Social UNA · Proyecto Programación III · 2026*
