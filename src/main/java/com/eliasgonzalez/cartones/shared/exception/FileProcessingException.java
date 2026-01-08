package com.eliasgonzalez.cartones.shared.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class FileProcessingException extends RuntimeException {

    // Lista de errores detallados para mostrar al usuario
    private final List<String> errorDetails;

    public FileProcessingException(String message, List<String> errorDetails) {
        super(message);
        this.errorDetails = errorDetails;
    }
}
