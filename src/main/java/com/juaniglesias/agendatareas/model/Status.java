package com.juaniglesias.agendatareas.model;

/**
 * Enumeración que representa el estado de una tarea.
 *
 * Una tarea siempre se encuentra en uno de los tres estados definidos.
 * El flujo habitual es {@link #POR_INICIAR} → {@link #EN_PROGRESO}
 * → {@link #COMPLETADA}, aunque la aplicación permite cambiar
 * libremente entre estados.
 *
 * @author Juan Luis Iglesias Llorena
 */
public enum Status {
/** La tarea ha sido creada pero todavía no se ha empezado. */
    POR_INICIAR,
/** La tarea está siendo realizada actualmente. */
    EN_PROGRESO,
/** La tarea se ha terminado por completo. */
    COMPLETADA
}