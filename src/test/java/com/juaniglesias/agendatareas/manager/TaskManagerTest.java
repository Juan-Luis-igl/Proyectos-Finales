package com.juaniglesias.agendatareas.manager;

import com.juaniglesias.agendatareas.model.Priority;
import com.juaniglesias.agendatareas.model.Status;
import com.juaniglesias.agendatareas.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias para la clase {@link TaskManager}.
 * @author Juan Luis Iglesias Llorena
 */
class TaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new TaskManager();
        Task.resetIdCounter(1);
    }

    private Task sample(String title, Priority p, Status s, LocalDate start, LocalDate end) {
        return new Task(title, title + " desc", start, end, p, s);
    }

    @Test
    @DisplayName("addTask añade tareas con fechas válidas")
    void addTaskValid() {
        Task t = sample("Estudiar", Priority.ALTA, Status.POR_INICIAR,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5));
        assertTrue(manager.addTask(t));
        assertEquals(1, manager.size());
    }

    @Test
    @DisplayName("addTask rechaza fechas inválidas")
    void addTaskInvalidDates() {
        Task t = sample("X", Priority.BAJA, Status.POR_INICIAR,
                LocalDate.of(2025, 2, 10), LocalDate.of(2025, 2, 1));
        assertFalse(manager.addTask(t));
        assertEquals(0, manager.size());
    }

    @Test
    @DisplayName("removeTask elimina la tarea cuyo ID coincide")
    void removeTaskExisting() {
        Task t = sample("Algo", Priority.MEDIA, Status.POR_INICIAR,
                LocalDate.now(), LocalDate.now().plusDays(1));
        manager.addTask(t);
        assertTrue(manager.removeTask(t.getId()));
        assertEquals(0, manager.size());
    }

    @Test
    @DisplayName("removeTask devuelve false si la tarea no existe")
    void removeTaskMissing() {
        assertFalse(manager.removeTask(999));
    }

    @Test
    @DisplayName("updateTask reemplaza la tarea con mismo ID")
    void updateTaskWorks() {
        Task t = sample("Original", Priority.BAJA, Status.POR_INICIAR,
                LocalDate.now(), LocalDate.now().plusDays(2));
        manager.addTask(t);
        Task replacement = new Task(t.getId(), "Modificada", "nueva", t.getStartDate(),
                t.getEndDate(), Priority.ALTA, Status.EN_PROGRESO);
        assertTrue(manager.updateTask(replacement));
        Task found = manager.findById(t.getId());
        assertNotNull(found);
        assertEquals("Modificada", found.getTitle());
        assertEquals(Priority.ALTA, found.getPriority());
    }

    @Test
    @DisplayName("searchTask busca por título y descripción sin diferenciar mayúsculas")
    void searchTaskIgnoreCase() {
        manager.addTask(sample("Comprar pan", Priority.BAJA, Status.POR_INICIAR,
                LocalDate.now(), LocalDate.now().plusDays(1)));
        manager.addTask(sample("Hacer ejercicio", Priority.ALTA, Status.POR_INICIAR,
                LocalDate.now(), LocalDate.now().plusDays(1)));
        List<Task> result = manager.searchTask("PAN");
        assertEquals(1, result.size());
        assertEquals("Comprar pan", result.get(0).getTitle());
    }

    @Test
    @DisplayName("filterByStatus filtra correctamente por estado")
    void filterByStatusWorks() {
        Task t1 = sample("A", Priority.BAJA, Status.POR_INICIAR,
                LocalDate.now(), LocalDate.now().plusDays(1));
        Task t2 = sample("B", Priority.MEDIA, Status.COMPLETADA,
                LocalDate.now(), LocalDate.now().plusDays(1));
        manager.addTask(t1);
        manager.addTask(t2);
        List<Task> done = manager.filterByStatus(Status.COMPLETADA);
        assertEquals(1, done.size());
        assertEquals("B", done.get(0).getTitle());
    }

    @Test
    @DisplayName("filterByPriority filtra correctamente por prioridad")
    void filterByPriorityWorks() {
        manager.addTask(sample("Alta1", Priority.ALTA, Status.POR_INICIAR,
                LocalDate.now(), LocalDate.now().plusDays(1)));
        manager.addTask(sample("Alta2", Priority.ALTA, Status.POR_INICIAR,
                LocalDate.now(), LocalDate.now().plusDays(1)));
        manager.addTask(sample("Baja", Priority.BAJA, Status.POR_INICIAR,
                LocalDate.now(), LocalDate.now().plusDays(1)));
        assertEquals(2, manager.filterByPriority(Priority.ALTA).size());
        assertEquals(1, manager.filterByPriority(Priority.BAJA).size());
    }

    @Test
    @DisplayName("filterByDates devuelve únicamente tareas dentro del rango")
    void filterByDatesRange() {
        manager.addTask(sample("Antes", Priority.BAJA, Status.POR_INICIAR,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2)));
        manager.addTask(sample("Dentro", Priority.BAJA, Status.POR_INICIAR,
                LocalDate.of(2025, 5, 10), LocalDate.of(2025, 5, 11)));
        manager.addTask(sample("Después", Priority.BAJA, Status.POR_INICIAR,
                LocalDate.of(2025, 12, 31), LocalDate.of(2026, 1, 1)));
        List<Task> result = manager.filterByDates(
                LocalDate.of(2025, 4, 1), LocalDate.of(2025, 6, 1));
        assertEquals(1, result.size());
        assertEquals("Dentro", result.get(0).getTitle());
    }

    @Test
    @DisplayName("changeStatus modifica el estado de la tarea con ID dado")
    void changeStatusWorks() {
        Task t = sample("X", Priority.MEDIA, Status.POR_INICIAR,
                LocalDate.now(), LocalDate.now().plusDays(1));
        manager.addTask(t);
        assertTrue(manager.changeStatus(t.getId(), Status.COMPLETADA));
        assertEquals(Status.COMPLETADA, manager.findById(t.getId()).getStatus());
    }

    @Test
    @DisplayName("changeStatus devuelve false cuando la tarea no existe")
    void changeStatusMissing() {
        assertFalse(manager.changeStatus(123, Status.COMPLETADA));
    }

    @Test
    @DisplayName("getAllTasks devuelve copia ordenada por ID")
    void getAllTasksSorted() {
        Task a = sample("A", Priority.BAJA, Status.POR_INICIAR,
                LocalDate.now(), LocalDate.now().plusDays(1));
        Task b = sample("B", Priority.BAJA, Status.POR_INICIAR,
                LocalDate.now(), LocalDate.now().plusDays(1));
        manager.addTask(b);
        manager.addTask(a);
        List<Task> all = manager.getAllTasks();
        assertEquals(2, all.size());
        assertTrue(all.get(0).getId() < all.get(1).getId());
    }

    @Test
    @DisplayName("replaceAll sustituye por completo el contenido")
    void replaceAllReplaces() {
        manager.addTask(sample("X", Priority.MEDIA, Status.POR_INICIAR,
                LocalDate.now(), LocalDate.now().plusDays(1)));
        Task fresh = sample("Y", Priority.ALTA, Status.COMPLETADA,
                LocalDate.now(), LocalDate.now().plusDays(1));
        manager.replaceAll(List.of(fresh));
        assertEquals(1, manager.size());
        assertNull(manager.findById(1) == fresh ? null : manager.findById(fresh.getId()) == null ? "missing" : null);
        assertEquals("Y", manager.findById(fresh.getId()).getTitle());
    }

    @Test
    @DisplayName("getTasksByOwner sólo devuelve las tareas del usuario indicado")
    void getTasksByOwnerIsolatesUsers() {
        Task adminTask = new Task("A", "de admin",
                LocalDate.now(), LocalDate.now().plusDays(1),
                Priority.ALTA, Status.POR_INICIAR, "admin");
        Task juanTask1 = new Task("J1", "de juan",
                LocalDate.now(), LocalDate.now().plusDays(1),
                Priority.MEDIA, Status.POR_INICIAR, "juan");
        Task juanTask2 = new Task("J2", "de juan 2",
                LocalDate.now(), LocalDate.now().plusDays(1),
                Priority.BAJA, Status.EN_PROGRESO, "juan");

        manager.addTask(adminTask);
        manager.addTask(juanTask1);
        manager.addTask(juanTask2);

        List<Task> adminTasks = manager.getTasksByOwner("admin");
        List<Task> juanTasks = manager.getTasksByOwner("juan");
        List<Task> ghostTasks = manager.getTasksByOwner("fantasma");

        assertEquals(1, adminTasks.size(), "admin no debe ver tareas de juan");
        assertEquals("A", adminTasks.get(0).getTitle());
        assertEquals(2, juanTasks.size(), "juan debe ver únicamente sus tareas");
        assertTrue(juanTasks.stream().allMatch(t -> "juan".equals(t.getOwner())));
        assertTrue(ghostTasks.isEmpty(), "un usuario sin tareas no debe ver nada");
        assertEquals(3, manager.size(), "el almacén interno mantiene todas las tareas");
    }

    @Test
    @DisplayName("getTasksByOwner('admin') hereda tareas legadas sin propietario")
    void getTasksByOwnerAdminIncludesLegacy() {
        // Tarea sin propietario (formato antiguo).
        Task legacy = new Task("vieja", "sin owner",
                LocalDate.now(), LocalDate.now().plusDays(1),
                Priority.MEDIA, Status.POR_INICIAR);
        Task ofJuan = new Task("nueva", "de juan",
                LocalDate.now(), LocalDate.now().plusDays(1),
                Priority.BAJA, Status.POR_INICIAR, "juan");
        manager.addTask(legacy);
        manager.addTask(ofJuan);

        List<Task> admin = manager.getTasksByOwner("admin");
        assertEquals(1, admin.size());
        assertEquals("vieja", admin.get(0).getTitle());

        // Juan no debe ver la tarea legada.
        List<Task> juan = manager.getTasksByOwner("juan");
        assertEquals(1, juan.size());
        assertEquals("nueva", juan.get(0).getTitle());
    }

    @Test
    @DisplayName("countByOwner cuenta correctamente las tareas por usuario")
    void countByOwnerWorks() {
        manager.addTask(new Task("a", "", LocalDate.now(), LocalDate.now().plusDays(1),
                Priority.BAJA, Status.POR_INICIAR, "juan"));
        manager.addTask(new Task("b", "", LocalDate.now(), LocalDate.now().plusDays(1),
                Priority.BAJA, Status.POR_INICIAR, "juan"));
        manager.addTask(new Task("c", "", LocalDate.now(), LocalDate.now().plusDays(1),
                Priority.BAJA, Status.POR_INICIAR, "admin"));
        assertEquals(2, manager.countByOwner("juan"));
        assertEquals(1, manager.countByOwner("admin"));
        assertEquals(0, manager.countByOwner("otro"));
        assertEquals(0, manager.countByOwner(null));
    }
}
