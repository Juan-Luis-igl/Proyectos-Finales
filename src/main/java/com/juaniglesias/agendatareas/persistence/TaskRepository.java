package com.juaniglesias.agendatareas.persistence;

import com.juaniglesias.agendatareas.model.Priority;
import com.juaniglesias.agendatareas.model.Status;
import com.juaniglesias.agendatareas.model.Task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio encargado de la persistencia de tareas en un fichero
 * de texto plano con formato CSV simplificado.
 *
 * Cada línea del fichero representa una tarea con el siguiente formato:
 *
 * id;title;description;startDate;endDate;priority;status;owner
 *
 * Desde la versión 1.1 se añade una octava columna opcional con el
 * nombre del usuario propietario, para permitir el aislamiento de
 * tareas por usuario.
 *
 * Los caracteres {@code ;} y los saltos de línea presentes en los
 * campos de texto se escapan para evitar conflictos con el separador.
 *
 * Por compatibilidad con versiones anteriores, los ficheros sin la
 * columna {@code owner} también se leen correctamente. En esos casos,
 * las tareas resultantes tienen el propietario vacío.
 *
 * @author Juan Luis Iglesias Llorena
 */
public class TaskRepository {

/** Separador de columnas usado en el formato del fichero. */
    private static final String SEP = ";";

/** Formato de fechas (ISO compatible con {@link LocalDate#parse(CharSequence)}). */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

/** Ruta del fichero donde se guardan las tareas. */
    private final Path file;

/**
* Crea un nuevo repositorio que opera sobre el fichero indicado.
* @param file ruta absoluta o relativa del fichero de tareas
*/
    public TaskRepository(Path file) {
        this.file = file;
    }

/** @return ruta del fichero gestionado por este repositorio */
    public Path getFile() {
        return file;
    }

/**
* Lee todas las tareas del fichero. Si el fichero no existe se
* devuelve una lista vacía. Las líneas mal formadas se ignoran.
* @return lista de tareas cargadas desde disco
* @throws IOException si ocurre un error de lectura
*/
    public List<Task> loadAll() throws IOException {
        List<Task> result = new ArrayList<>();
        if (!Files.exists(file)) {
            return result;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                Task t = parseLine(line);
                if (t != null) {
                    result.add(t);
                }
            }
        }
        return result;
    }

/**
* Guarda en disco la totalidad de las tareas indicadas.
* Se sobrescribe el fichero anterior.
* @param tasks tareas a persistir
* @throws IOException si ocurre un error de escritura
*/
    public void saveAll(List<Task> tasks) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Task t : tasks) {
                writer.write(formatLine(t));
                writer.newLine();
            }
        }
    }

/**
* Convierte una tarea a una línea CSV escapando los campos de texto.
* @param t tarea a serializar
* @return línea lista para escribir en el fichero
*/
    private String formatLine(Task t) {
        return t.getId() + SEP
                + escape(t.getTitle()) + SEP
                + escape(t.getDescription()) + SEP
                + (t.getStartDate() == null ? "" : t.getStartDate().format(DATE_FORMAT)) + SEP
                + (t.getEndDate() == null ? "" : t.getEndDate().format(DATE_FORMAT)) + SEP
                + t.getPriority().name() + SEP
                + t.getStatus().name() + SEP
                + escape(t.getOwner());
    }

/**
* Convierte una línea CSV en una tarea.
* @param line línea leída
* @return tarea o {@code null} si la línea no se pudo interpretar
*/
    private Task parseLine(String line) {
        String[] parts = splitFields(line);
        if (parts.length < 7) {
            return null;
        }
        try {
            int id = Integer.parseInt(parts[0]);
            String title = unescape(parts[1]);
            String description = unescape(parts[2]);
            LocalDate start = parts[3].isEmpty() ? null : LocalDate.parse(parts[3], DATE_FORMAT);
            LocalDate end = parts[4].isEmpty() ? null : LocalDate.parse(parts[4], DATE_FORMAT);
            Priority priority = Priority.valueOf(parts[5]);
            Status status = Status.valueOf(parts[6]);
            // Columna 8 (owner) opcional: si falta, se asume tarea sin propietario.
            String owner = parts.length >= 8 ? unescape(parts[7]) : "";
            return new Task(id, title, description, start, end, priority, status, owner);
        } catch (RuntimeException ex) {
            return null;
        }
    }

/**
* Divide una línea en sus campos respetando los caracteres escapados.
* @param line línea original
* @return array con los siete campos sin escapar
*/
    private String[] splitFields(String line) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\\' && i + 1 < line.length()) {
                current.append(c);
                current.append(line.charAt(++i));
            } else if (c == ';') {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString());
        return parts.toArray(new String[0]);
    }

/**
* Escapa un campo de texto para que el separador no entre en conflicto.
* @param value texto original
* @return texto con los caracteres especiales escapados
*/
    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace(";", "\\;")
                    .replace("\n", "\\n")
                    .replace("\r", "");
    }

/**
* Operación inversa de {@link #escape(String)}.
* @param value texto escapado
* @return texto original
*/
    private String unescape(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);
                switch (next) {
                    case 'n' -> sb.append('\n');
                    case ';' -> sb.append(';');
                    case '\\' -> sb.append('\\');
                    default -> sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}