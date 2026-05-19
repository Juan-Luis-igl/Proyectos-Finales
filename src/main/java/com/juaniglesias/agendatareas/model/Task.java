package com.juaniglesias.agendatareas.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Modelo de dominio que representa una tarea de la agenda personal.
 *
 * Cada tarea contiene un identificador numérico único, un título,
 * una descripción, un rango de fechas, una {@link Priority prioridad},
 * un {@link Status estado} y, desde la versión 1.1, el nombre del
 * usuario propietario almacenado en el campo {@code owner}.
 *
 * La clase es mutable: sus atributos pueden modificarse mediante
 * los setters, excepto el identificador, que se asigna al crear la
 * tarea y permanece inmutable.
 *
 * @author Juan Luis Iglesias Llorena
 */
public class Task {

/** Contador estático usado para asignar identificadores únicos. */
    private static int nextId = 1;

/** Formato de fecha empleado en la representación textual. */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final int id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Priority priority;
    private Status status;
/**
* Nombre del usuario propietario de la tarea. Se utiliza para
* que cada usuario sólo vea sus propias tareas. Cadena vacía
* significa <i>tarea sin dueño asignado</i> (tareas heredadas de
* versiones anteriores, que se consideran del administrador).
*/
    private String owner;

/**
* Construye una nueva tarea asignándole automáticamente un identificador.
*
* @param title       título corto de la tarea
* @param description descripción detallada
* @param startDate   fecha de inicio
* @param endDate     fecha de fin
* @param priority    prioridad de la tarea
* @param status      estado inicial
*/
    public Task(String title, String description, LocalDate startDate,
                LocalDate endDate, Priority priority, Status status) {
        this(title, description, startDate, endDate, priority, status, "");
    }

/**
* Construye una nueva tarea asignándole automáticamente un
* identificador y un usuario propietario.
*
* @param title       título corto de la tarea
* @param description descripción detallada
* @param startDate   fecha de inicio
* @param endDate     fecha de fin
* @param priority    prioridad de la tarea
* @param status      estado inicial
* @param owner       nombre de usuario propietario (puede ser cadena vacía)
*/
    public Task(String title, String description, LocalDate startDate,
                LocalDate endDate, Priority priority, Status status, String owner) {
        this.id = nextId++;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.priority = priority;
        this.status = status;
        this.owner = owner == null ? "" : owner;
    }

/**
* Constructor secundario que permite indicar el identificador de la tarea
* (utilizado al cargar tareas desde fichero para conservar los IDs).
*
* @param id          identificador a usar
* @param title       título corto de la tarea
* @param description descripción detallada
* @param startDate   fecha de inicio
* @param endDate     fecha de fin
* @param priority    prioridad de la tarea
* @param status      estado inicial
*/
    public Task(int id, String title, String description, LocalDate startDate,
                LocalDate endDate, Priority priority, Status status) {
        this(id, title, description, startDate, endDate, priority, status, "");
    }

/**
* Constructor secundario que permite indicar el identificador y el
* usuario propietario. Utilizado típicamente al cargar tareas desde
* fichero (formato CSV con columna {@code owner}).
*
* @param id          identificador a usar
* @param title       título corto de la tarea
* @param description descripción detallada
* @param startDate   fecha de inicio
* @param endDate     fecha de fin
* @param priority    prioridad de la tarea
* @param status      estado inicial
* @param owner       nombre de usuario propietario (puede ser cadena vacía)
*/
    public Task(int id, String title, String description, LocalDate startDate,
                LocalDate endDate, Priority priority, Status status, String owner) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.priority = priority;
        this.status = status;
        this.owner = owner == null ? "" : owner;
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

/**
* Reinicia el contador interno de identificadores. Utilizado
* exclusivamente por pruebas y por la carga de datos persistidos.
*
* @param value siguiente valor a usar como ID
*/
    public static void resetIdCounter(int value) {
        nextId = value;
    }

/** @return identificador único de la tarea */
    public int getId() {
        return id;
    }

/** @return título de la tarea */
    public String getTitle() {
        return title;
    }

/**
* Establece el título de la tarea.
*
* @param title nuevo título
*/
    public void setTitle(String title) {
        this.title = title;
    }

/** @return descripción detallada de la tarea */
    public String getDescription() {
        return description;
    }

/**
* Establece la descripción de la tarea.
*
* @param description nueva descripción
*/
    public void setDescription(String description) {
        this.description = description;
    }

/** @return fecha en la que comienza la tarea */
    public LocalDate getStartDate() {
        return startDate;
    }

/**
* Establece la fecha de inicio.
*
* @param startDate nueva fecha de inicio
*/
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

/** @return fecha límite de la tarea */
    public LocalDate getEndDate() {
        return endDate;
    }

/**
* Establece la fecha de fin.
*
* @param endDate nueva fecha de fin
*/
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

/** @return prioridad asignada a la tarea */
    public Priority getPriority() {
        return priority;
    }

/**
* Establece la prioridad de la tarea.
*
* @param priority nueva prioridad
*/
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

/** @return estado actual de la tarea */
    public Status getStatus() {
        return status;
    }

/**
* Establece el estado de la tarea.
*
* @param status nuevo estado
*/
    public void setStatus(Status status) {
        this.status = status;
    }

/**
* Devuelve el nombre del usuario propietario de la tarea. Una
* cadena vacía indica una tarea sin propietario asignado.
*
* @return nombre del usuario propietario o cadena vacía
*/
    public String getOwner() {
        return owner;
    }

/**
* Establece el usuario propietario de la tarea.
*
* @param owner nombre de usuario propietario (puede ser cadena vacía)
*/
    public void setOwner(String owner) {
        this.owner = owner == null ? "" : owner;
    }

/**
* Indica si el rango de fechas es coherente, es decir, que la fecha de
* fin no sea anterior a la fecha de inicio. Se permite que alguna de
* las dos fechas sea {@code null}.
*
* @return {@code true} si las fechas son válidas; {@code false} en caso contrario
*/
    public boolean isValidDates() {
        return endDate == null || startDate == null || !endDate.isBefore(startDate);
    }

/**
* Genera una representación textual legible de la tarea.
*
* @return cadena formateada con todos los campos relevantes
*/
    @Override
    public String toString() {
        return String.format("ID: %d | %s | %s | %s → %s | %s | %s",
                id, title, description,
                startDate == null ? "-" : startDate.format(DATE_FORMAT),
                endDate == null ? "-" : endDate.format(DATE_FORMAT),
                priority, status);
    }

/**
 * Dos tareas se consideran iguales si comparten identificador.
 *
 * @param o objeto a comparar
 * @return {@code true} si los identificadores coinciden
 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task task)) return false;
        return id == task.id;
    }

/**
* Calcula el hash a partir del identificador.
*
* @return código hash basado en el ID
*/
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}