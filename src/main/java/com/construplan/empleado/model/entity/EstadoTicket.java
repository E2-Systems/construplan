package com.construplan.empleado.model.entity;

/**
 * Representa los estados del ciclo de vida de una inconformidad.
 * Se mapea directamente con los valores ENUM definidos en la base de datos
 * para mantener la integridad referencial y de estado.
 */
public enum EstadoTicket {
    ABIERTO,
    EN_REVISION,
    RESUELTO;
    
    public boolean isAbierto()    { return this == ABIERTO; }
    public boolean isEnRevision() { return this == EN_REVISION; }
    public boolean isResuelto()   { return this == RESUELTO; }
}
