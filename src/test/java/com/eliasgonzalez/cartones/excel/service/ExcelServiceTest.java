package com.eliasgonzalez.cartones.excel.service;

import com.eliasgonzalez.cartones.excel.enums.ExcelEnum;
import com.eliasgonzalez.cartones.shared.exception.ExcelProcessingException;
import com.eliasgonzalez.cartones.vendedor.dto.VendedorExcelDTO;
import com.eliasgonzalez.cartones.vendedor.entity.Vendedor;
import com.eliasgonzalez.cartones.vendedor.interfaces.VendedorRepository;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExcelServiceTest {

    @Mock
    private VendedorRepository vendedorRepo;

    @Mock
    private ExcelValidationService validationService;

    @InjectMocks
    private ExcelService excelService;

    private MockMultipartFile createMockExcelFile(boolean valid) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(true);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(ExcelEnum.HOJA_SISTEMA_ETIQUETAS.getValue());
            Row header = sheet.createRow(0);
            if (valid) {
                header.createCell(0).setCellValue(ExcelEnum.VENDEDOR.getValue());
                header.createCell(1).setCellValue(ExcelEnum.SALDO.getValue());
                header.createCell(2).setCellValue(ExcelEnum.CANT_SENETE.getValue());
                header.createCell(3).setCellValue(ExcelEnum.RESULT_SENETE.getValue());
                header.createCell(4).setCellValue(ExcelEnum.CANT_TELEBINGO.getValue());
                header.createCell(5).setCellValue(ExcelEnum.RESULT_TELEBINGO.getValue());

                Row dataRow = sheet.createRow(1);
                dataRow.createCell(0).setCellValue("Vendedor 1");
                dataRow.createCell(1).setCellValue("100.50");
                dataRow.createCell(2).setCellValue(10);
                dataRow.createCell(3).setCellValue(5);
                dataRow.createCell(4).setCellValue(20);
                dataRow.createCell(5).setCellValue(8);
            } else {
                header.createCell(0).setCellValue("Columna Invalida");
            }

            workbook.write(out);
            InputStream in = new ByteArrayInputStream(out.toByteArray());
            return new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", in);
        }
    }

    @DisplayName("Test leerExcel con archivo válido - Happy Path")
    @Test
    void testLeerExcel_conArchivoValido() throws IOException {
        // Arrange
        MockMultipartFile mockFile = createMockExcelFile(true);
        when(validationService.validate(any(VendedorExcelDTO.class))).thenReturn(Collections.emptyList());

        // Act
        excelService.leerExcel(mockFile, "test-proceso-id");

        // Assert
        ArgumentCaptor<List<Vendedor>> captor = ArgumentCaptor.forClass(List.class);
        verify(vendedorRepo, times(1)).saveAll(captor.capture());
        
        List<Vendedor> savedVendedores = captor.getValue();
        assertThat(savedVendedores).hasSize(1);
        assertThat(savedVendedores.get(0).getNombre()).isEqualTo("Vendedor 1");
        assertThat(savedVendedores.get(0).getDeuda()).isEqualByComparingTo("100.50");
    }

    @DisplayName("Test leerExcel cuando la hoja no existe")
    @Test
    void testLeerExcel_hojaNoEncontrada() throws IOException {
        // Arrange
        try (Workbook workbook = WorkbookFactory.create(true);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.createSheet("OTRA HOJA"); // Wrong sheet name
            workbook.write(out);
            InputStream in = new ByteArrayInputStream(out.toByteArray());
            MockMultipartFile mockFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", in);
            
            // Act & Assert
            assertThatThrownBy(() -> excelService.leerExcel(mockFile, "test-proceso-id"))
                    .isInstanceOf(ExcelProcessingException.class)
                    .hasMessageContaining("La hoja ('" + ExcelEnum.HOJA_SISTEMA_ETIQUETAS.getValue() + "') no fue encontrada.");
        }
    }

    @DisplayName("Test leerExcel cuando faltan encabezados")
    @Test
    void testLeerExcel_faltanEncabezados() throws IOException {
        // Arrange
        MockMultipartFile mockFile = createMockExcelFile(false);

        // Act & Assert
        assertThatThrownBy(() -> excelService.leerExcel(mockFile, "test-proceso-id"))
                .isInstanceOf(ExcelProcessingException.class)
                .hasMessageContaining("Faltan encabezados requeridos");
    }

    @DisplayName("Test leerExcel con errores de validación en una fila")
    @Test
    void testLeerExcel_conErroresDeValidacion() throws IOException {
        // Arrange
        MockMultipartFile mockFile = createMockExcelFile(true);
        when(validationService.validate(any(VendedorExcelDTO.class))).thenReturn(List.of("Error de validación de prueba"));

        // Act & Assert
        assertThatThrownBy(() -> excelService.leerExcel(mockFile, "test-proceso-id"))
                .isInstanceOf(ExcelProcessingException.class)
                .hasMessageContaining("El archivo Excel contiene errores. No se ha guardado ningún dato.");

        verify(vendedorRepo, never()).saveAll(any());
    }
}
