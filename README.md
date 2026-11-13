# Anthony

## Social UNA — Proyecto Programación III

**Red social estudiantil · JavaFX · Persistencia en CSV · Noviembre 2026**

Este README documenta **mi parte** del proyecto Social UNA, cómo la desarrollé y los prompts que usé como guía. El proyecto es un trabajo en equipo con Leria y Boris; aquí explico qué hice yo, cómo lo hice y qué aprendí en el camino.

---

## 1. De qué trata el proyecto

Social UNA es una red social de escritorio hecha con JavaFX. Permite registrarse, iniciar sesión, buscar usuarios, enviar y aceptar solicitudes de amistad, publicar en un muro y ver el feed de uno mismo y de sus amigos. Todo se guarda en archivos CSV, sin base de datos, y las estructuras principales (lista enlazada y grafo de amistades) están implementadas desde cero.

El flujo que se defiende es:

> Registro → Login → Buscar usuario → Enviar solicitud → Aceptar → Ver amigo → Publicar → Ver feed propio + de amigos → Cerrar la app → Reabrir → Todo sigue igual.

---

## 2. Mi rol en el equipo

Según la guía del equipo, mi parte es **autenticación y red social**:

- Seguridad de las contraseñas.
- Manejo de la sesión activa.
- Catálogo y búsqueda de usuarios.
- Registro e inicio de sesión.
- Grafo de amistades, solicitudes y amigos en común.
- Las vistas de **Buscar**, **Amigos** y **Solicitudes**.

En la práctica eso significa que todo lo que responde a “quién es quién” y “quién es amigo de quién” pasa por mi código.

### Clases que implementé

| Clase | Responsabilidad |
|---|---|
| `HashUtil` | Genera el hash SHA-256 con salt aleatorio y verifica contraseñas en formato `salt:hash`. |
| `SesionActual` | Guarda el usuario que tiene la sesión abierta y su id. |
| `ServicioUsuarios` | Catálogo de usuarios: listar, buscar por id, buscar por nombre de usuario, buscar por texto. |
| `ServicioAutenticacion` | Registro con validaciones y login con verificación de credenciales. |
| `ServicioAmistades` | Enviar, aceptar y rechazar solicitudes; eliminar amigos; listar pendientes y amigos en común. |
| `GrafoAmistades` | Grafo no dirigido con lista de adyacencia para representar las amistades. |
| `Solicitud` | Modelo de una solicitud de amistad pendiente. |
| `BuscarVista` | Buscador de usuarios con envío de solicitud. |
| `AmigosVista` | Lista de amigos con opción de eliminar. |
| `SolicitudesVista` | Bandeja de solicitudes pendientes con aceptar y rechazar. |

Los repositorios CSV (`RepositorioUsuarios`, `RepositorioAmistades`, `RepositorioSolicitudes`) y `GestorDatos` son la base que hizo Leria; mis servicios los usan sin duplicar la persistencia.

---

## 3. Cómo trabajé

1. **Leí la guía del equipo y el código base** antes de escribir nada. Ahí estaban claras las reglas: las vistas no crean servicios con `new`, los servicios piden las estructuras a `GestorDatos` y se guarda después de cada cambio.
2. **Dividí mi parte en pasos pequeños**, uno por commit, para poder revisar y volver atrás si algo salía mal:
   - `HashUtil`
   - `SesionActual`
   - `ServicioUsuarios`
   - `ServicioAutenticacion`
   - `GrafoAmistades` + `Solicitud`
   - `ServicioAmistades`
   - Las tres vistas
3. **Antes de dar un paso por terminado, compilaba y probaba.** No me quedaba con “parece que funciona”: escribía pruebas temporales, registraba usuarios, enviaba solicitudes, aceptaba, rechazaba, cerraba y volvía a cargar los datos para comprobar la persistencia.
4. **Los commits los hice yo**, con mensajes claros, y revisé el diff antes de subir.
5. **No cambié el trabajo de mis compañeros.** Cuando encontré un problema en código de Leria, lo hablé con ella; cuando algo de mi parte no encajaba con la base, lo adapté a lo que ya existía.
6. **Pedí explicaciones, no solo código.** Varias veces pedí que me explicaran por qué una clase estaba así, qué hacía el salt, por qué el feed se arma insertando al inicio o por qué el grafo es no dirigido.

---

## 4. Uso de la IA

Usé la IA como **herramienta de apoyo y guía**, no como un botón de “hacer todo”. La dinámica fue:

- Yo decidía **qué** hacer y en qué orden.
- Le pedía que me explicara el problema y que me propusiera una solución.
- Revisaba lo que generaba, lo probaba y pedía cambios cuando algo no me convencía (por ejemplo, quitar comentarios o no tocar archivos de otros).
- Hacía los commits y me quedaba con la responsabilidad de entender lo que subía.

En otras palabras: **me fui guiando y tratando de entender cada parte**. La IA me ayudó a avanzar más rápido y a no quedarme trabado, pero las decisiones, la revisión, las pruebas y la integración fueron mías.

Los prompts de la siguiente sección están transcritos de esa conversación.

---

## 5. Prompts que usé

| # | Prompt | Para qué lo usé |
|---|---|---|
| 1 | “Quiero que me ayudes con un proyecto en IntelliJ IDEA.” | Arrancar y ordenar el trabajo. |
| 2 | “¿Podés entrar a IntelliJ y trabajar dentro sin que yo programe?” | Entender hasta dónde podía ayudarme la herramienta y cómo se reflejaban los cambios. |
| 3 | “La estructura ya está en IntelliJ. Quiero ir poco a poco para tener varios commits, pero los commits los hago yo.” | Marcar el ritmo de trabajo y dejar claro que los commits eran míos. |
| 4 | “Empecemos con el paso 1 y no hace falta dejar un main de prueba.” | Empezar por `HashUtil` sin agregar archivos innecesarios. |
| 5 | “A `SesionActual` quítale los comentarios.” | Dejar el código limpio, sin comentarios de más. |
| 6 | “Antes de seguir, ¿cuántos pasos quedan?” | Planificar el resto de mi parte. |
| 7 | “Hagamos el 3 y 4 seguidos.” | Implementar `ServicioUsuarios` y `ServicioAutenticacion` en dos commits. |
| 8 | “Antes de seguir, examiná todo el proyecto; no cambies lo de los otros integrantes y revisá los comentarios que dicen lo que debo hacer.” | Auditar la integración y detectar pendientes sin pisar a mis compañeros. |
| 9 | “1 = sí. 2 = creá `ContextoApp` completo, pero solo lo de Leria; lo de Boris no, él se adapta. 3 = sí, hacé las vistas de Boris y avisale.” | Repartir el trabajo y no invadir la parte de Boris. |
| 10 | “¿Eso afecta el trabajo de Boris?” | Evaluar el riesgo de conflicto antes de tocar archivos ajenos. |
| 11 | “Si no hago las vistas de Boris, ¿podemos dejarlo así y decir que terminé?” | Decidir hasta dónde llegaba mi parte. |
| 12 | “Hacé solo lo que me toca a mí y dame un mensaje para Boris sobre el merge.” | Cerrar mi parte y dejar una entrega clara para el equipo. |
| 13 | “Hacé el README con mi nombre y los prompts que hice.” | Documentar mi proceso y mi participación. |

---

## 6. Decisiones técnicas que tomé

- **Contraseñas con SHA-256 + salt por usuario.** El salt se genera con `SecureRandom` y se guarda junto al hash como `salt:hash`. Así dos usuarios con la misma contraseña tienen hashes distintos.
- **Comparación en tiempo constante** al verificar contraseñas, para no filtrar información.
- **Mismo mensaje de error** para usuario inexistente y contraseña incorrecta: “Usuario o contraseña incorrectos”. No conviene revelar qué usuario existe.
- **Usuario único sin distinguir mayúsculas.** `ANA` y `ana` no pueden coexistir, y el login funciona igual con mayúsculas o minúsculas.
- **Los servicios no guardan copias de las estructuras**: piden siempre la lista o el grafo vivos a `GestorDatos`, y guardan el CSV después de cada cambio.
- **El grafo es no dirigido y simétrico.** Si A es amigo de B, B es amigo de A; no hay auto-amistades ni aristas duplicadas.
- **Las vistas no crean servicios con `new`.** Todo llega por constructor, como pide la guía, para que Boris pueda enchufar su `NavegadorEscenas` sin reescribirlas.
- **Mis vistas son `BorderPane`** y reciben un `Runnable` opcional para “Volver”, así no dependen de una clase de navegación que todavía no existe.
- **No toqué archivos de Boris** y no creé `NavegadorEscenas`: esa es su parte y él se adapta a la interfaz que dejamos.

---

## 7. Problemas que encontré y cómo los resolví

- **`ListaEnlazada.buscar` fallaba en listas vacías y se saltaba el último elemento.** Lo detecté al recargar la app: `GestorDatos.cargarTodo()` llamaba a `GrafoAmistades.buscarVertice()` y explotaba con `NullPointerException`. Lo corregí y se lo avisé a Leria, porque era código de ella.
- **`GrafoAmistades` tenía tres bugs:** agregaba los dos ids al mismo vértice (amistad asimétrica), no detectaba duplicados y `eliminarVertice` tenía la condición invertida. Lo reescribí completo, que además era lo que pedía el comentario del archivo temporal.
- **Al registrar un usuario no se creaba su vértice en el grafo.** Eso hacía que aceptar una solicitud no creara la amistad. Lo detectó una prueba y lo corregí en `ServicioAutenticacion`.
- **Las vistas no se podían conectar todavía** porque `ContextoApp` y `NavegadorEscenas` no existen. Lo resolví dejándolas desacopladas: reciben los servicios por constructor y la navegación por un callback.

---

## 8. Cómo probé mi parte

No me quedé solo con que compilara. Probé, entre otras cosas:

- **Hash:** misma contraseña genera hashes distintos; verifica la correcta; rechaza la incorrecta; formato `salt:hash`; hash dañado devuelve `false` sin romper.
- **Registro:** ids autoincrementales; usuario duplicado; nombre vacío; contraseña en blanco; persistencia en `usuarios.csv`.
- **Login:** correcto, incorrecto, usuario inexistente, mayúsculas/minúsculas.
- **Sesión:** iniciar, consultar id, cerrar, usuario `null`.
- **Amistades:** enviar, duplicada, a sí mismo, usuario inexistente, “ya te la envió”, aceptar, rechazar, eliminar amigo, volver a enviar después de eliminar.
- **Grafo:** simetría, sin duplicados, amigos en común, borrar vértice y arista, aristas únicas.
- **Persistencia:** cerrar y reabrir con un `GestorDatos` nuevo; las amistades, solicitudes y publicaciones siguen ahí.
- **Feed:** solo publicaciones propias y de amigos, más reciente primero; editar y borrar solo las propias.
- **Vistas:** las tres se crearon y se mostraron en una ventana de prueba con usuarios, amistades y solicitudes reales, sin errores.

---

## 9. Cómo ejecutar

### Desde IntelliJ

1. Abrir el proyecto (Maven lo reconoce por `pom.xml`).
2. Verificar que el JDK del proyecto sea **JDK 24 o superior** (yo usé JDK 26).
3. Ejecutar la clase `com.una.socialuna.Launcher`.

### Desde Maven

```bash
mvn javafx:run
```

### Generar el `.jar`

```bash
mvn clean package
```

El `.jar` queda en `target/` y se ejecuta desde una carpeta que tenga la carpeta `data/` al lado.

> **Nota:** la carpeta `data/` hoy solo tiene los encabezados de los CSV. Para probar hay que registrar usuarios desde la app o esperar los datos de ejemplo del equipo.

---

## 10. Estado de mi parte y pendientes del equipo

**Terminado por mí:**

- Seguridad, sesión, usuarios y autenticación.
- Grafo de amistades, solicitudes y amigos en común.
- Vistas de búsqueda, amigos y solicitudes.
- Pruebas de lógica, feed y persistencia.

**Pendiente del equipo (no es mío):**

- `ContextoApp` — Leria.
- `NavegadorEscenas`, `LoginVista`, `RegistroVista`, `PrincipalVista`, `MuroVista`, `AlertaUtil`, `Validador`, `CampoContrasena`, `estilos.css` — Boris.
- `datos_ejemplo/` con el auto-seed, `README` del equipo, `.jar` probado y UML — Leria y Boris.

---

## 11. Qué aprendí

- A usar **SHA-256 con salt** y por qué no se guardan contraseñas en texto plano.
- A separar **modelo, servicio y vista**, y a no crear dependencias dentro de la vista.
- A trabajar con **lista enlazada y grafo** implementados a mano, sin las colecciones de Java.
- A manejar **persistencia en CSV** con escritura atómica y validación de datos.
- A usar **ramas y merges** de Git, y a revisar el diff antes de commitear.
- A **leer código ajeno**, detectar bugs y avisar en vez de cambiarlo por mi cuenta.
- A probar de verdad: cerrar, reabrir y comprobar que los datos siguen ahí.

---

## 12. Equipo

| Integrante | Parte |
|---|---|
| **Anthony** | Autenticación, sesión, usuarios, amistades, grafo, búsqueda y vistas sociales. |
| **Leria** | Persistencia, `GestorDatos`, repositorios CSV, lista enlazada, publicaciones, feed y datos de ejemplo. |
| **Boris** | GUI base, `NavegadorEscenas`, login/registro, muro, validaciones visuales y estilos. |

---

*Anthony · Social UNA · Proyecto Programación III · 2026*
