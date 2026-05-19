package com.juaniglesias.agendatareas.model;

/**
 * Enumeración que representa la prioridad de una tarea.
 *
 * Los valores están ordenados de menor a mayor importancia, lo que
 * permite clasificarlas o filtrarlas según su urgencia.
 *
 * @author Juan Luis Iglesias Llorena
 */
public enum Priority {
/** Prioridad baja: la tarea puede esperar. */
    BAJA,
/** Prioridad media: la tarea es relevante pero no urgente. */
    MEDIA,
/** Prioridad alta: la tarea es urgente y debe atenderse cuanto antes. */
    ALTA
}