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

        ErrorResponse response = ErrorResponse
                .builder()
                .status(status.value())
                .error("Error de Procesamiento de Excel")
                .message(ex.getMessage())
                .details(ex.getErrorDetails())
                .build();

        return new ResponseEntity<>(response, status);
    }

    // Manejador específico para las posibles excepciones para FileProcessingException
    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ErrorResponse> handleFileProcessingException(FileProcessingException ex){

        HttpStatus status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;

        ErrorResponse response = ErrorResponse
                .builder()
                .status(status.value())
                .error("Error")
                .message(ex.getMessage())
                .details(ex.getErrorDetails())
                .build();

        return new ResponseEntity<>(response, status);
    }

    // Manejador específico para las posibles excepciones para PdfCreationException
    @ExceptionHandler(PdfCreationException.class)
    public ResponseEntity<ErrorResponse> handlePdfCreationException(PdfCreationException ex){

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse response = ErrorResponse
                .builder()
                .status(status.value())
                .error("Error Interno del Servidor")
                .message(ex.getMessage())
                .details(ex.getErrorDetails())
                .build();

        return new ResponseEntity<>(response, status);
    }

    // Manejador específico para las posibles excepciones para PdfCreationException
    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ErrorResponse> handleUnprocessableEntityException(UnprocessableEntityException ex){

        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;

        ErrorResponse response = ErrorResponse
                .builder()
                .status(status.value())
                .error("Estado de recurso inválido")
                .message(ex.getMessage())
                .details(ex.getErrorDetails())
                .build();

        return new ResponseEntity<>(response, status);
    }
    // Manejador específico para las posibles excepciones para ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUnprocessableEntityException(ResourceNotFoundException ex){

        HttpStatus status = HttpStatus.NOT_FOUND;

        ErrorResponse response = ErrorResponse
                .builder()
                .status(status.value())
                .error("Recurso no encontrado")
                .message(ex.getMessage())
                .details(ex.getErrorDetails())
                .build();

        return new ResponseEntity<>(response, status);
    }

    // Manejar excepciones generales del servidor
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        System.out.println("\n\n########## ¡ATENCIÓN! EL MANEJADOR DE EXCEPCIONES SE ESTÁ EJECUTANDO. ##########\n\n");
        ex.printStackTrace(); // <-- AÑADIDO PARA DEBUGGING
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse response = ErrorResponse
                .builder()
                .status(status.value())
                .error("Error Interno del Servidor")
                .message("Ocurrió un error inesperado. Consulte los logs del servidor.")
                .details(null)
                .build();

        return new ResponseEntity<>(response, status);
    }
}