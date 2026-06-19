package com.transportista.guias.exception;

/**
 * Excepción personalizada para recursos no encontrados.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
