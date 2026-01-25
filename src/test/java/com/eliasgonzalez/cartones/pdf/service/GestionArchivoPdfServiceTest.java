package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.component.SaveInMemoryTemp;
import com.eliasgonzalez.cartones.pdf.entity.PdfProcesos;
import com.eliasgonzalez.cartones.pdf.interfaces.IPdfService;
import com.eliasgonzalez.cartones.pdf.interfaces.PdfProcesosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestionArchivoPdfServiceTest {

    @Mock
    private IPdfService pdfService;

    @Mock
    private SaveInMemoryTemp saveInMemoryTemp;

    @Mock
    private PdfProcesosRepository pdfProcesosRepo;

    @Mock
    private GestionDistribucionService gestionDistribucionService;

    @InjectMocks
    private GestionArchivoPdfService gestionArchivoPdfService;

    private String procesoId;
    private PdfProcesos mockPdfProcesos;
    private Resource mockZipResource;

    @BeforeEach
    void setUp() {
        procesoId = "test-proceso-id";
        mockPdfProcesos = PdfProcesos.builder().procesoId(procesoId).build();
        mockZipResource = mock(Resource.class); // Mock the returned resource
    }

    @DisplayName("Test generarPaqueteZip - Happy Path")
    @Test
    void testGenerarPaqueteZip_happyPath() {
        // Arrange
        when(gestionDistribucionService.buscarProcesoOError(anyString())).thenReturn(mockPdfProcesos);
        when(saveInMemoryTemp.getVendedorSimuladoDTOs()).thenReturn(Collections.emptyList());
        when(saveInMemoryTemp.getFechaSorteoSenete()).thenReturn(LocalDate.now());
        when(saveInMemoryTemp.getFechaSorteoTelebingo()).thenReturn(LocalDate.now());
        when(pdfService.obtenerZipPdfs(anyString(), any(), any(), any(), any())).thenReturn(mockZipResource);

        try (MockedStatic<ProcesoIdService> mockedProcesoIdService = mockStatic(ProcesoIdService.class)) {
            mockedProcesoIdService.when(() -> ProcesoIdService.VerificandoToCompletado(anyString(), any()))
                    .thenAnswer(invocation -> null); // Simulate void method

            // Act
            Resource result = gestionArchivoPdfService.generarPaqueteZip(procesoId);

            // Assert
            assertThat(result).isEqualTo(mockZipResource);

            verify(gestionDistribucionService, times(1)).buscarProcesoOError(procesoId);
            verify(saveInMemoryTemp, times(1)).getVendedorSimuladoDTOs();
            verify(saveInMemoryTemp, times(1)).getFechaSorteoSenete();
            verify(saveInMemoryTemp, times(1)).getFechaSorteoTelebingo();
            verify(pdfService, times(1)).obtenerZipPdfs(anyString(), eq(mockPdfProcesos), any(), any(), any());
            mockedProcesoIdService.verify(() -> ProcesoIdService.VerificandoToCompletado(procesoId, mockPdfProcesos), times(1));
            verify(pdfProcesosRepo, times(1)).save(mockPdfProcesos);
        }
    }

    @DisplayName("Test generarPaqueteZip - Excepción al buscar proceso")
    @Test
    void testGenerarPaqueteZip_exceptionOnBuscarProceso() {
        // Arrange
        RuntimeException mockException = new RuntimeException("Proceso no encontrado");
        when(gestionDistribucionService.buscarProcesoOError(anyString())).thenThrow(mockException);

        // Act & Assert
        assertThatThrownBy(() -> gestionArchivoPdfService.generarPaqueteZip(procesoId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Proceso no encontrado");

        verify(gestionDistribucionService, times(1)).buscarProcesoOError(procesoId);
        verifyNoInteractions(pdfService);
        verifyNoInteractions(saveInMemoryTemp);
        verifyNoInteractions(pdfProcesosRepo);
    }

    @DisplayName("Test generarPaqueteZip - Excepción al obtener ZIP de PDFs")
    @Test
    void testGenerarPaqueteZip_exceptionOnObtenerZipPdfs() {
        // Arrange
        when(gestionDistribucionService.buscarProcesoOError(anyString())).thenReturn(mockPdfProcesos);
        when(saveInMemoryTemp.getVendedorSimuladoDTOs()).thenReturn(Collections.emptyList());
        when(saveInMemoryTemp.getFechaSorteoSenete()).thenReturn(LocalDate.now());
        when(saveInMemoryTemp.getFechaSorteoTelebingo()).thenReturn(LocalDate.now());

        RuntimeException mockException = new RuntimeException("Error al generar ZIP de PDFs");
        when(pdfService.obtenerZipPdfs(anyString(), any(), any(), any(), any())).thenThrow(mockException);

        // Act & Assert
        assertThatThrownBy(() -> gestionArchivoPdfService.generarPaqueteZip(procesoId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al generar ZIP de PDFs");

        verify(gestionDistribucionService, times(1)).buscarProcesoOError(procesoId);
        verify(saveInMemoryTemp, times(1)).getVendedorSimuladoDTOs();
        verify(saveInMemoryTemp, times(1)).getFechaSorteoSenete();
        verify(saveInMemoryTemp, times(1)).getFechaSorteoTelebingo();
        verify(pdfService, times(1)).obtenerZipPdfs(anyString(), eq(mockPdfProcesos), any(), any(), any());
        verifyNoInteractions(pdfProcesosRepo); // Should not save if an exception occurs before
    }
}
