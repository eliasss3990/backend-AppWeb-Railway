package com.eliasgonzalez.cartones.vendedor.controller;

import com.eliasgonzalez.cartones.vendedor.dto.VendedorResponseDTO;
import com.eliasgonzalez.cartones.vendedor.interfaces.IVendedorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile; // Added
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never; // Added

import static org.mockito.Mockito.verifyNoInteractions; // Added

import static org.mockito.ArgumentMatchers.any; // Added
import static org.mockito.ArgumentMatchers.anyString; // Added
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content; // Added


@WebMvcTest(VendedorController.class)
class VendedorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IVendedorService vendedorService;

    @DisplayName("GET /api/vendedores/{procesoId} - Happy Path")
    @Test
    void testListarVendedoresValidos_happyPath() throws Exception {
        // Arrange
        String procesoId = "proceso-123";
        List<VendedorResponseDTO> mockResponse = Arrays.asList(
                VendedorResponseDTO.builder().id(1L).nombre("Vendedor Valido 1").build(),
                VendedorResponseDTO.builder().id(2L).nombre("Vendedor Valido 2").build()
        );
        when(vendedorService.listarVendedoresValidos(procesoId)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/vendedores/{procesoId}", procesoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Vendedor Valido 1"))
                .andExpect(jsonPath("$[1].nombre").value("Vendedor Valido 2"));

        verify(vendedorService, times(1)).listarVendedoresValidos(procesoId);
    }

    @DisplayName("GET /api/vendedores/{procesoId} - Excepción de servicio")
    @Test
    void testListarVendedoresValidos_serviceException() throws Exception {
        // Arrange
        String procesoId = "proceso-123";
        when(vendedorService.listarVendedoresValidos(procesoId)).thenThrow(new RuntimeException("Error simulado en el servicio"));

        // Act & Assert
        mockMvc.perform(get("/api/vendedores/{procesoId}", procesoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error Interno del Servidor"));

        verify(vendedorService, times(1)).listarVendedoresValidos(procesoId);
    }


    @DisplayName("POST /api/vendedores/carga - Happy Path")
    @Test
    void testCargarVendedoresDesdeExcel_happyPath() throws Exception {
        // Arrange
        MockMultipartFile mockFile =
                new MockMultipartFile(
                        "file",
                        "test.xlsx",
                        "application/vnd.ms-excel",
                        "some excel content".getBytes());
        String procesoIdCreado = "nuevo-proceso-id";

        when(vendedorService.iniciarProceso()).thenReturn(procesoIdCreado);
        doNothing().when(vendedorService).procesarExcel(any(), anyString());

        // Act & Assert
        mockMvc.perform(multipart("/api/vendedores/carga").file(mockFile))
                .andExpect(status().isOk())
                .andExpect(content().string(procesoIdCreado));

        verify(vendedorService, times(1)).iniciarProceso();
        verify(vendedorService, times(1)).procesarExcel(mockFile, procesoIdCreado);
    }

    @DisplayName("POST /api/vendedores/carga - Archivo MultipartFile faltante")
    @Test
    void testCargarVendedoresDesdeExcel_missingFile() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/vendedores/carga")
                .contentType(MediaType.MULTIPART_FORM_DATA)) // No .file() call
                .andExpect(status().isBadRequest());

        verifyNoInteractions(vendedorService);
    }

    @DisplayName("POST /api/vendedores/carga - Excepción en iniciarProceso")
    @Test
    void testCargarVendedoresDesdeExcel_iniciarProcesoException() throws Exception {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.ms-excel",
                "some excel content".getBytes()
        );
        when(vendedorService.iniciarProceso()).thenThrow(new RuntimeException("Error simulado al iniciar proceso"));

        // Act & Assert
        mockMvc.perform(multipart("/api/vendedores/carga")
                .file(mockFile))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error Interno del Servidor"));

        verify(vendedorService, times(1)).iniciarProceso();
        verify(vendedorService, never()).procesarExcel(any(), anyString()); // procesarExcel should not be called
    }

    @DisplayName("POST /api/vendedores/carga - Excepción en procesarExcel")
    @Test
    void testCargarVendedoresDesdeExcel_procesarExcelException() throws Exception {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.ms-excel",
                "some excel content".getBytes()
        );
        String procesoIdCreado = "nuevo-proceso-id";

        when(vendedorService.iniciarProceso()).thenReturn(procesoIdCreado);
        doThrow(new RuntimeException("Error simulado al procesar Excel")).when(vendedorService).procesarExcel(any(), anyString());

        // Act & Assert
        mockMvc.perform(multipart("/api/vendedores/carga")
                .file(mockFile))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error Interno del Servidor"));

        verify(vendedorService, times(1)).iniciarProceso();
        verify(vendedorService, times(1)).procesarExcel(mockFile, procesoIdCreado);
    }
}
