package com.eliasgonzalez.cartones.dto.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * DTO utilizado para estandarizar la respuesta de error enviada al cliente.
 * Mantiene consistencia y evita exponer detalles internos del servidor.
 */

@AllArgsConstructor
@Getter
public class ErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final List<String> details; // Lista de errores específicos.

}