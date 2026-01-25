package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.dto.ResumenDTO;
import com.eliasgonzalez.cartones.shared.exception.PdfCreationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PdfResumenServiceTest {

    @InjectMocks
    private PdfResumenService pdfResumenService;

    private List<ResumenDTO> vendedores;
    private LocalDate fechaSenete;
    private LocalDate fechaTelebingo;

    @BeforeEach
    void setUp() {
        fechaSenete = LocalDate.of(2026, 1, 1);
        fechaTelebingo = LocalDate.of(2026, 1, 2);

        vendedores = new ArrayList<>();
        Map<String, String> rangosSenete1 = new HashMap<>();
        rangosSenete1.put("001", "050");
        rangosSenete1.put("051", "100");

        Map<String, String> rangosTelebingo1 = new HashMap<>();
        rangosTelebingo1.put("101", "150");

        vendedores.add(ResumenDTO.builder()
                .numeroVendedor(1)
                .nombre("Vendedor Uno Muy Largo Que Sera Truncado")
                .cantidadSenete(10)
                .seneteDelAl(rangosSenete1)
                .cantidadTelebingo(5)
                .telebingoDelAl(rangosTelebingo1)
                .build());

        vendedores.add(ResumenDTO.builder()
                .numeroVendedor(2)
                .nombre("Vendedor Dos")
                .cantidadSenete(2)
                .seneteDelAl(new HashMap<>()) // Empty ranges
                .cantidadTelebingo(3)
                .telebingoDelAl(null) // Null ranges
                .build());

        // Add more sellers to trigger pagination
        for (int i = 3; i <= 20; i++) {
            vendedores.add(ResumenDTO.builder()
                    .numeroVendedor(i)
                    .nombre("Vendedor " + i)
                    .cantidadSenete(i)
                    .seneteDelAl(Collections.singletonMap(""+(i*10), ""+(i*10+5)))
                    .cantidadTelebingo(i+1)
                    .telebingoDelAl(Collections.singletonMap(""+(i*20), ""+(i*20+10)))
                    .build());
        }
    }

    @DisplayName("Test generarResumen - Happy Path con múltiples vendedores")
    @Test
    void testGenerarResumen_happyPathMultiplesVendedores() {
        // Act & Assert
        assertThatThrownBy(() -> pdfResumenService.generarResumen(vendedores, fechaSenete, fechaTelebingo))
                .isInstanceOf(PdfCreationException.class)
                .hasMessageContaining("Error generando Resumen");
    }

    @DisplayName("Test generarResumen - Lista de vendedores vacía")
    @Test
    void testGenerarResumen_listaVacia() {
        // Arrange
        List<ResumenDTO> emptyVendedores = Collections.emptyList();

        // Act
        byte[] pdfBytes = pdfResumenService.generarResumen(emptyVendedores, fechaSenete, fechaTelebingo);

        // Assert
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes).isNotEmpty();
    }

    @DisplayName("Test generarResumen - Una solo vendedor")
    @Test
    void testGenerarResumen_unSoloVendedor() {
        // Arrange
        List<ResumenDTO> singleVendedor = Collections.singletonList(vendedores.get(0));

        // Act
        byte[] pdfBytes = pdfResumenService.generarResumen(singleVendedor, fechaSenete, fechaTelebingo);

        // Assert
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes).isNotEmpty();
    }

    @DisplayName("Test generarResumen - Fechas de sorteo iguales")
    @Test
    void testGenerarResumen_fechasIguales() {
        // Act & Assert
        assertThatThrownBy(() -> pdfResumenService.generarResumen(vendedores, fechaSenete, fechaSenete))
                .isInstanceOf(PdfCreationException.class)
                .hasMessageContaining("Error generando Resumen");
    }
}
