package com.eliasgonzalez.cartones.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Manejador específico para las posibles excepciones de Excel
    @ExceptionHandler(ExcelProcessingException.class)
    public ResponseEntity<ErrorResponse> handleExcelProcessingException(ExcelProcessingException ex) {

        log.error("SE LANZÓ 'handleExcelProcessingException': {}", ex.getMessage());

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

        log.error("SE LANZÓ 'handleFileProcessingException': {}", ex.getMessage());

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

        log.error("SE LANZÓ 'handlePdfCreationException': {}", ex.getMessage());

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

    // Manejador específico para las posibles excepciones para UnprocessableEntityException
    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ErrorResponse> handleUnprocessableEntityException(UnprocessableEntityException ex){

        log.error("SE LANZÓ 'handleUnprocessableEntityException': {}", ex.getMessage());

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
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex){

        log.error("SE LANZÓ 'handleResourceNotFoundException': {}", ex.getMessage());

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
    
    // Manejador para errores de validación de argumentos (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        log.error("SE LANZÓ 'handleMethodArgumentNotValidException': {}", ex.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(java.util.stream.Collectors.toList());
        
        ErrorResponse response = ErrorResponse
                .builder()
                .status(status.value())
                .error("Error de Validación")
                .message("Uno o más campos de la solicitud no son válidos.")
                .details(errors)
                .build();
        
        return new ResponseEntity<>(response, status);
    }
        
    // Manejador para cuando falta un archivo MultipartFile
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {

        log.error("SE LANZÓ 'handleMissingServletRequestPartException': {}", ex.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        ErrorResponse response = ErrorResponse.builder()
                .status(status.value())
                .error("Archivo Requerido Faltante")
                .message("Se esperaba un archivo, pero no se encontró en la solicitud.")
                .details(List.of(ex.getMessage()))
                .build();
        
        return new ResponseEntity<>(response, status);
    }
        
    // Manejar excepciones generales del servidor
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {

        log.error("SE LANZÓ 'handleAllExceptions': {}", ex.getMessage());

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse response = ErrorResponse
                .builder()
                .status(status.value())
                .error("Error Interno del Servidor")
                .message("Ocurrió un error inesperado.")
                .details(List.of())
                .build();
        return new ResponseEntity<>(response, status);
    }
}