package com.eliasgonzalez.cartones.shared.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    // Lista de errores detallados para mostrar al usuario
    private final List<String> errorDetails;

    public ResourceNotFoundException(String message, List<String> errorDetails) {
        super(message);
        this.errorDetails = errorDetails;
    }
}
