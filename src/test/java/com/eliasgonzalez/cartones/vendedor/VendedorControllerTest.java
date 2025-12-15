package com.eliasgonzalez.cartones.vendedor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VendedorController.class)
class VendedorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IVendedorService vendedorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listarVendedores_deberiaDevolverListaDeVendedores() throws Exception {
        // Arrange: Preparar el escenario
        // 1. Crear una lista de DTOs de vendedor que esperamos que el servicio devuelva.
        List<VendedorResponseDTO> vendedoresEsperados = Arrays.asList(
                new VendedorResponseDTO(1L, "Pepe Argento", new BigDecimal("150.00"), 10, 2, 10, 5),
                new VendedorResponseDTO(2L, "Moni Argento", new BigDecimal("200.50"), 15, 3, 15, 4)
        );

        // 2. Configurar el mock del servicio: cuando se llame a `listaVendedores`, debe devolver nuestra lista.
        when(vendedorService.listaVendedores()).thenReturn(vendedoresEsperados);

        // Act & Assert: Ejecutar la petición y verificar el resultado
        mockMvc.perform(get("/api/vendedores"))
                .andExpect(status().isOk()) // Verificar que el estado HTTP sea 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Verificar que la respuesta es JSON
                .andExpect(jsonPath("$.size()").value(vendedoresEsperados.size())) // Verificar que la lista JSON tiene 2 elementos
                .andExpect(jsonPath("$[0].id").value(1L)) // Verificar el ID del primer vendedor
                .andExpect(jsonPath("$[0].nombre").value("Pepe Argento")) // Verificar el nombre del primer vendedor
                .andExpect(jsonPath("$[1].id").value(2L)) // Verificar el ID del segundo vendedor
                .andExpect(jsonPath("$[1].nombre").value("Moni Argento")); // Verificar el nombre del segundo vendedor
    }
}
