package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.entity.PdfProcesos;
import com.eliasgonzalez.cartones.pdf.enums.EstadoEnum;
import com.eliasgonzalez.cartones.shared.exception.UnprocessableEntityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcesoIdServiceTest {

    private String procesoId;
    private PdfProcesos pdfProcesos;

    @BeforeEach
    void setUp() {
        procesoId = "test-proceso-id";
        pdfProcesos = PdfProcesos.builder().procesoId(procesoId).build();
    }

    // --- Tests para PendienteToVerificando ---

    @DisplayName("PendienteToVerificando: Estado inicial PENDIENTE debe cambiar a VERIFICANDO")
    @Test
    void testPendienteToVerificando_pendienteToVerificando() {
        // Arrange
        pdfProcesos.setEstado(EstadoEnum.PENDIENTE.getValue());

        // Act
        ProcesoIdService.PendienteToVerificando(procesoId, pdfProcesos);

        // Assert
        assertThat(pdfProcesos.getEstado()).isEqualTo(EstadoEnum.VERIFICANDO.getValue());
    }

    @DisplayName("PendienteToVerificando: Estado inicial VERIFICANDO debe mantenerse como VERIFICANDO")
    @Test
    void testPendienteToVerificando_verificandoToVerificando() {
        // Arrange
        pdfProcesos.setEstado(EstadoEnum.VERIFICANDO.getValue());

        // Act
        ProcesoIdService.PendienteToVerificando(procesoId, pdfProcesos);

        // Assert
        assertThat(pdfProcesos.getEstado()).isEqualTo(EstadoEnum.VERIFICANDO.getValue());
    }

    @DisplayName("PendienteToVerificando: Estado inicial COMPLETADO debe lanzar UnprocessableEntityException")
    @Test
    void testPendienteToVerificando_completadoThrowsException() {
        // Arrange
        pdfProcesos.setEstado(EstadoEnum.COMPLETADO.getValue());

        // Act & Assert
        assertThatThrownBy(() -> ProcesoIdService.PendienteToVerificando(procesoId, pdfProcesos))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessageContaining("El proceso no está en estado 'pendiente'.");
    }

    // --- Tests para VerificandoToCompletado ---

    @DisplayName("VerificandoToCompletado: Estado inicial VERIFICANDO debe cambiar a COMPLETADO")
    @Test
    void testVerificandoToCompletado_verificandoToCompletado() {
        // Arrange
        pdfProcesos.setEstado(EstadoEnum.VERIFICANDO.getValue());

        // Act
        ProcesoIdService.VerificandoToCompletado(procesoId, pdfProcesos);

        // Assert
        assertThat(pdfProcesos.getEstado()).isEqualTo(EstadoEnum.COMPLETADO.getValue());
    }

    @DisplayName("VerificandoToCompletado: Estado inicial PENDIENTE debe lanzar UnprocessableEntityException")
    @Test
    void testVerificandoToCompletado_pendienteThrowsException() {
        // Arrange
        pdfProcesos.setEstado(EstadoEnum.PENDIENTE.getValue());

        // Act & Assert
        assertThatThrownBy(() -> ProcesoIdService.VerificandoToCompletado(procesoId, pdfProcesos))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessageContaining("El proceso no está en estado 'verificando'.");
    }

    @DisplayName("VerificandoToCompletado: Estado inicial COMPLETADO debe lanzar UnprocessableEntityException")
    @Test
    void testVerificandoToCompletado_completadoThrowsException() {
        // Arrange
        pdfProcesos.setEstado(EstadoEnum.COMPLETADO.getValue());

        // Act & Assert
        assertThatThrownBy(() -> ProcesoIdService.VerificandoToCompletado(procesoId, pdfProcesos))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessageContaining("El proceso no está en estado 'verificando'.");
    }
}
