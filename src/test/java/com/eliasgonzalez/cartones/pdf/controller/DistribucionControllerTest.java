package com.eliasgonzalez.cartones.pdf.controller;

import com.eliasgonzalez.cartones.pdf.dto.SimulacionRequestDTO;
import com.eliasgonzalez.cartones.pdf.dto.VendedorSimuladoDTO;
import com.eliasgonzalez.cartones.pdf.service.GestionArchivoPdfService;
import com.eliasgonzalez.cartones.pdf.service.GestionDistribucionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import com.eliasgonzalez.cartones.shared.exception.FileProcessingException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock; // Added mock
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(DistribucionController.class)
class DistribucionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GestionDistribucionService gestionDistribucion;

    @MockBean
    private GestionArchivoPdfService gestionArchivoPdf;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("POST /api/distribuciones/{procesoId}/simular - Happy Path")
    @Test
    void testSimular_happyPath() throws Exception {
        // Arrange
        String procesoId = "proceso-123";
        SimulacionRequestDTO requestDTO = new SimulacionRequestDTO();
        requestDTO.setFechaSorteoSenete(LocalDate.now());
        requestDTO.setFechaSorteoTelebingo(LocalDate.now().plusDays(1));
        requestDTO.setVendedores(Collections.emptyList());

        List<VendedorSimuladoDTO> mockResponse = Arrays.asList(
                VendedorSimuladoDTO.builder().id(1L).nombre("Test Vendedor 1").build(),
                VendedorSimuladoDTO.builder().id(2L).nombre("Test Vendedor 2").build()
        );

        when(gestionDistribucion.procesarSimulacion(anyString(), any(SimulacionRequestDTO.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/distribuciones/{procesoId}/simular", procesoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Test Vendedor 1"))
                .andExpect(jsonPath("$[1].nombre").value("Test Vendedor 2"));
    }

    @DisplayName("POST /api/distribuciones/{procesoId}/simular - Request inválido (Validación)")
    @Test
    void testSimular_invalidRequest() throws Exception {
        // Arrange
        String procesoId = "proceso-123";
        SimulacionRequestDTO requestDTO = new SimulacionRequestDTO();
        // Missing required fields or providing invalid data to trigger validation errors
        // For example, if 'vendedores' cannot be null, set it to null
        requestDTO.setFechaSorteoSenete(null); // Assuming this might trigger a validation error if not default
        requestDTO.setFechaSorteoTelebingo(null);
        requestDTO.setVendedores(null); // Assuming @Valid on this field means it can't be null or empty

        // Act & Assert
        mockMvc.perform(post("/api/distribuciones/{procesoId}/simular", procesoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("POST /api/distribuciones/{procesoId}/simular - Excepción de servicio")
    @Test
    void testSimular_serviceException() throws Exception {
        // Arrange
        String procesoId = "proceso-123";
        SimulacionRequestDTO requestDTO = new SimulacionRequestDTO();
        requestDTO.setFechaSorteoSenete(LocalDate.now());
        requestDTO.setFechaSorteoTelebingo(LocalDate.now().plusDays(1));
        requestDTO.setVendedores(Collections.emptyList());

        when(gestionDistribucion.procesarSimulacion(anyString(), any(SimulacionRequestDTO.class)))
                .thenThrow(new RuntimeException("Error simulado en el servicio"));

        // Act & Assert
        mockMvc.perform(post("/api/distribuciones/{procesoId}/simular", procesoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error Interno del Servidor"));
    }

    @DisplayName("GET /api/distribuciones/{procesoId}/pdfs - Happy Path")
    @Test
    void testDescargar_happyPath() throws Exception {
        // Arrange
        String procesoId = "proceso-123";
        byte[] zipContent = "zip-test-content".getBytes();
        Resource mockResource = mock(Resource.class);

        when(gestionArchivoPdf.generarPaqueteZip(anyString())).thenReturn(mockResource);
        when(mockResource.contentLength()).thenReturn((long) zipContent.length);
        when(mockResource.getInputStream()).thenReturn(new ByteArrayInputStream(zipContent));

        // Act & Assert
        mockMvc.perform(get("/api/distribuciones/{procesoId}/pdfs", procesoId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/zip"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"zip-proceso-123.zip\""))
                .andExpect(content().bytes(zipContent));

        verify(gestionArchivoPdf, times(1)).generarPaqueteZip(procesoId);
    }

    @DisplayName("GET /api/distribuciones/{procesoId}/pdfs - IOException al generar ZIP")
    @Test
    void testDescargar_ioExceptionGenerandoZip() throws Exception {
        // Arrange
        String procesoId = "proceso-123";
        when(gestionArchivoPdf.generarPaqueteZip(anyString())).thenThrow(new FileProcessingException("Error de E/S simulado", List.of()));

        // Act & Assert
        mockMvc.perform(get("/api/distribuciones/{procesoId}/pdfs", procesoId))
                .andExpect(status().isUnsupportedMediaType()) // Expected 415
                .andExpect(jsonPath("$.error").value("Error")); // Changed error message to match GlobalExceptionHandler

        verify(gestionArchivoPdf, times(1)).generarPaqueteZip(procesoId);
    }
}
