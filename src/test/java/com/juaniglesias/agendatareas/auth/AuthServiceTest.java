package com.juaniglesias.agendatareas.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias para {@link AuthService}, cubriendo el login,
 * el registro de nuevos usuarios y la persistencia en
 * {@code users.csv}.
 * @author Juan Luis Iglesias Llorena
 */
class AuthServiceTest {

    @Test
    @DisplayName("login del administrador por defecto funciona y juan/1234 no existe")
    void defaultAdminLoginWorks() {
        AuthService auth = new AuthService();
        assertTrue(auth.login("admin", "admin"));
        assertFalse(auth.login("juan", "1234"));
        assertFalse(auth.login("admin", "otra"));
    }

    @Test
    @DisplayName("register crea un nuevo usuario y permite iniciar sesión")
    void registerAllowsLogin() {
        AuthService auth = new AuthService();
        assertTrue(auth.register("alice", "secret"));
        assertTrue(auth.login("alice", "secret"));
    }

    @Test
    @DisplayName("register rechaza duplicados y valores en blanco o nulos")
    void registerRejectsDuplicatesAndBlanks() {
        AuthService auth = new AuthService();
        assertTrue(auth.register("bob", "pw"));
        assertFalse(auth.register("bob", "pw2"), "no debe permitir duplicados");
        assertFalse(auth.register("admin", "x"), "admin ya existe");
        assertFalse(auth.register("", "pw"));
        assertFalse(auth.register("   ", "pw"));
        assertFalse(auth.register("nuevo", ""));
        assertFalse(auth.register(null, "pw"));
        assertFalse(auth.register("nuevo", null));
    }

    @Test
    @DisplayName("register rechaza nombres con el separador ';'")
    void registerRejectsSeparatorInUsername() {
        AuthService auth = new AuthService();
        assertFalse(auth.register("mal;usuario", "pw"));
    }

    @Test
    @DisplayName("los usuarios registrados se persisten en el fichero")
    void registerPersistsToFile(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("users.csv");
        AuthService first = new AuthService(file);
        assertTrue(first.register("carol", "p4ss"));

        // Una segunda instancia carga el fichero y reconoce a carol.
        AuthService second = new AuthService(file);
        assertTrue(second.login("admin", "admin"));
        assertTrue(second.login("carol", "p4ss"));
        assertFalse(second.login("juan", "1234"));
    }

    @Test
    @DisplayName("se siembra admin/admin la primera vez que se crea el fichero")
    void seedsAdminOnFirstRun(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("users.csv");
        new AuthService(file);
        assertTrue(Files.exists(file), "el fichero debe crearse al sembrar el admin");
        List<String> lines = Files.readAllLines(file);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).startsWith("admin;"));
    }
}
