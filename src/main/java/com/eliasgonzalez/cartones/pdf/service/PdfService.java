package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.dto.ConfiguracionPdfDTO;
import com.eliasgonzalez.cartones.pdf.dto.EtiquetaDTO;
import com.eliasgonzalez.cartones.pdf.dto.RangoCortadoDTO;
import com.eliasgonzalez.cartones.pdf.dto.ResumenDTO;
import com.eliasgonzalez.cartones.pdf.entity.PdfProcesos;
import com.eliasgonzalez.cartones.pdf.enums.EstadoEnum;
import com.eliasgonzalez.cartones.pdf.interfaces.IPdfService;
import com.eliasgonzalez.cartones.pdf.interfaces.PdfProcesosRepository;
import com.eliasgonzalez.cartones.shared.exception.FileProcessingException;
import com.eliasgonzalez.cartones.shared.exception.PdfCreationException;
import com.eliasgonzalez.cartones.shared.exception.ResourceNotFoundException;
import com.eliasgonzalez.cartones.shared.exception.UnprocessableEntityException;
import com.eliasgonzalez.cartones.zip.ZipService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class PdfService implements IPdfService {

    private final PdfEtiquetasService pdfEtiquetasService;
    private final PdfResumenService pdfResumenService;
    private final PdfProcesosRepository pdfProcesosRepo;

    @Override
    @Transactional
    public Resource obtenerZipPdfs(String procesoIdRecibido, ConfiguracionPdfDTO config) {
        try {
            // 1. Buscar: Si no existe, es un 404 Not Found (o un error de recurso no encontrado)
            PdfProcesos pdfProcesos = pdfProcesosRepo.findById(procesoIdRecibido)
                    .orElseThrow(() -> new ResourceNotFoundException("El proceso con ID " + procesoIdRecibido + " no existe.", null));

            // 2. Validar lógica de rangos del JSON
            validarYFiltrarRangos(config);

            // Generar y guardar PDFs
            generarYGuardarPdfs(procesoIdRecibido, config.getFechaSorteo(), pdfProcesos, config);

            // 3. Validar Estado: Si existe pero el estado es incorrecto, es un 422
            if (!EstadoEnum.PENDIENTE.getValue().equals(pdfProcesos.getEstado())) {
                throw new UnprocessableEntityException(
                        "El proceso no está en estado PENDIENTE.",
                        List.of("El proceso " + procesoIdRecibido + " tiene un estado " + pdfProcesos.getEstado())
                );
            }

            // 4. Buscar bytes de PDFs guardados en la base de datos
            byte[] etiquetas = pdfProcesos.getPdfEtiquetas();
            byte[] resumen = pdfProcesos.getPdfResumen();

            if (etiquetas == null || resumen == null) {
                // Si no se encuentran los PDFs, devolvemos un error
                throw new FileProcessingException("No se encontraron procesos pendientes para: " + procesoIdRecibido, null);
            }

            // 5. Preparar Mapa para el ZIP y generarlo
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
            throw new FileProcessingException("Error inesperado en la generación de PDF.", List.of(e.getMessage()));
        }
    }

    @Transactional
    public void generarYGuardarPdfs(String procesoIdRecibido, LocalDate fechaSorteo,
                                    PdfProcesos pdfProceso, ConfiguracionPdfDTO config) {

        // Por ahora, datos de ejemplo
        List<EtiquetaDTO> etiquetasVacias = List.of();
        List<ResumenDTO> resumenVacio = List.of();

        byte[] etiquetas = pdfEtiquetasService.generarEtiquetas(etiquetasVacias, fechaSorteo.toString());
        byte[] resumen = pdfResumenService.generarResumen(resumenVacio, fechaSorteo.toString());

        // Actualizar la entidad con los PDFs generados
        // Se ejecuta solo si la excepción de arriba no es lanzada
        pdfProceso.setPdfEtiquetas(etiquetas);
        pdfProceso.setPdfResumen(resumen);
        pdfProceso.setEstado(EstadoEnum.COMPLETADO.getValue());

    }

    private void validarYFiltrarRangos(ConfiguracionPdfDTO config) {
        if (config.getRangosCortados() == null) return;

        // 1. Filtrar los que son 0-0 o inválidos
        List<RangoCortadoDTO> validos = config.getRangosCortados().stream()
                .filter(r -> r.getInicio() > 0 && r.getFin() > 0 && r.getInicio() <= r.getFin())
                .sorted(Comparator.comparingInt(RangoCortadoDTO::getInicio))
                .toList();

        // 2. Revisar solapamientos
        for (int i = 0; i < validos.size() - 1; i++) {
            if (validos.get(i).getFin() >= validos.get(i + 1).getInicio()) {
                throw new PdfCreationException("Validación de Rangos",
                        List.of("Los rangos " + validos.get(i).getInicio() + "-" + validos.get(i).getFin() +
                                " y " + validos.get(i+1).getInicio() + "-" + validos.get(i+1).getFin() + " se solapan."));
            }
        }
        // Actualizamos la lista en el config con los filtrados y ordenados
        config.setRangosCortados(validos);
    }
}