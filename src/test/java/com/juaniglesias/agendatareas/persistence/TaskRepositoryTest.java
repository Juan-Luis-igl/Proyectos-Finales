package com.juaniglesias.agendatareas.persistence;

import com.juaniglesias.agendatareas.model.Priority;
import com.juaniglesias.agendatareas.model.Status;
import com.juaniglesias.agendatareas.model.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del repositorio de persistencia {@link TaskRepository}.
 * @author Juan Luis Iglesias Llorena
 */
class TaskRepositoryTest {

    @Test
    void saveAndLoadRoundTrip(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("tasks.csv");
        TaskRepository repo = new TaskRepository(file);

        Task t1 = new Task(1, "Estudiar", "Repasar; capítulo 3",
                LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 2),
                Priority.ALTA, Status.EN_PROGRESO);
        Task t2 = new Task(2, "Comprar pan", "Panadería de la esquina",
                LocalDate.of(2025, 5, 3), LocalDate.of(2025, 5, 3),
                Priority.BAJA, Status.POR_INICIAR);

        repo.saveAll(List.of(t1, t2));
        List<Task> loaded = repo.loadAll();

        assertEquals(2, loaded.size());
        assertEquals("Estudiar", loaded.get(0).getTitle());
        // El punto y coma escapado se recupera correctamente
        assertTrue(loaded.get(0).getDescription().contains(";"));
        assertEquals(Priority.BAJA, loaded.get(1).getPriority());
    }

    @Test
    void loadAllReturnsEmptyWhenFileMissing(@TempDir Path tempDir) throws Exception {
        TaskRepository repo = new TaskRepository(tempDir.resolve("nope.csv"));
        assertTrue(repo.loadAll().isEmpty());
    }

    @Test
    void ownerColumnRoundTripsAndIsolatesUsers(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("tasks.csv");
        TaskRepository repo = new TaskRepository(file);

        Task adminTask = new Task(10, "Reunión", "con equipo",
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 1),
                Priority.MEDIA, Status.POR_INICIAR, "admin");
        Task juanTask = new Task(11, "Estudiar Maven", "captíulo 5",
                LocalDate.of(2025, 6, 2), LocalDate.of(2025, 6, 3),
                Priority.ALTA, Status.EN_PROGRESO, "juan");

        repo.saveAll(List.of(adminTask, juanTask));
        List<Task> loaded = repo.loadAll();

        assertEquals(2, loaded.size());
        assertEquals("admin", loaded.get(0).getOwner());
        assertEquals("juan", loaded.get(1).getOwner());

        long admins = loaded.stream().filter(t -> "admin".equals(t.getOwner())).count();
        long juans = loaded.stream().filter(t -> "juan".equals(t.getOwner())).count();
        assertEquals(1, admins);
        assertEquals(1, juans);
    }

    @Test
    void legacyFilesWithoutOwnerColumnLoadGracefully(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("legacy.csv");
        // Formato antiguo de 7 columnas (sin la octava de owner).
        String legacy = "1;Tarea legada;descripción antigua;2024-01-01;2024-01-02;MEDIA;POR_INICIAR\n";
        Files.writeString(file, legacy, StandardCharsets.UTF_8);

        TaskRepository repo = new TaskRepository(file);
        List<Task> loaded = repo.loadAll();
        assertEquals(1, loaded.size());
        Task t = loaded.get(0);
        assertEquals("Tarea legada", t.getTitle());
        assertEquals("", t.getOwner(), "el owner de tareas legadas debe ser cadena vacía");
    }
}
