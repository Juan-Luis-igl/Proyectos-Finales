package com.juaniglesias.agendatareas.ui;

import com.juaniglesias.agendatareas.auth.AuthService;
import com.juaniglesias.agendatareas.manager.TaskManager;
import com.juaniglesias.agendatareas.persistence.TaskRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Ventana de inicio de sesión.
 * Solicita usuario y contraseña y, si las credenciales son válidas,
 * abre la ventana principal {@link MainView}.
 *
 * También ofrece la opción "Crear usuario", que muestra un diálogo
 * de registro y, si se completa correctamente, guarda el nuevo usuario
 * en {@code users.csv} mediante {@link AuthService}.
 *
 * Esta vista también se reutiliza al cerrar sesión desde
 * {@link MainView}, permitiendo volver al formulario de login
 * e iniciar sesión con otra cuenta sin reiniciar la aplicación.
 *
 * @author Juan Luis Iglesias Llorena
 */
public class LoginView {

    private final AuthService authService;
    private final TaskManager taskManager;
    private final TaskRepository repository;

    /**
     * Crea la vista de login.
     *
     * @param authService servicio de autenticación
     * @param taskManager gestor compartido de tareas
     * @param repository  repositorio de persistencia
     */
    public LoginView(AuthService authService, TaskManager taskManager, TaskRepository repository) {
        this.authService = authService;
        this.taskManager = taskManager;
        this.repository = repository;
    }

    /**
     * Muestra la ventana de login.
     *
     * @param stage ventana principal de la aplicación
     */
    public void show(Stage stage) {
        Label title = new Label("Agenda Personal de Tareas");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label subtitle = new Label("Inicia sesión o crea tu usuario");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        Label userLabel = new Label("Usuario:");
        TextField userField = new TextField();
        userField.setPromptText("nombre de usuario");

        Label passLabel = new Label("Contraseña:");
        PasswordField passField = new PasswordField();
        passField.setPromptText("contraseña");

        Button loginBtn = new Button("Entrar");
        loginBtn.setDefaultButton(true);
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        Hyperlink registerLink = new Hyperlink("¿No tienes cuenta? Crear usuario");
        registerLink.setStyle("-fx-font-size: 12px;");

        Label hint = new Label("Cuenta por defecto: admin / admin");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.add(userLabel, 0, 0);
        form.add(userField, 1, 0);
        form.add(passLabel, 0, 1);
        form.add(passField, 1, 1);

        HBox linkBox = new HBox(registerLink);
        linkBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(12, title, subtitle, form, loginBtn, linkBox, hint);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.CENTER);

        loginBtn.setOnAction(e -> {
            String u = userField.getText() == null ? "" : userField.getText().trim();
            String p = passField.getText() == null ? "" : passField.getText();
            if (u.isEmpty() || p.isEmpty()) {
                new Alert(Alert.AlertType.WARNING,
                        "Introduce usuario y contraseña.").showAndWait();
                return;
            }
            if (authService.login(u, p)) {
                // Al cerrar sesión desde MainView se invocará esta lambda,
                // que vuelve a abrir esta misma vista de login en la
                // misma ventana, sin reiniciar la aplicación.
                Runnable logoutCallback = () -> new LoginView(authService, taskManager, repository).show(stage);
                MainView mainView = new MainView(taskManager, repository, u, logoutCallback);
                mainView.show(stage);
            } else {
                new Alert(Alert.AlertType.ERROR, "Usuario o contraseña incorrectos.").showAndWait();
                passField.clear();
            }
        });

        registerLink.setOnAction(e -> showRegisterDialog(stage));

        Scene scene = new Scene(root, 420, 320);
        stage.setTitle("Login — Agenda de Tareas");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Muestra el diálogo de registro de un nuevo usuario.
     * <p>
     * Realiza validaciones en cliente y, si todo es correcto, llama
     * a {@link AuthService#register(String, String)}. El nuevo
     * usuario se persiste automáticamente.
     * </p>
     *
     * @param owner ventana propietaria del diálogo
     */
    private void showRegisterDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Crear usuario");

        Label header = new Label("Crear nuevo usuario");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField userField = new TextField();
        userField.setPromptText("nombre de usuario");

        PasswordField passField = new PasswordField();
        passField.setPromptText("contraseña");

        PasswordField pass2Field = new PasswordField();
        pass2Field.setPromptText("repite la contraseña");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(new Label("Usuario:"), 0, 0);
        grid.add(userField, 1, 0);
        grid.add(new Label("Contraseña:"), 0, 1);
        grid.add(passField, 1, 1);
        grid.add(new Label("Repetir:"), 0, 2);
        grid.add(pass2Field, 1, 2);

        Button createBtn = new Button("Crear");
        createBtn.setDefaultButton(true);
        Button cancelBtn = new Button("Cancelar");
        cancelBtn.setCancelButton(true);

        HBox buttons = new HBox(8, createBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, header, grid, buttons);
        root.setPadding(new Insets(20));

        cancelBtn.setOnAction(e -> dialog.close());

        createBtn.setOnAction(e -> {
            String u = userField.getText() == null ? "" : userField.getText().trim();
            String p = passField.getText() == null ? "" : passField.getText();
            String p2 = pass2Field.getText() == null ? "" : pass2Field.getText();

            if (u.isEmpty() || p.isEmpty()) {
                new Alert(Alert.AlertType.WARNING,
                        "El usuario y la contraseña son obligatorios.").showAndWait();
                return;
            }
            if (!p.equals(p2)) {
                new Alert(Alert.AlertType.WARNING,
                        "Las contraseñas no coinciden.").showAndWait();
                return;
            }
            if (authService.userExists(u)) {
                new Alert(Alert.AlertType.ERROR,
                        "Ya existe un usuario con ese nombre.").showAndWait();
                return;
            }
            if (!authService.register(u, p)) {
                new Alert(Alert.AlertType.ERROR,
                        "No se pudo crear el usuario. Revisa que no contenga ';' ni saltos de línea.")
                        .showAndWait();
                return;
            }
            new Alert(Alert.AlertType.INFORMATION,
                    "Usuario creado correctamente. Ya puedes iniciar sesión.").showAndWait();
            dialog.close();
        });

        Scene scene = new Scene(root, 360, 220);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
