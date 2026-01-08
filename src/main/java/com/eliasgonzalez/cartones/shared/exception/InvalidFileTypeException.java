package com.eliasgonzalez.cartones.shared.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class InvalidFileTypeException extends RuntimeException {

    // Lista de errores detallados para mostrar al usuario
    private final List<String> errorDetails;

    public InvalidFileTypeException(String message, List<String> errorDetails) {
        super(message);
        this.errorDetails = errorDetails;
    }
}
