package com.eliasgonzalez.cartones.vendedor.service;

import com.eliasgonzalez.cartones.excel.interfaces.IExcelService;
import com.eliasgonzalez.cartones.pdf.entity.PdfProcesos;
import com.eliasgonzalez.cartones.pdf.interfaces.PdfProcesosRepository;
import com.eliasgonzalez.cartones.vendedor.dto.VendedorResponseDTO;
import com.eliasgonzalez.cartones.vendedor.entity.Vendedor;
import com.eliasgonzalez.cartones.vendedor.interfaces.VendedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendedorServiceTest {

    @Mock
    private VendedorRepository vendedorRepo;

    @Mock
    private IExcelService excelService;

    @Mock
    private PdfProcesosRepository pdfProcesosRepo;

    @InjectMocks
    private VendedorService vendedorService;

    private Vendedor vendedor;

    @BeforeEach
    void setUp() {
        vendedor = new Vendedor();
        vendedor.setId(1L);
        vendedor.setNombre("John Doe");
    }

    @DisplayName("Test para listar vendedores cuando la lista no está vacía")
    @Test
    void testListaVendedores_conResultados() {
        // Arrange
        when(vendedorRepo.findAll()).thenReturn(List.of(vendedor));

        // Act
        List<VendedorResponseDTO> resultado = vendedorService.listaVendedores();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("John Doe");
    }

    @DisplayName("Test para listar vendedores cuando la lista está vacía")
    @Test
    void testListaVendedores_vacia() {
        // Arrange
        when(vendedorRepo.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<VendedorResponseDTO> resultado = vendedorService.listaVendedores();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
    }

    @DisplayName("Test para listar vendedores válidos con resultados")
    @Test
    void testListarVendedoresValidos_conResultados() {
        // Arrange
        String procesoId = "test-proceso";
        vendedor.setProcesoId(procesoId);
        when(vendedorRepo.findVendedoresValidos(procesoId)).thenReturn(List.of(vendedor));

        // Act
        List<VendedorResponseDTO> resultado = vendedorService.listarVendedoresValidos(procesoId);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("John Doe");
    }

    @DisplayName("Test para listar vendedores válidos cuando la lista está vacía")
    @Test
    void testListarVendedoresValidos_vacia() {
        // Arrange
        String procesoId = "test-proceso";
        when(vendedorRepo.findVendedoresValidos(procesoId)).thenReturn(Collections.emptyList());

        // Act
        List<VendedorResponseDTO> resultado = vendedorService.listarVendedoresValidos(procesoId);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
    }

    @DisplayName("Test para eliminar todos los vendedores")
    @Test
    void testEliminarTodosLosVendedores() {
        // Arrange
        // No arrangement needed for this test

        // Act
        vendedorService.eliminarTodosLosVendedores();

        // Assert
        verify(vendedorRepo, times(1)).deleteAll();
    }

    @DisplayName("Test para iniciar un nuevo proceso")
    @Test
    void testIniciarProceso() {
        // Arrange
        when(pdfProcesosRepo.save(any(PdfProcesos.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String procesoId = vendedorService.iniciarProceso();

        // Assert
        assertThat(procesoId).isNotNull();
        ArgumentCaptor<PdfProcesos> captor = ArgumentCaptor.forClass(PdfProcesos.class);
        verify(pdfProcesosRepo, times(1)).save(captor.capture());
        assertThat(captor.getValue().getProcesoId()).isEqualTo(procesoId);
    }

    @DisplayName("Test para procesar un archivo Excel")
    @Test
    void testProcesarExcel() {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", "test data".getBytes());
        String procesoId = "test-proceso-id";
        doNothing().when(excelService).leerExcel(any(MultipartFile.class), anyString());

        // Act
        vendedorService.procesarExcel(file, procesoId);

        // Assert
        verify(excelService, times(1)).leerExcel(eq(file), eq(procesoId));
    }
}