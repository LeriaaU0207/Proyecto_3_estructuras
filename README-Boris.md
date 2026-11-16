# README — Boris

## Social UNA — Interfaz y navegación

**Red social estudiantil · JavaFX · Persistencia en CSV · Noviembre 2026**

Este documento resume el módulo de **interfaz y navegación** de Social UNA: cómo se
arman las pantallas, cómo se conectan con los servicios y qué decisiones visuales
se tomaron. El proyecto es un trabajo en equipo con Anthony y Leria.

---

## 1. Mi parte en el equipo

Mi responsabilidad fue la capa visual y de navegación:

- Arranque de la aplicación y punto de entrada.
- Navegación entre pantallas.
- Login y registro.
- Pantalla principal.
- Muro, feed y tarjetas de publicación.
- Validaciones visuales y mensajes de error.
- Estilos generales de la aplicación.

La regla que seguí fue que **las vistas no crean servicios con `new`**: todo llega
por constructor desde `ContextoApp`. Así la interfaz queda desacoplada de la
lógica y de la persistencia.

---

## 2. Archivos principales

| Archivo | Responsabilidad |
|---|---|
| `MainApp` | Arranca JavaFX, crea el contexto y abre la primera pantalla. |
| `Launcher` | Punto de entrada seguro para ejecutar la app y el `.jar`. |
| `NavegadorEscenas` | Cambia de vista y controla el acceso sin sesión. |
| `LoginVista` | Inicio de sesión. |
| `RegistroVista` | Alta de usuarios. |
| `PrincipalVista` | Menú principal con contadores de amigos y solicitudes. |
| `MuroVista` | Publicar, editar, borrar, ver el feed, dar me gusta, comentar y responder. |
| `Avatar` | Avatar circular con iniciales y color estable por usuario. |
| `AlertaUtil` | Alertas, errores y confirmaciones. |
| `Validador` | Validaciones de campos de la interfaz. |
| `CampoContrasena` | Campo de contraseña con opción de mostrar/ocultar. |
| `estilos.css` | Tema visual completo: azul oscuro, blanco y turquesa. |

---

## 3. Cómo está armada la interfaz

- La interfaz es **JavaFX programático**, sin FXML.
- Cada vista es un `BorderPane` o un contenedor de JavaFX.
- Las vistas reciben los servicios por constructor.
- `NavegadorEscenas` decide qué vista mostrar y bloquea el acceso si no hay sesión.
- `ContextoApp` crea los servicios una sola vez y se los pasa al navegador.
- Los estilos están centralizados en `src/main/resources/estilos.css`.

### Flujo de navegación

```
Login → Registro
Login → Principal
Principal → Muro
Principal → Buscar
Principal → Amigos
Principal → Solicitudes
Principal → Cerrar sesión → Login
```

---

## 4. Muro estilo Instagram

El muro se rediseñó como un feed centrado de aproximadamente **620 px**, con
tarjetas separadas para cada publicación.

Cada tarjeta muestra:

- Avatar, nombre, `@usuario`, fecha y aviso de “editada”.
- Chip **Nueva** si la publicación tiene menos de 24 horas.
- Texto de la publicación.
- Botón de me gusta y nombres de quienes reaccionaron.
- Cantidad de comentarios.
- Comentarios raíz y respuestas indentadas.
- Campo para comentar y botón **Responder** en cada comentario raíz.

### Interacciones

- **Me gusta:** botón `♡ Me gusta` / `♥ Te gusta`.
- **Doble clic:** doble clic sobre el texto de la publicación también da o quita
  el me gusta.
- **Comentarios:** se escriben desde el campo inferior de cada tarjeta.
- **Respuestas:** solo un nivel; responder a una respuesta cuelga del comentario
  raíz.
- **Borrado:** si se elimina un comentario raíz, también se eliminan sus
  respuestas, con confirmación previa.
- **Contador:** la barra superior muestra el total de publicaciones del feed y
  cuántas son nuevas.

---

## 5. Decisiones visuales y técnicas

- **Paleta consistente:** azul oscuro para títulos y estructura, blanco para
  tarjetas y turquesa para acciones y acentos.
- **Feed legible:** ancho máximo de 620 px, tarjetas con bordes suaves y
  separación uniforme.
- **Sin lógica en la vista:** la vista solo muestra datos y llama a los servicios.
- **Confirmaciones antes de borrar:** se usa `AlertaUtil.confirmar` para evitar
  pérdidas accidentales.
- **Mensajes claros:** los errores de validación se muestran con alertas, no con
  excepciones sin explicación.
- **Launcher:** se ejecuta `Launcher` y no `MainApp` directamente, porque una
  clase que extiende `Application` exige JavaFX como módulo al arrancar.

---

## 6. Cómo ejecutar

### Desde IntelliJ

1. Abrir el proyecto con el `pom.xml`.
2. Usar JDK 24 o superior.
3. Ejecutar `com.una.socialuna.Launcher`.

> No ejecutar `MainApp` directamente: puede fallar con
> `JavaFX runtime components are missing`.

### Desde Maven

```bash
mvn javafx:run
```

### Con el `.jar`

```bash
mvn clean package
java -jar target/social-una-1.0.0.jar
```

El `.jar` debe estar en una carpeta que tenga `data/` y `datos_ejemplo/` al lado.

---

## 7. Cómo probé la interfaz

- `PruebaUI`: login, registro, navegación, publicar, editar, borrar, me gusta por
  botón y por doble clic, comentar, responder y persistencia.
- `PruebaVistas`: creación y visualización de las vistas de amigos, búsqueda y
  solicitudes sin errores.
- Capturas de pantalla de login, registro, principal, muro, búsqueda, amigos y
  solicitudes.
- Revisión de layout: sin desbordes horizontales y con el feed centrado.

---

## 8. Preguntas probables de la defensa

**¿Por qué no usan FXML?**
Porque la interfaz se construye programáticamente con JavaFX. Así se ve
directamente en el código qué componentes se crean y cómo se conectan.

**¿Por qué las vistas no crean los servicios?**
Para mantenerlas desacopladas. `ContextoApp` crea los servicios una vez y
`NavegadorEscenas` se los pasa a cada vista por constructor.

**¿Cómo se evita entrar sin sesión?**
`NavegadorEscenas` verifica la sesión antes de mostrar las pantallas internas y
redirige al login si no hay usuario activo.

**¿Por qué `Launcher` y no `MainApp`?**
Porque `MainApp` extiende `Application` y Java exige JavaFX como módulo al
lanzarla directamente. `Launcher` tiene un `main` normal y delega en `MainApp`.

**¿Cómo se mantiene el estilo?**
Con `estilos.css`, clases CSS reutilizables y una paleta fija de azul oscuro,
blanco y turquesa.

---

## 9. Enlaces del equipo

- [README de Anthony](README.md)
- [README de Leria](README-Leria.md)
- [Manual técnico](docs/manual-tecnico.md)
- [Hoja de defensa](docs/hoja-de-defensa.md)

---

*Boris · Social UNA · Proyecto Programación III · 2026*
