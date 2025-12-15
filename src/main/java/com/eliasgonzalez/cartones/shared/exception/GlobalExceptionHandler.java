package com.eliasgonzalez.cartones.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Manejador específico para las posibles excepciones de Excel
    @ExceptionHandler(ExcelProcessingException.class)
    public ResponseEntity<ErrorResponse> handleExcelProcessingException(ExcelProcessingException ex) {

        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;

        ErrorResponse response = new ErrorResponse(
                status.value(),
                "Error de Procesamiento de Excel",
                ex.getMessage(),
                ex.getErrorDetails()
        );

        return new ResponseEntity<>(response, status);
    }

    // Manejar excepciones generales del servidor
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse response = new ErrorResponse(
                status.value(),
                "Error Interno del Servidor",
                "Ocurrió un error inesperado. Consulte los logs del servidor.",
                null
        );

        return new ResponseEntity<>(response, status);
    }
}