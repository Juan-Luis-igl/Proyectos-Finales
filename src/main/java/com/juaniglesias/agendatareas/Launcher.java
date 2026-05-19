package com.juaniglesias.agendatareas;

import com.juaniglesias.agendatareas.ui.AgendaApp;
import javafx.application.Application;

/**
 * Clase lanzadora de la aplicación JavaFX.
 *
 * Esta clase no extiende {@link javafx.application.Application}
 * para evitar errores frecuentes al ejecutar la aplicación desde
 * IntelliJ IDEA, como la falta de componentes del runtime de JavaFX.
 *
 * Al utilizar una clase lanzadora intermedia que invoca
 * {@link Application#launch(Class, String...)}, JavaFX puede
 * iniciarse correctamente desde el classpath gestionado por Maven,
 * sin necesidad de configurar manualmente {@code --module-path}
 * ni {@code --add-modules}.
 *
 * @author Juan Luis Iglesias Llorena
 */
public final class Launcher {

    /** Constructor privado: clase utilitaria, no instanciable. */
    private Launcher() {
        // No-op
    }

    /**
     * Punto de entrada de la aplicación. Lanza {@link AgendaApp} mediante
     * el ciclo de vida estándar de JavaFX.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        Application.launch(AgendaApp.class, args);
    }
}
