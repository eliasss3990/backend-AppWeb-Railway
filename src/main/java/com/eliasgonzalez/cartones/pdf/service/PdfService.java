package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.dto.EtiquetaDTO;
import com.eliasgonzalez.cartones.pdf.dto.ResumenDTO;
import com.eliasgonzalez.cartones.pdf.interfaces.IPdfService;
import com.eliasgonzalez.cartones.shared.exception.FileProcessingException;
import com.eliasgonzalez.cartones.zip.ZipService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfService implements IPdfService {

    // Inyección de los servicios de generación de PDFs
    private final PdfEtiquetasService pdfEtiquetasService;
    private final PdfResumenService pdfResumenService;

    public PdfService(PdfEtiquetasService pdfEtiquetasService, PdfResumenService pdfResumenService) {
        this.pdfEtiquetasService = pdfEtiquetasService;
        this.pdfResumenService = pdfResumenService;
    }

    @Override
    public Resource obtenerZipPdfs(String procesoIdRecibido) {
        try {
            // Por ahora, datos de ejemplo
            String fechaSorteo = "12/10/2025";
            List<EtiquetaDTO> etiquetasVacias = List.of();
            List<ResumenDTO> resumenVacio = List.of();

            // Generación de bytes de PDFs
            byte[] etiquetas = pdfEtiquetasService.generarEtiquetas(etiquetasVacias, fechaSorteo);
            byte[] resumen = pdfResumenService.generarResumen(resumenVacio, fechaSorteo);

            // Preparar Mapa para el ZIP
            Map<String, byte[]> misPdfs = new HashMap<>();
            misPdfs.put("Imprimir_etiquetas.pdf", etiquetas);
            misPdfs.put("Resumen_entrega.pdf", resumen);

            // Generar ZIP y retornar directamente el Resource
            return ZipService.crearZip(misPdfs);

        } catch (IOException e) {
            // Error específico de entrada/salida de bytes
            throw new FileProcessingException("Error de E/S al generar el ZIP del proceso: " + procesoIdRecibido, List.of(e.getMessage()));
        } catch (Exception e) {
            // Cualquier otro error (OpenPDF, Nulls, etc.)
            throw new FileProcessingException("Error inesperado en la generación de PDFs", List.of(e.getMessage()));
        }
    }
}