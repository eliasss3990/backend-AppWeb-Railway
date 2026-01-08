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

    // Manejador específico para las posibles excepciones para InvalidFileTypeException
    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileTypeException(InvalidFileTypeException ex){

        HttpStatus status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;

        ErrorResponse response = ErrorResponse
                .builder()
                .status(status.value())
                .error("Formato de archivo no admitido.")
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

    // Manejar excepciones generales del servidor
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
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