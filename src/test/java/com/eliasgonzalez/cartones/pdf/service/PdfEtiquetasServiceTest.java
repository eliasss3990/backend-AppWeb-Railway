package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.dto.EtiquetaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PdfEtiquetasServiceTest {

    @InjectMocks
    private PdfEtiquetasService pdfEtiquetasService;

    private List<EtiquetaDTO> etiquetas;
    private LocalDate fechaSenete;
    private LocalDate fechaTelebingo;

    @BeforeEach
    void setUp() {
        fechaSenete = LocalDate.of(2026, 1, 1);
        fechaTelebingo = LocalDate.of(2026, 1, 2);

        etiquetas = new ArrayList<>();
        etiquetas.add(EtiquetaDTO.builder()
                .numeroVendedor(1)
                .nombre("Vendedor Uno")
                .saldo("150.000")
                .seneteCartones("10")
                .seneteRangos(Arrays.asList("001-050", "051-100"))
                .resultadoSenete("5")
                .telebingoCartones("20")
                .telebingoRangos(Arrays.asList("101-150", "151-200"))
                .resultadoTelebingo("8")
                .build());
        etiquetas.add(EtiquetaDTO.builder()
                .numeroVendedor(2)
                .nombre("Vendedor Dos")
                .saldo("250.000")
                .seneteCartones("5")
                .seneteRangos(Collections.singletonList("201-250"))
                .resultadoSenete("2")
                .telebingoCartones("10")
                .telebingoRangos(Collections.singletonList("251-300"))
                .resultadoTelebingo("4")
                .build());
        etiquetas.add(EtiquetaDTO.builder()
                .numeroVendedor(3)
                .nombre("Vendedor Tres")
                .saldo("50.000")
                .seneteCartones("2")
                .seneteRangos(null) // Test null ranges
                .resultadoSenete("1")
                .telebingoCartones("3")
                .telebingoRangos(null) // Test null ranges
                .resultadoTelebingo("0")
                .build());
    }

    @DisplayName("Test generarEtiquetas - Happy Path con múltiples etiquetas")
    @Test
    void testGenerarEtiquetas_happyPathMultiplesEtiquetas() {
        // Act
        byte[] pdfBytes = pdfEtiquetasService.generarEtiquetas(etiquetas, fechaSenete, fechaTelebingo);

        // Assert
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes).isNotEmpty();
        // Further assertions could involve parsing PDF or checking content,
        // but that's out of scope for a basic unit test without specific PDF parsing libraries.
    }

    @DisplayName("Test generarEtiquetas - Lista de etiquetas vacía")
    @Test
    void testGenerarEtiquetas_listaVacia() {
        // Arrange
        List<EtiquetaDTO> emptyEtiquetas = Collections.emptyList();

        // Act
        byte[] pdfBytes = pdfEtiquetasService.generarEtiquetas(emptyEtiquetas, fechaSenete, fechaTelebingo);

        // Assert
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes).isNotEmpty(); // An empty PDF should still be a valid, non-empty byte array
    }

    @DisplayName("Test generarEtiquetas - Una sola etiqueta")
    @Test
    void testGenerarEtiquetas_unaSolaEtiqueta() {
        // Arrange
        List<EtiquetaDTO> singleEtiqueta = Collections.singletonList(etiquetas.get(0));

        // Act
        byte[] pdfBytes = pdfEtiquetasService.generarEtiquetas(singleEtiqueta, fechaSenete, fechaTelebingo);

        // Assert
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes).isNotEmpty();
    }
}
