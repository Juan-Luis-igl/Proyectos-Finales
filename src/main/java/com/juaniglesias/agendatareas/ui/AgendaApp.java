package com.juaniglesias.agendatareas.ui;

import com.juaniglesias.agendatareas.auth.AuthService;
import com.juaniglesias.agendatareas.manager.TaskManager;
import com.juaniglesias.agendatareas.persistence.TaskRepository;
import javafx.application.Application;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Punto de entrada de la aplicación JavaFX Agenda Personal de Tareas.
 *
 * Inicializa los componentes principales, como el servicio de
 * autenticación con persistencia local de usuarios, el repositorio
 * de tareas y el gestor en memoria. Además, carga los datos
 * persistidos y muestra la ventana de inicio de sesión.
 *
 * Las tareas almacenadas pertenecen a todos los usuarios, pero el
 * filtrado por usuario autenticado se realiza en {@link MainView}
 * mediante el campo {@code owner} de cada tarea.
 *
 * La interfaz de {@link MainView} incluye un botón de cierre de
 * sesión que permite volver a {@link LoginView} en la misma ventana
 * sin reiniciar la aplicación.
 *
 * @author Juan Luis Iglesias Llorena
 */
public class AgendaApp extends Application {

    /** Nombre del fichero donde se persisten las tareas. */
    public static final String DATA_FILE_NAME = "tasks.csv";

    /** Nombre del fichero donde se persisten los usuarios registrados. */
    public static final String USERS_FILE_NAME = "users.csv";

    /** Gestor de tareas compartido por la aplicación. */
    private final TaskManager taskManager = new TaskManager();

    /** Servicio de autenticación de usuarios (se inicializa en {@link #init()}). */
    private AuthService authService;

    /** Repositorio en disco usado para cargar/guardar tareas. */
    private TaskRepository repository;

    /**
     * Método principal estándar.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Inicializa la aplicación. JavaFX llama a este método antes de
     * crear la ventana principal.
     */
    @Override
    public void init() {
        Path baseDir = Paths.get(System.getProperty("user.home"), ".agenda-tareas");
        Path tasksFile = baseDir.resolve(DATA_FILE_NAME);
        Path usersFile = baseDir.resolve(USERS_FILE_NAME);

        authService = new AuthService(usersFile);
        repository = new TaskRepository(tasksFile);
        try {
            taskManager.replaceAll(repository.loadAll());
        } catch (Exception ex) {
            System.err.println("No se pudieron cargar las tareas: " + ex.getMessage());
        }
    }

    /**
     * Construye y muestra la primera ventana (login).
     *
     * @param primaryStage ventana principal proporcionada por JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        LoginView loginView = new LoginView(authService, taskManager, repository);
        loginView.show(primaryStage);
    }

    /**
     * Persiste el estado actual al cerrar la aplicación.
     */
    @Override
    public void stop() {
        try {
            if (repository != null) {
                repository.saveAll(taskManager.getAllTasks());
            }
        } catch (Exception ex) {
            System.err.println("No se pudieron guardar las tareas: " + ex.getMessage());
        }
    }
}
