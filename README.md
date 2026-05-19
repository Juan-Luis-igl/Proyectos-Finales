# Agenda Personal de Tareas

Autor: **Juan Luis Iglesias Llorena**
Asignatura: Entornos de Desarrollo

Proyecto final de la asignatura. Aplicación de escritorio para gestionar tareas
personales, con interfaz gráfica en JavaFX, persistencia en fichero,
autenticación básica con **registro de usuarios desde la propia interfaz** y
pruebas unitarias con JUnit 5.

---

## Tabla de contenidos

1. [Requisitos](#requisitos)
2. [Estructura del proyecto](#estructura-del-proyecto)
3. [Cómo abrirlo en IntelliJ IDEA](#cómo-abrirlo-en-intellij-idea)
4. [Cómo ejecutar la aplicación](#cómo-ejecutar-la-aplicación)
5. [Cómo ejecutar las pruebas](#cómo-ejecutar-las-pruebas)
6. [Cómo generar el JavaDoc](#cómo-generar-el-javadoc)
7. [Usuarios y registro](#usuarios-y-registro)
8. [Tareas privadas por usuario](#tareas-privadas-por-usuario)
9. [Cierre de sesión](#cierre-de-sesión)
10. [Persistencia](#persistencia)

---

## Requisitos

- JDK 17 o superior (probado con JDK 17 y JDK 21).
- Apache Maven 3.9 o superior.
- IntelliJ IDEA Community o Ultimate (recomendado, con plugin Maven activo).

JavaFX se descarga automáticamente como dependencia Maven (`org.openjfx:javafx-controls`
y `org.openjfx:javafx-fxml`); **no es necesario instalar JavaFX manualmente**.

## Estructura del proyecto

```
agenda-tareas-javafx/
├── pom.xml
├── docs/
│   └── Entrega-Final-Agenda-Tareas.pdf   ← documento de entrega
├── src/
│   ├── main/java/com/juaniglesias/agendatareas/
│   │   ├── model/        Task, Priority, Status
│   │   ├── manager/      TaskManager (lógica de negocio)
│   │   ├── persistence/  TaskRepository (CSV)
│   │   ├── auth/         AuthService (login + registro persistente)
│   │   ├── ui/           AgendaApp, LoginView, MainView
│   │   └── Launcher.java (clase lanzadora — evita el error de runtime JavaFX en IntelliJ)
│   └── test/java/com/juaniglesias/agendatareas/
│       ├── auth/         AuthServiceTest
│       ├── manager/      TaskManagerTest
│       └── persistence/  TaskRepositoryTest
```

## Cómo abrirlo en IntelliJ IDEA

1. `File → Open…` y selecciona la carpeta `agenda-tareas-javafx` (la que contiene
   `pom.xml`).
2. IntelliJ detecta automáticamente el proyecto Maven y descarga las dependencias
   (JavaFX, JUnit).
3. Espera a que termine la indexación.
4. Asegúrate de tener un **JDK 17 o superior** seleccionado en
   `File → Project Structure → Project → SDK`.
5. Recarga el proyecto Maven (icono **Reload All Maven Projects** en el panel
   lateral Maven) para que IntelliJ resuelva todas las dependencias de JavaFX.

## Cómo ejecutar la aplicación

1. Abre la clase `com.juaniglesias.agendatareas.Launcher` (no la clase
   `AgendaApp`).
2. Pulsa el botón ▶ junto al método `main` de `Launcher`.
3. Ejecuta la configuración.

> **¿Por qué una clase `Launcher`?**
> Cuando la clase principal extiende directamente
> `javafx.application.Application`, la JVM exige que JavaFX esté disponible
> como módulo del runtime, lo que provoca el mensaje:
> *"Error: faltan los componentes de JavaFX runtime y son necesarios para
> ejecutar esta aplicación"*.
> Usar `Launcher` (una clase que **no** extiende `Application` y que llama
> a `Application.launch(AgendaApp.class, args)`) inicializa JavaFX desde el
> classpath gestionado por Maven y evita ese error por completo.

## Cómo generar el JavaDoc

```bash
mvn javadoc:javadoc
```

El resultado queda en `target/site/apidocs/index.html`.

## Usuarios y registro

La aplicación arranca con una pantalla de login.

| Usuario | Contraseña | Descripción                                    |
| ------- | ---------- | ---------------------------------------------- |
| admin   | admin      | Cuenta sembrada por defecto la primera vez     |

**`admin / admin` es la única cuenta predefinida.** El resto de personas
deben crear su propio usuario desde la pantalla de login pulsando el enlace
**"¿No tienes cuenta? Crear usuario"**. El diálogo de registro pide nombre de
usuario y contraseña (con confirmación) y muestra alertas si:

- el usuario o la contraseña están vacíos,
- el nombre de usuario ya existe,
- las dos contraseñas no coinciden,
- el nombre de usuario contiene caracteres no permitidos (`;`, saltos de línea).

Los usuarios creados se guardan automáticamente en
`~/.agenda-tareas/users.csv`, de modo que **siguen disponibles tras cerrar y
volver a abrir la aplicación**. Si se elimina ese fichero, al arrancar la
aplicación se vuelve a sembrar únicamente la cuenta `admin / admin`.

## Tareas privadas por usuario

**cada usuario sólo ve sus propias tareas**:

- Al iniciar sesión se carga el fichero global de tareas, pero la tabla y
  todos los filtros (búsqueda, estado, prioridad, fechas) muestran
  únicamente las tareas cuyo propietario coincide con el usuario
  autenticado.
- Las tareas creadas se etiquetan automáticamente con el nombre del
  usuario que las creó.
- Para no romper instalaciones previas, las tareas históricas guardadas
  sin propietario (formato antiguo de 7 columnas) se consideran tareas
  del administrador y sólo se muestran al iniciar sesión como `admin`.
- Los intentos de modificar o eliminar tareas ajenas se rechazan con un
  aviso, aunque los datos ajenos no se muestran en la tabla de cada
  usuario.

## Cierre de sesión

La cabecera de la ventana principal incluye el botón **"Cerrar sesión"**.
Al pulsarlo:

1. Se guardan en disco las tareas pendientes.
2. Se sustituye la escena actual por la pantalla de login.
3. Es posible iniciar sesión con otro usuario (o crear uno nuevo) en la
   misma ejecución.

Este flujo permite alternar entre cuentas rápidamente y verificar de
forma cómoda el aislamiento de tareas entre usuarios.

## Persistencia

Las tareas se almacenan automáticamente en el fichero:

```
~/.agenda-tareas/tasks.csv
```

Los usuarios registrados se almacenan en el mismo directorio:

```
~/.agenda-tareas/users.csv
```