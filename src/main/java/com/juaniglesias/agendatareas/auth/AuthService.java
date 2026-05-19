package com.juaniglesias.agendatareas.auth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Servicio de autenticación y registro de usuarios.
 *
 * Gestiona el inicio de sesión y el alta de nuevos usuarios
 * desde la interfaz de la aplicación.
 *
 * Los usuarios se almacenan en un fichero de texto plano,
 * lo que permite conservar las cuentas entre ejecuciones.
 *
 * Incluye una cuenta de administración por defecto y permite
 * crear nuevos usuarios desde la pantalla de inicio de sesión
 * o mediante {@link #register(String, String)}.
 *
 * @author Juan Luis Iglesias Llorena
 */
public class AuthService {

/** Separador usado en el fichero {@code users.csv}. */
    private static final String SEP = ";";

/** Usuario administrador por defecto. */
    public static final String DEFAULT_ADMIN_USERNAME = "admin";

/** Contraseña del administrador por defecto. */
    public static final String DEFAULT_ADMIN_PASSWORD = "admin";

/** Mapa con los usuarios válidos y sus contraseñas en claro. */
    private final Map<String, String> users = new LinkedHashMap<>();

/** Fichero opcional donde se persisten los usuarios registrados. */
    private final Path file;

/**
* Crea un servicio en memoria, sin persistencia, sembrando
* únicamente la cuenta de administración por defecto. Útil
* sobre todo para pruebas unitarias.
*/
    public AuthService() {
        this.file = null;
        seedDefaults();
    }

/**
* Crea un servicio que persiste los usuarios en el fichero
* indicado. Si el fichero ya existe se cargan los usuarios
* almacenados; en caso contrario se siembra la cuenta
* <b>admin / admin</b> y se guarda inmediatamente.
*
* @param file ruta del fichero de usuarios (puede no existir aún)
*/
    public AuthService(Path file) {
        this.file = file;
        if (file != null && Files.exists(file)) {
            try {
                loadFromFile();
            } catch (IOException ex) {
                throw new UncheckedIOException("No se pudo leer el fichero de usuarios", ex);
            }
        }
        // Asegura que el administrador por defecto exista siempre.
        if (!users.containsKey(DEFAULT_ADMIN_USERNAME)) {
            users.put(DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
            persistSilently();
        }
    }

/**
* Comprueba si las credenciales proporcionadas son correctas.
*
* @param username nombre de usuario
* @param password contraseña en claro
* @return {@code true} si el par usuario/contraseña es válido
*/
    public boolean login(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        String stored = users.get(username);
        return stored != null && stored.equals(password);
    }

/**
* Registra un nuevo usuario y lo persiste si el servicio fue
* creado con un fichero asociado.
*
* <p>Validaciones:</p>
* <ul>
*   <li>El nombre de usuario y la contraseña no pueden ser
*       {@code null} ni estar en blanco.</li>
*   <li>El nombre de usuario no puede contener el carácter
*       separador {@code ;} ni saltos de línea.</li>
*   <li>No se permiten nombres de usuario duplicados.</li>
* </ul>
*
* @param username nombre de usuario único
* @param password contraseña asociada
* @return {@code true} si se creó correctamente; {@code false}
*         si los datos no son válidos o el usuario ya existía
*/
    public boolean register(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        String u = username.trim();
        if (u.isEmpty() || password.isEmpty()) {
            return false;
        }
        if (u.contains(SEP) || u.contains("\n") || u.contains("\r")) {
            return false;
        }
        if (users.containsKey(u)) {
            return false;
        }
        users.put(u, password);
        persistSilently();
        return true;
    }

/**
* Indica si existe un usuario con el nombre indicado.
*
* @param username nombre a comprobar
* @return {@code true} si el usuario ya está registrado
*/
    public boolean userExists(String username) {
        return username != null && users.containsKey(username);
    }

/**
* Devuelve el número de usuarios registrados (incluido el
* administrador por defecto).
*
* @return cantidad de usuarios actualmente conocidos
*/
    public int userCount() {
        return users.size();
    }

/**
* Carga el contenido del fichero de usuarios en memoria.
*
* @throws IOException si ocurre un error de lectura
*/
    private void loadFromFile() throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                int idx = line.indexOf(SEP);
                if (idx <= 0 || idx == line.length() - 1) {
                    continue;
                }
                String user = line.substring(0, idx);
                String pass = line.substring(idx + 1);
                users.put(user, pass);
            }
        }
    }

/**
* Siembra la cuenta de administración por defecto en memoria
* (no persiste).
*/
    private void seedDefaults() {
        users.put(DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
    }

/**
* Persiste el mapa de usuarios en disco si hay fichero
* configurado. Los errores de E/S se descartan para no
* interrumpir el flujo de la UI; se reportan en
* {@code System.err}.
*/
    private void persistSilently() {
        if (file == null) {
            return;
        }
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                for (Map.Entry<String, String> entry : users.entrySet()) {
                    writer.write(entry.getKey());
                    writer.write(SEP);
                    writer.write(entry.getValue());
                    writer.newLine();
                }
            }
        } catch (IOException ex) {
            System.err.println("No se pudo guardar el fichero de usuarios: " + ex.getMessage());
        }
    }
}