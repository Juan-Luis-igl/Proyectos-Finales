package com.juaniglesias.agendatareas.ui;

import com.juaniglesias.agendatareas.manager.TaskManager;
import com.juaniglesias.agendatareas.model.Priority;
import com.juaniglesias.agendatareas.model.Status;
import com.juaniglesias.agendatareas.model.Task;
import com.juaniglesias.agendatareas.persistence.TaskRepository;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ventana principal de la aplicación JavaFX.
 * Permite crear, eliminar, actualizar, buscar y filtrar tareas,
 * además de cambiar su estado.
 *
 * La interfaz está organizada en dos paneles: a la izquierda,
 * un formulario de edición; a la derecha, una tabla con las tareas.
 *
 * Todas las operaciones de la vista están filtradas por el usuario
 * que ha iniciado sesión. El listado, las búsquedas y los filtros
 * solo muestran las tareas asociadas al usuario actual. Las nuevas
 * tareas se asignan automáticamente a dicho usuario.
 *
 * El botón Cerrar sesión de la cabecera ejecuta el {@link Runnable}
 * proporcionado por el llamador, normalmente {@link AgendaApp},
 * para volver a mostrar la pantalla de inicio de sesión sin
 * reiniciar la aplicación.
 *
 * @author Juan Luis Iglesias Llorena
 */
public class MainView {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TaskManager taskManager;
    private final TaskRepository repository;
    private final String username;
    private final Runnable onLogout;

    private final ObservableList<Task> tableData = FXCollections.observableArrayList();
    private TableView<Task> table;

    private TextField titleField;
    private TextField descField;
    private DatePicker startPicker;
    private DatePicker endPicker;
    private ChoiceBox<Priority> priorityBox;
    private ChoiceBox<Status> statusBox;
    private Task selectedTask;

    /**
     * Crea la vista principal sin acción de logout (compatibilidad).
     *
     * @param taskManager gestor de tareas en memoria
     * @param repository  repositorio para guardar cambios
     * @param username    nombre del usuario autenticado
     */
    public MainView(TaskManager taskManager, TaskRepository repository, String username) {
        this(taskManager, repository, username, null);
    }

    /**
     * Crea la vista principal indicando un callback de cierre de sesión.
     *
     * @param taskManager gestor de tareas en memoria
     * @param repository  repositorio para guardar cambios
     * @param username    nombre del usuario autenticado
     * @param onLogout    acción a ejecutar cuando el usuario pulse
     *                    <i>Cerrar sesión</i>; puede ser {@code null}
     */
    public MainView(TaskManager taskManager, TaskRepository repository,
                    String username, Runnable onLogout) {
        this.taskManager = taskManager;
        this.repository = repository;
        this.username = username;
        this.onLogout = onLogout;
    }

    /**
     * Construye y muestra la escena principal.
     *
     * @param stage ventana en la que dibujar
     */
    public void show(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        Label header = new Label("Agenda de " + username);
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button logoutBtn = new Button("Cerrar sesión");
        logoutBtn.setOnAction(e -> onLogoutClicked());

        HBox topBar = new HBox(10, header, spacer, logoutBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        root.setTop(topBar);
        BorderPane.setMargin(topBar, new Insets(0, 0, 12, 0));

        root.setLeft(buildForm());
        root.setCenter(buildTablePane());

        refreshTable();

        Scene scene = new Scene(root, 1050, 600);
        stage.setTitle("Agenda Personal de Tareas — " + username);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Maneja la pulsación del botón <i>Cerrar sesión</i>.
     * <p>
     * Persiste cualquier cambio pendiente y delega en el callback
     * {@link #onLogout}. Si no se proporcionó callback, se muestra un
     * aviso (no debería ocurrir en uso normal).
     * </p>
     */
    private void onLogoutClicked() {
        try {
            repository.saveAll(taskManager.getAllTasks());
        } catch (Exception ex) {
            // Se notifica pero no se bloquea el logout.
            new Alert(Alert.AlertType.WARNING,
                    "No se pudo guardar antes de cerrar sesión: " + ex.getMessage())
                    .showAndWait();
        }
        if (onLogout != null) {
            onLogout.run();
        } else {
            new Alert(Alert.AlertType.INFORMATION,
                    "No hay acción de logout configurada.").showAndWait();
        }
    }

    /**
     * Construye el formulario lateral de edición.
     *
     * @return contenedor con los campos y botones
     */
    private VBox buildForm() {
        titleField = new TextField();
        titleField.setPromptText("Título");

        descField = new TextField();
        descField.setPromptText("Descripción");

        startPicker = new DatePicker(LocalDate.now());
        endPicker = new DatePicker(LocalDate.now().plusDays(1));

        priorityBox = new ChoiceBox<>(FXCollections.observableArrayList(Priority.values()));
        priorityBox.setValue(Priority.MEDIA);

        statusBox = new ChoiceBox<>(FXCollections.observableArrayList(Status.values()));
        statusBox.setValue(Status.POR_INICIAR);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);
        grid.add(new Label("Título:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Fecha inicio:"), 0, 2);
        grid.add(startPicker, 1, 2);
        grid.add(new Label("Fecha fin:"), 0, 3);
        grid.add(endPicker, 1, 3);
        grid.add(new Label("Prioridad:"), 0, 4);
        grid.add(priorityBox, 1, 4);
        grid.add(new Label("Estado:"), 0, 5);
        grid.add(statusBox, 1, 5);

        Button createBtn = new Button("Crear");
        createBtn.setOnAction(e -> onCreate());

        Button updateBtn = new Button("Actualizar");
        updateBtn.setOnAction(e -> onUpdate());

        Button deleteBtn = new Button("Eliminar");
        deleteBtn.setOnAction(e -> onDelete());

        Button clearBtn = new Button("Limpiar");
        clearBtn.setOnAction(e -> clearForm());

        HBox buttons = new HBox(6, createBtn, updateBtn, deleteBtn, clearBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(10, new Label("Tarea"), grid, buttons);
        box.setPadding(new Insets(0, 12, 0, 0));
        box.setPrefWidth(340);
        return box;
    }

    /**
     * Construye la zona central con tabla y barra de filtros.
     *
     * @return panel central
     */
    private BorderPane buildTablePane() {
        BorderPane pane = new BorderPane();

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por título o descripción");
        Button searchBtn = new Button("Buscar");
        Button allBtn = new Button("Ver todas");

        ChoiceBox<Status> statusFilter = new ChoiceBox<>(FXCollections.observableArrayList(Status.values()));
        Button filterStatusBtn = new Button("Filtrar estado");

        ChoiceBox<Priority> priorityFilter = new ChoiceBox<>(FXCollections.observableArrayList(Priority.values()));
        Button filterPriorityBtn = new Button("Filtrar prioridad");

        DatePicker fromPicker = new DatePicker();
        DatePicker toPicker = new DatePicker();
        Button filterDateBtn = new Button("Filtrar fechas");

        HBox topRow = new HBox(6, searchField, searchBtn, allBtn);
        HBox bottomRow = new HBox(6,
                new Label("Estado:"), statusFilter, filterStatusBtn,
                new Label("Prioridad:"), priorityFilter, filterPriorityBtn,
                new Label("Desde:"), fromPicker,
                new Label("Hasta:"), toPicker, filterDateBtn);
        bottomRow.setPadding(new Insets(6, 0, 6, 0));
        VBox filtersBox = new VBox(4, topRow, bottomRow);
        pane.setTop(filtersBox);

        table = new TableView<>(tableData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Task, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        idCol.setMaxWidth(60);

        TableColumn<Task, String> titleCol = new TableColumn<>("Título");
        titleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));

        TableColumn<Task, String> descCol = new TableColumn<>("Descripción");
        descCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));

        TableColumn<Task, String> startCol = new TableColumn<>("Inicio");
        startCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStartDate() == null ? "-" : c.getValue().getStartDate().format(DATE_FORMAT)));

        TableColumn<Task, String> endCol = new TableColumn<>("Fin");
        endCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEndDate() == null ? "-" : c.getValue().getEndDate().format(DATE_FORMAT)));

        TableColumn<Task, String> prioCol = new TableColumn<>("Prioridad");
        prioCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPriority().name()));

        TableColumn<Task, String> statusCol = new TableColumn<>("Estado");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));

        table.getColumns().addAll(idCol, titleCol, descCol, startCol, endCol, prioCol, statusCol);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldT, newT) -> {
            if (newT != null) {
                loadTaskInForm(newT);
            }
        });

        pane.setCenter(table);

        searchBtn.setOnAction(e -> {
            String q = searchField.getText();
            if (q == null || q.isBlank()) {
                refreshTable();
            } else {
                tableData.setAll(filterOwnedByUser(taskManager.searchTask(q)));
            }
        });
        allBtn.setOnAction(e -> {
            searchField.clear();
            refreshTable();
        });
        filterStatusBtn.setOnAction(e -> {
            Status s = statusFilter.getValue();
            if (s != null) tableData.setAll(filterOwnedByUser(taskManager.filterByStatus(s)));
        });
        filterPriorityBtn.setOnAction(e -> {
            Priority p = priorityFilter.getValue();
            if (p != null) tableData.setAll(filterOwnedByUser(taskManager.filterByPriority(p)));
        });
        filterDateBtn.setOnAction(e -> {
            LocalDate from = fromPicker.getValue();
            LocalDate to = toPicker.getValue();
            if (from == null || to == null) {
                new Alert(Alert.AlertType.WARNING, "Selecciona ambas fechas.").showAndWait();
                return;
            }
            tableData.setAll(filterOwnedByUser(taskManager.filterByDates(from, to)));
        });

        return pane;
    }

    /**
     * Carga los datos de la tarea seleccionada en el formulario.
     *
     * @param task tarea seleccionada
     */
    private void loadTaskInForm(Task task) {
        selectedTask = task;
        titleField.setText(task.getTitle());
        descField.setText(task.getDescription());
        startPicker.setValue(task.getStartDate());
        endPicker.setValue(task.getEndDate());
        priorityBox.setValue(task.getPriority());
        statusBox.setValue(task.getStatus());
    }

    /**
     * Limpia el formulario y la selección actual.
     */
    private void clearForm() {
        selectedTask = null;
        table.getSelectionModel().clearSelection();
        titleField.clear();
        descField.clear();
        startPicker.setValue(LocalDate.now());
        endPicker.setValue(LocalDate.now().plusDays(1));
        priorityBox.setValue(Priority.MEDIA);
        statusBox.setValue(Status.POR_INICIAR);
    }

    /**
     * Acción de creación de tarea. La tarea creada se etiqueta con
     * el nombre del usuario autenticado.
     */
    private void onCreate() {
        if (!validateForm()) return;
        Task t = new Task(
                titleField.getText().trim(),
                descField.getText().trim(),
                startPicker.getValue(),
                endPicker.getValue(),
                priorityBox.getValue(),
                statusBox.getValue(),
                username);
        if (taskManager.addTask(t)) {
            persistAndRefresh();
            clearForm();
        } else {
            new Alert(Alert.AlertType.ERROR, "La fecha fin no puede ser anterior a la de inicio.").showAndWait();
        }
    }

    /**
     * Acción de actualización de tarea sobre el registro seleccionado.
     * Sólo se permite modificar tareas del propio usuario; el campo
     * {@code owner} se conserva intacto en la tarea actualizada.
     */
    private void onUpdate() {
        if (selectedTask == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona una tarea de la tabla.").showAndWait();
            return;
        }
        if (!isOwnedByCurrentUser(selectedTask)) {
            new Alert(Alert.AlertType.WARNING,
                    "Sólo puedes modificar tus propias tareas.").showAndWait();
            return;
        }
        if (!validateForm()) return;
        Task updated = new Task(
                selectedTask.getId(),
                titleField.getText().trim(),
                descField.getText().trim(),
                startPicker.getValue(),
                endPicker.getValue(),
                priorityBox.getValue(),
                statusBox.getValue(),
                selectedTask.getOwner());
        if (!updated.isValidDates()) {
            new Alert(Alert.AlertType.ERROR, "La fecha fin no puede ser anterior a la de inicio.").showAndWait();
            return;
        }
        taskManager.updateTask(updated);
        persistAndRefresh();
    }

    /**
     * Acción de eliminación de tarea seleccionada.
     */
    private void onDelete() {
        if (selectedTask == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona una tarea de la tabla.").showAndWait();
            return;
        }
        if (!isOwnedByCurrentUser(selectedTask)) {
            new Alert(Alert.AlertType.WARNING,
                    "Sólo puedes eliminar tus propias tareas.").showAndWait();
            return;
        }
        taskManager.removeTask(selectedTask.getId());
        persistAndRefresh();
        clearForm();
    }

    /**
     * Persiste los cambios en disco y refresca la tabla.
     */
    private void persistAndRefresh() {
        try {
            repository.saveAll(taskManager.getAllTasks());
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR,
                    "No se pudo guardar el fichero: " + ex.getMessage()).showAndWait();
        }
        refreshTable();
    }

    /**
     * Recarga la tabla con las tareas del usuario autenticado.
     */
    private void refreshTable() {
        tableData.setAll(taskManager.getTasksByOwner(username));
    }

    /**
     * Indica si la tarea pertenece al usuario autenticado.
     *
     * @param task tarea a comprobar
     * @return {@code true} si la tarea es del usuario actual (o si es
     *         una tarea sin propietario y el usuario es {@code admin})
     */
    private boolean isOwnedByCurrentUser(Task task) {
        if (task == null) return false;
        String o = task.getOwner();
        if (username == null) return false;
        if (username.equals(o)) return true;
        return "admin".equals(username) && (o == null || o.isEmpty());
    }

    /**
     * Filtra una lista de tareas para conservar únicamente las que
     * pertenecen al usuario autenticado.
     *
     * @param tasks lista de origen
     * @return nueva lista con sólo las tareas del usuario
     */
    private List<Task> filterOwnedByUser(List<Task> tasks) {
        if (tasks == null) return new ArrayList<>();
        return tasks.stream()
                .filter(this::isOwnedByCurrentUser)
                .collect(Collectors.toList());
    }

    /**
     * Valida los campos obligatorios del formulario.
     *
     * @return {@code true} si el formulario es válido
     */
    private boolean validateForm() {
        if (titleField.getText() == null || titleField.getText().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "El título es obligatorio.").showAndWait();
            return false;
        }
        return true;
    }
}
