package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.dto.VendedorSimuladoDTO;
import com.eliasgonzalez.cartones.pdf.entity.PdfProcesos;
import com.eliasgonzalez.cartones.pdf.enums.EstadoEnum;
import com.eliasgonzalez.cartones.vendedor.entity.Vendedor;
import com.eliasgonzalez.cartones.vendedor.interfaces.VendedorRepository;
import com.eliasgonzalez.cartones.zip.ZipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import com.eliasgonzalez.cartones.shared.exception.UnprocessableEntityException;
import com.eliasgonzalez.cartones.shared.exception.FileProcessingException;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfServiceTest {

    @Mock
    private PdfEtiquetasService pdfEtiquetasService;

    @Mock
    private PdfResumenService pdfResumenService;

    @Mock
    private VendedorRepository vendedorRepo;

    @InjectMocks
    private PdfService pdfService;

    private String procesoId;
    private PdfProcesos pdfProceso;
    private List<VendedorSimuladoDTO> config;
    private LocalDate fechaSorteoSenete;
    private LocalDate fechaSorteoTelebingo;
    private Vendedor vendedor;

    @BeforeEach
    void setUp() {
        procesoId = "test-proceso-id";
        pdfProceso = PdfProcesos.builder()
                .procesoId(procesoId)
                .estado(EstadoEnum.VERIFICANDO.getValue())
                .build();
        config = Collections.singletonList(VendedorSimuladoDTO.builder().id(1L).build());
        fechaSorteoSenete = LocalDate.now();
        fechaSorteoTelebingo = LocalDate.now().plusDays(1);

        vendedor = new Vendedor();
        vendedor.setId(1L);
        vendedor.setProcesoId(procesoId);
        vendedor.setNombre("Test Vendedor");

        // Static mock for ZipService
        // Mockito.mockStatic() must be opened and closed in each test or in a try-with-resources
    }

    @DisplayName("Test obtenerZipPdfs - Happy Path")
    @Test
    void testObtenerZipPdfs_happyPath() throws IOException {
        // Arrange
        byte[] mockEtiquetasPdf = "etiquetas-pdf-content".getBytes();
        byte[] mockResumenPdf = "resumen-pdf-content".getBytes();
        Resource mockResource = mock(Resource.class); // Mock the returned resource

        when(vendedorRepo.findAllByProcesoId(procesoId)).thenReturn(Collections.singletonList(vendedor));
        when(pdfEtiquetasService.generarEtiquetas(any(), any(), any())).thenReturn(mockEtiquetasPdf);
        when(pdfResumenService.generarResumen(any(), any(), any())).thenReturn(mockResumenPdf);

        try (MockedStatic<ZipService> mockedZipService = mockStatic(ZipService.class)) {
            mockedZipService.when(() -> ZipService.crearZip(any(Map.class))).thenReturn(mockResource);

            // Act
            Resource result = pdfService.obtenerZipPdfs(procesoId, pdfProceso, config, fechaSorteoSenete, fechaSorteoTelebingo);

            // Assert
            assertThat(result).isEqualTo(mockResource);
            assertThat(pdfProceso.getPdfEtiquetas()).isEqualTo(mockEtiquetasPdf);
            assertThat(pdfProceso.getPdfResumen()).isEqualTo(mockResumenPdf);

            verify(vendedorRepo, times(1)).findAllByProcesoId(procesoId);
            verify(pdfEtiquetasService, times(1)).generarEtiquetas(any(), any(), any());
            verify(pdfResumenService, times(1)).generarResumen(any(), any(), any());
            mockedZipService.verify(() -> ZipService.crearZip(any(Map.class)), times(1));
        }
    }

    @DisplayName("Test obtenerZipPdfs - Estado de proceso inválido")
    @Test
    void testObtenerZipPdfs_invalidProcessState() throws IOException {
        // Arrange
        pdfProceso.setEstado(EstadoEnum.PENDIENTE.getValue()); // Set an invalid state

        // Mock generarPdfs dependencies to prevent it from throwing exceptions prematurely
        when(vendedorRepo.findAllByProcesoId(procesoId)).thenReturn(Collections.singletonList(vendedor));
        when(pdfEtiquetasService.generarEtiquetas(any(), any(), any())).thenReturn("mock-etiquetas".getBytes());
        when(pdfResumenService.generarResumen(any(), any(), any())).thenReturn("mock-resumen".getBytes());


        // Act & Assert
        assertThatThrownBy(() -> pdfService.obtenerZipPdfs(procesoId, pdfProceso, config, fechaSorteoSenete, fechaSorteoTelebingo))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessageContaining("El estado del proceso no es válido para descarga.");

        // Verify that generarPdfs dependencies were called, but ZipService was not
        verify(vendedorRepo, times(1)).findAllByProcesoId(procesoId);
        verify(pdfEtiquetasService, times(1)).generarEtiquetas(any(), any(), any());
        verify(pdfResumenService, times(1)).generarResumen(any(), any(), any());

        try (MockedStatic<ZipService> mockedZipService = mockStatic(ZipService.class)) {
            mockedZipService.verify(() -> ZipService.crearZip(any(Map.class)), never());
        }
    }

    @DisplayName("Test obtenerZipPdfs - IOException al crear ZIP")
    @Test
    void testObtenerZipPdfs_ioExceptionCreatingZip() throws IOException {
        // Arrange
        byte[] mockEtiquetasPdf = "etiquetas-pdf-content".getBytes();
        byte[] mockResumenPdf = "resumen-pdf-content".getBytes();

        when(vendedorRepo.findAllByProcesoId(procesoId)).thenReturn(Collections.singletonList(vendedor));
        when(pdfEtiquetasService.generarEtiquetas(any(), any(), any())).thenReturn(mockEtiquetasPdf);
        when(pdfResumenService.generarResumen(any(), any(), any())).thenReturn(mockResumenPdf);

        try (MockedStatic<ZipService> mockedZipService = mockStatic(ZipService.class)) {
            mockedZipService.when(() -> ZipService.crearZip(any(Map.class))).thenThrow(new IOException("Error de ZIP de prueba"));

            // Act & Assert
            assertThatThrownBy(() -> pdfService.obtenerZipPdfs(procesoId, pdfProceso, config, fechaSorteoSenete, fechaSorteoTelebingo))
                    .isInstanceOf(FileProcessingException.class)
                    .hasMessageContaining("Error generando ZIP");

            verify(vendedorRepo, times(1)).findAllByProcesoId(procesoId);
            verify(pdfEtiquetasService, times(1)).generarEtiquetas(any(), any(), any());
            verify(pdfResumenService, times(1)).generarResumen(any(), any(), any());
            mockedZipService.verify(() -> ZipService.crearZip(any(Map.class)), times(1));
        }
    }

    @DisplayName("Test obtenerZipPdfs - Excepción general al generar PDFs")
    @Test
    void testObtenerZipPdfs_generalExceptionGeneratingPdfs() {
        // Arrange
        // We need to mock the call to generarPdfs from obtenerZipPdfs, but generarPdfs is in the same class.
        // So, we need to spy on pdfService and mock the private generarPdfs method.
        PdfService spyPdfService = spy(pdfService);

        doThrow(new RuntimeException("Error simulado al generar PDFs"))
                .when(spyPdfService).generarPdfs(any(), any(), any(), any());

        // Act & Assert
        assertThatThrownBy(() -> spyPdfService.obtenerZipPdfs(procesoId, pdfProceso, config, fechaSorteoSenete, fechaSorteoTelebingo))
                .isInstanceOf(FileProcessingException.class)
                .hasMessageContaining("Error inesperado en PDF Service");

        verify(spyPdfService, times(1)).generarPdfs(any(), any(), any(), any());
        try (MockedStatic<ZipService> mockedZipService = mockStatic(ZipService.class)) {
            mockedZipService.verify(() -> ZipService.crearZip(any(Map.class)), never());
        }
    }

    @DisplayName("Test generarPdfs - Happy Path")
    @Test
    void testGenerarPdfs_happyPath() {
        // Arrange
        byte[] mockEtiquetasPdf = "etiquetas-pdf-content".getBytes();
        byte[] mockResumenPdf = "resumen-pdf-content".getBytes();

        when(vendedorRepo.findAllByProcesoId(procesoId)).thenReturn(Collections.singletonList(vendedor));
        when(pdfEtiquetasService.generarEtiquetas(any(), any(), any())).thenReturn(mockEtiquetasPdf);
        when(pdfResumenService.generarResumen(any(), any(), any())).thenReturn(mockResumenPdf);

        // Act
        Map<String, byte[]> result = pdfService.generarPdfs(config, fechaSorteoSenete, fechaSorteoTelebingo, procesoId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).containsKey("etiquetas");
        assertThat(result).containsKey("resumen");
        assertThat(result.get("etiquetas")).isEqualTo(mockEtiquetasPdf);
        assertThat(result.get("resumen")).isEqualTo(mockResumenPdf);

        verify(vendedorRepo, times(1)).findAllByProcesoId(procesoId);
        verify(pdfEtiquetasService, times(1)).generarEtiquetas(any(), any(), any());
        verify(pdfResumenService, times(1)).generarResumen(any(), any(), any());
    }

    @DisplayName("Test generarPdfs - pdfEtiquetasService.generarEtiquetas devuelve null")
    @Test
    void testGenerarPdfs_etiquetasPdfNull() {
        // Arrange
        byte[] mockResumenPdf = "resumen-pdf-content".getBytes();

        when(vendedorRepo.findAllByProcesoId(procesoId)).thenReturn(Collections.singletonList(vendedor));
        when(pdfEtiquetasService.generarEtiquetas(any(), any(), any())).thenReturn(null); // Simulate null return
        when(pdfResumenService.generarResumen(any(), any(), any())).thenReturn(mockResumenPdf);

        // Act & Assert
        assertThatThrownBy(() -> pdfService.generarPdfs(config, fechaSorteoSenete, fechaSorteoTelebingo, procesoId))
                .isInstanceOf(FileProcessingException.class)
                .hasMessageContaining("Error: El PDF de etiquetas no pudo ser generado.");

        verify(vendedorRepo, times(1)).findAllByProcesoId(procesoId);
        verify(pdfEtiquetasService, times(1)).generarEtiquetas(any(), any(), any());
        verify(pdfResumenService, times(1)).generarResumen(any(), any(), any());
    }

    @DisplayName("Test generarPdfs - pdfResumenService.generarResumen devuelve null")
    @Test
    void testGenerarPdfs_resumenPdfNull() {
        // Arrange
        byte[] mockEtiquetasPdf = "etiquetas-pdf-content".getBytes();

        when(vendedorRepo.findAllByProcesoId(procesoId)).thenReturn(Collections.singletonList(vendedor));
        when(pdfEtiquetasService.generarEtiquetas(any(), any(), any())).thenReturn(mockEtiquetasPdf);
        when(pdfResumenService.generarResumen(any(), any(), any())).thenReturn(null); // Simulate null return

        // Act & Assert
        assertThatThrownBy(() -> pdfService.generarPdfs(config, fechaSorteoSenete, fechaSorteoTelebingo, procesoId))
                .isInstanceOf(FileProcessingException.class)
                .hasMessageContaining("Error: El PDF de resumen no pudo ser generado.");

        verify(vendedorRepo, times(1)).findAllByProcesoId(procesoId);
        verify(pdfEtiquetasService, times(1)).generarEtiquetas(any(), any(), any());
        verify(pdfResumenService, times(1)).generarResumen(any(), any(), any());
    }

    @DisplayName("Test generarPdfs - no hay vendedores")
    @Test
    void testGenerarPdfs_noVendedores() {
        // Arrange
        when(vendedorRepo.findAllByProcesoId(procesoId)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThatThrownBy(() -> pdfService.generarPdfs(config, fechaSorteoSenete, fechaSorteoTelebingo, procesoId))
                .isInstanceOf(NullPointerException.class) // Mapper throws NullPointerException directly
                .hasMessageContaining("El vendedor es null");

        verify(vendedorRepo, times(1)).findAllByProcesoId(procesoId);
        // Ensure PDF generation services were not called if no sellers (because of early exception)
        verify(pdfEtiquetasService, never()).generarEtiquetas(any(), any(), any());
        verify(pdfResumenService, never()).generarResumen(any(), any(), any());
    }
}
