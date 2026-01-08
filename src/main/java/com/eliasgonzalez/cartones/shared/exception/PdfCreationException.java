package com.eliasgonzalez.cartones.shared.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class PdfCreationException extends RuntimeException {

    // Lista de errores detallados para el usuario
    private final List<String> errorDetails;

    public PdfCreationException(String message, List<String> errorDetails) {
        super(message);
        this.errorDetails = errorDetails;
    }
}
