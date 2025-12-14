package com.eliasgonzalez.cartones.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class ExcelProcessingException extends RuntimeException {

    // Lista de errores detallados (para informar al usuario)
    private final List<String> errorDetails;

    public ExcelProcessingException(String message, List<String> errorDetails) {
        super(message);
        this.errorDetails = errorDetails;
    }

}
