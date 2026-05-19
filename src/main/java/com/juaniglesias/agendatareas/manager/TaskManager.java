package com.juaniglesias.agendatareas.manager;

import com.juaniglesias.agendatareas.model.Priority;
import com.juaniglesias.agendatareas.model.Status;
import com.juaniglesias.agendatareas.model.Task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestor central de tareas.
 *
 * Concentra la lógica de negocio relacionada con la creación,
 * eliminación, modificación, búsqueda y filtrado de tareas
 * dentro de la agenda.
 *
 * Internamente mantiene una colección {@link ArrayList} y
 * devuelve copias de los datos para preservar la encapsulación.
 *
 * @author Juan Luis Iglesias Llorena
 */
public class TaskManager {

/** Lista interna de tareas gestionadas. */
    private final List<Task> tasks = new ArrayList<>();

/**
* Añade una nueva tarea siempre y cuando el rango de fechas sea válido.
*
* @param task tarea a añadir
* @return {@code true} si la tarea se añadió; {@code false} si las fechas son inválidas
*/
    public boolean addTask(Task task) {
        if (task != null && task.isValidDates()) {
            tasks.add(task);
            return true;
        }
        return false;
    }

/**
* Elimina la tarea cuyo identificador coincida con el indicado.
*
* @param id identificador a buscar
* @return {@code true} si se eliminó alguna tarea
*/
    public boolean removeTask(int id) {
        return tasks.removeIf(task -> task.getId() == id);
    }

/**
* Sustituye la tarea con identificador igual al de la pasada como
* parámetro. La búsqueda se hace por ID; el resto de campos se
* actualizan completamente.
*
* @param updatedTask tarea con los datos nuevos
* @return {@code true} si se localizó y actualizó la tarea
*/
    public boolean updateTask(Task updatedTask) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == updatedTask.getId()) {
                tasks.set(i, updatedTask);
                return true;
            }
        }
        return false;
    }

/**
* Busca tareas cuyo título o descripción contengan la palabra clave
* indicada, ignorando mayúsculas y minúsculas.
*
* @param keyword texto a buscar
* @return lista (posiblemente vacía) con las tareas coincidentes
*/
    public List<Task> searchTask(String keyword) {
        if (keyword == null) {
            return new ArrayList<>();
        }
        String needle = keyword.toLowerCase();
        return tasks.stream()
                .filter(t -> t.getTitle().toLowerCase().contains(needle)
                          || t.getDescription().toLowerCase().contains(needle))
                .collect(Collectors.toList());
    }

/**
* Devuelve las tareas que coinciden con un estado concreto.
*
* @param status estado a filtrar
* @return lista de tareas en dicho estado
*/
    public List<Task> filterByStatus(Status status) {
        return tasks.stream()
                .filter(task -> task.getStatus() == status)
                .collect(Collectors.toList());
    }

/**
* Devuelve las tareas que coinciden con la prioridad indicada.
*
* @param priority prioridad a filtrar
* @return lista de tareas con esa prioridad
*/
    public List<Task> filterByPriority(Priority priority) {
        return tasks.stream()
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toList());
    }

/**
* Devuelve las tareas cuya fecha de inicio se encuentre dentro del
* rango {@code [from, to]} (ambos extremos incluidos).
*
* @param from fecha mínima de inicio
* @param to   fecha máxima de inicio
* @return lista de tareas dentro del rango
*/
    public List<Task> filterByDates(LocalDate from, LocalDate to) {
        return tasks.stream()
                .filter(task -> task.getStartDate() != null
                        && !task.getStartDate().isBefore(from)
                        && !task.getStartDate().isAfter(to))
                .collect(Collectors.toList());
    }

/**
* Cambia el estado de la tarea con el ID indicado.
*
* @param id        identificador de la tarea
* @param newStatus nuevo estado
* @return {@code true} si se localizó y actualizó la tarea
*/
    public boolean changeStatus(int id, Status newStatus) {
        return tasks.stream()
                .filter(task -> task.getId() == id)
                .findFirst()
                .map(task -> {
                    task.setStatus(newStatus);
                    return true;
                }).orElse(false);
    }

/**
* Devuelve una copia defensiva de todas las tareas, ordenadas por
* identificador ascendente.
*
* @return copia ordenada de la lista interna
*/
    public List<Task> getAllTasks() {
        List<Task> copy = new ArrayList<>(tasks);
        copy.sort(Comparator.comparingInt(Task::getId));
        return copy;
    }

/**
* Devuelve, ordenadas por identificador ascendente, únicamente las
* tareas cuyo propietario coincide con {@code owner}.
* <p>
* Por compatibilidad hacia atrás, si el usuario solicitado es el
* administrador por defecto ({@code "admin"}), también se incluyen
* las tareas históricas que no tengan propietario asignado
* (campo {@code owner} vacío), provenientes de versiones anteriores
* de la aplicación.
* </p>
*
* @param owner nombre de usuario propietario; {@code null} devuelve lista vacía
* @return lista ordenada de tareas pertenecientes a ese usuario
*/
    public List<Task> getTasksByOwner(String owner) {
        if (owner == null) {
            return new ArrayList<>();
        }
        boolean isAdmin = "admin".equals(owner);
        return tasks.stream()
                .filter(t -> owner.equals(t.getOwner())
                          || (isAdmin && (t.getOwner() == null || t.getOwner().isEmpty())))
                .sorted(Comparator.comparingInt(Task::getId))
                .collect(Collectors.toList());
    }

/**
* Cuenta cuántas tareas pertenecen al usuario indicado.
* <p>
* Aplica las mismas reglas que {@link #getTasksByOwner(String)}: las
* tareas sin propietario se contabilizan al administrador.
* </p>
*
* @param owner nombre de usuario
* @return número de tareas asociadas
*/
    public int countByOwner(String owner) {
        return getTasksByOwner(owner).size();
    }

/**
* Reemplaza por completo el contenido del gestor con la lista
* proporcionada. Útil al cargar datos desde fichero.
*
* @param newTasks tareas nuevas que sustituyen a las actuales
*/
    public void replaceAll(List<Task> newTasks) {
        tasks.clear();
        if (newTasks != null) {
            tasks.addAll(newTasks);
        }
    }

/**
* Localiza una tarea por su identificador.
*
* @param id identificador a buscar
* @return la tarea encontrada o {@code null} si no existe
*/
    public Task findById(int id) {
        return tasks.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);
    }

/**
* Indica el número de tareas almacenadas.
*
* @return número total de tareas gestionadas
*/
    public int size() {
        return tasks.size();
    }
}