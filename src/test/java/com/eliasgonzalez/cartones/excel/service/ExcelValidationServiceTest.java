package com.eliasgonzalez.cartones.excel.service;

import com.eliasgonzalez.cartones.vendedor.dto.VendedorExcelDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ExcelValidationServiceTest {

    private final ExcelValidationService validationService = new ExcelValidationService();

    @DisplayName("Validación de DTO válido: sin errores esperados")
    @Test
    void testValidate_validDto_noErrors() {
        // Arrange
        VendedorExcelDTO dto = VendedorExcelDTO.builder()
                .filaActual(1)
                .nombre("Vendedor Prueba")
                .deudaStr("100.50")
                .build();

        // Act
        List<String> errors = validationService.validate(dto);

        // Assert
        assertThat(errors).isEmpty();
    }

    @DisplayName("Validación de DTO con nombre nulo: se espera error")
    @Test
    void testValidate_nullNombre_expectError() {
        // Arrange
        VendedorExcelDTO dto = VendedorExcelDTO.builder()
                .filaActual(1)
                .nombre(null)
                .deudaStr("100.50")
                .build();

        // Act
        List<String> errors = validationService.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("Fila 1: El campo NOMBRE del vendedor no puede estar vacío.");
    }

    @DisplayName("Validación de DTO con nombre vacío: se espera error")
    @Test
    void testValidate_blankNombre_expectError() {
        // Arrange
        VendedorExcelDTO dto = VendedorExcelDTO.builder()
                .filaActual(1)
                .nombre("   ")
                .deudaStr("100.50")
                .build();

        // Act
        List<String> errors = validationService.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("Fila 1: El campo NOMBRE del vendedor no puede estar vacío.");
    }

    @DisplayName("Validación de DTO con deudaStr inválida (no numérica): se espera error")
    @Test
    void testValidate_invalidDeudaStr_expectError() {
        // Arrange
        VendedorExcelDTO dto = VendedorExcelDTO.builder()
                .filaActual(1)
                .nombre("Vendedor Prueba")
                .deudaStr("abc")
                .build();

        // Act
        List<String> errors = validationService.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("Fila 1: El campo SALDO ('abc') no es un número válido.");
    }

    @DisplayName("Validación de DTO con deudaStr nula: no se esperan errores")
    @Test
    void testValidate_nullDeudaStr_noErrors() {
        // Arrange
        VendedorExcelDTO dto = VendedorExcelDTO.builder()
                .filaActual(1)
                .nombre("Vendedor Prueba")
                .deudaStr(null)
                .build();

        // Act
        List<String> errors = validationService.validate(dto);

        // Assert
        assertThat(errors).isEmpty();
    }

    @DisplayName("Validación de DTO con deudaStr vacía: no se esperan errores")
    @Test
    void testValidate_blankDeudaStr_noErrors() {
        // Arrange
        VendedorExcelDTO dto = VendedorExcelDTO.builder()
                .filaActual(1)
                .nombre("Vendedor Prueba")
                .deudaStr("   ")
                .build();

        // Act
        List<String> errors = validationService.validate(dto);

        // Assert
        assertThat(errors).isEmpty();
    }

    @DisplayName("Validación de DTO con deudaStr válida: no se esperan errores")
    @Test
    void testValidate_validDeudaStr_noErrors() {
        // Arrange
        VendedorExcelDTO dto = VendedorExcelDTO.builder()
                .filaActual(1)
                .nombre("Vendedor Prueba")
                .deudaStr("200")
                .build();

        // Act
        List<String> errors = validationService.validate(dto);

        // Assert
        assertThat(errors).isEmpty();
    }
}
