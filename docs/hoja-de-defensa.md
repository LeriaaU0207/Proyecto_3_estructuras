# Hoja de defensa — Social UNA

Documento de una página para la defensa. Resume quién hizo qué, el guion de la
demostración y las preguntas más probables.

---

## 1. Integrantes y responsabilidades

| Integrante | Módulo | Archivos principales |
|---|---|---|
| **Anthony** | Autenticación y red social | `HashUtil`, `SesionActual`, `ServicioUsuarios`, `ServicioAutenticacion`, `ServicioAmistades`, `GrafoAmistades`, `Solicitud`, `BuscarVista`, `AmigosVista`, `SolicitudesVista` |
| **Leria** | Persistencia y publicaciones | `GestorDatos`, `CsvUtil`, `ListaEnlazada`, `Nodo`, los cuatro `Repositorio*`, `ServicioPublicaciones`, `Publicacion`, `Usuario`, `ContextoApp`, `datos_ejemplo/` |
| **Boris** | Interfaz y navegación | `NavegadorEscenas`, `MainApp`, `LoginVista`, `RegistroVista`, `PrincipalVista`, `MuroVista`, `AlertaUtil`, `Validador`, `CampoContrasena`, `estilos.css` |

---

## 2. Guion de la demostración (5–7 minutos)

### Paso 1 — Abrir la app

- Ejecutar `java -jar target/social-una-1.0.0.jar` o `Launcher` desde IntelliJ.
- Si `data/` está vacía, se copian los datos de `datos_ejemplo/` (auto-seed).
- Se ve la pantalla de **Login**.

### Paso 2 — Iniciar sesión

- Usuario: `ana` · Contraseña: `demo1234`.
- Entra a `PrincipalVista`, que muestra amigos y solicitudes pendientes.

### Paso 3 — Muro y feed

- Entrar a **Ver muro y publicar**.
- Publicar un mensaje nuevo.
- Mostrar que aparecen las publicaciones propias y las de los amigos
  (Luis), pero no las de Sofía.
- Editar o borrar una publicación propia.

### Paso 4 — Buscar y enviar solicitud

- Ir a **Buscar estudiantes**.
- Buscar `sofia`: ya hay una solicitud pendiente entre ellas, así que aparece
  como "Solicitud pendiente".
- Buscar `pedro` (si se registró) o crear un usuario nuevo y enviarle solicitud.

### Paso 5 — Aceptar solicitud

- Volver al menú e ir a **Solicitudes**.
- Aceptar la solicitud de Sofía.
- Ver que ahora aparece en **Mis amigos** y que su publicación entra al feed.

### Paso 6 — Persistencia

- Cerrar la aplicación por completo.
- Volver a abrirla e iniciar sesión con `ana`.
- Mostrar que la amistad, la solicitud aceptada y las publicaciones siguen ahí.

### Paso 7 — Cerrar sesión

- Usar **Cerrar sesión** y volver al login.

---

## 3. Qué explicar en cada módulo

### Autenticación (Anthony)

- La contraseña no se guarda en texto plano: se guarda `salt:hash`.
- El salt es distinto por usuario, así dos personas con la misma contraseña
  tienen hashes distintos.
- El login usa `HashUtil.verificar` y compara en tiempo constante.
- El mismo mensaje de error para usuario inexistente y contraseña incorrecta.

### Amistades (Anthony)

- El grafo es **no dirigido** y se guarda como lista de adyacencia.
- `enviarSolicitud` valida: no a sí mismo, no a un usuario inexistente, no si ya
  son amigos, no si ya hay solicitud.
- `aceptar` crea la arista y borra la solicitud.
- `amigosEnComun` compara las listas de vecinos.

### Persistencia (Leria)

- `GestorDatos` es el dueño de las estructuras en memoria.
- Cada cambio se guarda en su CSV con escritura atómica (`.tmp` + rename).
- `ListaEnlazada` y el grafo están implementados a mano, sin colecciones.

### Interfaz (Boris)

- `NavegadorEscenas` decide qué vista mostrar y bloquea el acceso sin sesión.
- Las vistas reciben los servicios por constructor; no hacen `new`.
- `ContextoApp` arma todos los servicios una sola vez.

---

## 4. Preguntas probables y respuestas cortas

**¿Por qué no usan una base de datos?**
Porque el proyecto pide persistencia en CSV y estructuras propias. Los
repositorios aíslan el formato, así que cambiar a base de datos solo tocaría esa
capa.

**¿Por qué SHA-256 y no guardar la contraseña?**
Guardar la contraseña en texto plano es inseguro. Con salt + hash, aunque
alguien lea el CSV no puede recuperar la contraseña.

**¿Por qué el grafo es no dirigido?**
Porque la amistad es mutua: si Ana es amiga de Luis, Luis es amigo de Ana. Por
eso `agregarArista(a, b)` agrega la relación en ambos vértices.

**¿Cómo evitan amistades duplicadas?**
Antes de agregar la arista se comprueba si ya existe en la lista de vecinos; si
existe, no se agrega y se devuelve `false`.

**¿Cómo se arma el feed?**
`ServicioPublicaciones.obtenerFeed` filtra las publicaciones cuyo autor sea el
usuario activo o uno de sus amigos, y las ordena de la más reciente a la más
vieja.

**¿Qué pasa si un CSV está dañado?**
Los repositorios lanzan `IOException` con el número de fila, `GestorDatos` la
convierte en `UncheckedIOException` y `MainApp` muestra una alerta en vez de
cerrarse sin explicación.

**¿Qué pasa si borro un usuario?**
Al cargar, `GestorDatos` descarta amistades, solicitudes y publicaciones
huérfanas (de usuarios que ya no existen).

**¿Por qué las vistas no hacen `new` de los servicios?**
Para que la navegación y los servicios estén desacoplados: `ContextoApp` los
crea una vez y se los pasa a `NavegadorEscenas`, que a su vez los pasa a cada
vista.

**¿Qué estructura se usa para los usuarios?**
Una lista enlazada propia (`ListaEnlazada<Usuario>`), no `ArrayList`.

**¿Cómo se guarda una amistad en el CSV?**
Como un par `idUsuarioA,idUsuarioB` con el menor primero, una sola vez por
amistad.

---

## 5. Comandos útiles

```bash
# Compilar y ejecutar
mvn javafx:run

# Generar el .jar
mvn clean package

# Ejecutar el .jar
java -jar target/social-una-1.0.0.jar
```

Credenciales demo: `ana` / `luis` / `sofia`, todas con `demo1234`.
