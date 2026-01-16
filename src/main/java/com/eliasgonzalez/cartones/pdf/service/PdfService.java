package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.dto.*;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PdfService implements IPdfService {

    private final PdfEtiquetasService pdfEtiquetasService;
    private final PdfResumenService pdfResumenService;
    private final PdfProcesosRepository pdfProcesosRepo;

    @Override
    @Transactional
    public Resource obtenerZipPdfs(String procesoIdRecibido, SimulacionRequestDTO config) {
        try {
            // 1. Buscar Proceso
            PdfProcesos pdfProcesos = pdfProcesosRepo.findById(procesoIdRecibido)
                    .orElseThrow(() -> new ResourceNotFoundException("El proceso con ID " + procesoIdRecibido + " no existe.", null));

            // 2. Validar lógica de rangos (Ahora valida ambas listas por separado)
            validarYFiltrarRangos(config);

            // 3. Generar y guardar PDFs
            generarYGuardarPdfs(procesoIdRecibido, pdfProcesos, config);

            // 4. Validar Estado
            if (!EstadoEnum.PENDIENTE.getValue().equals(pdfProcesos.getEstado()) &&
                    !EstadoEnum.COMPLETADO.getValue().equals(pdfProcesos.getEstado())) { // Agregué COMPLETADO por si se regenera
                throw new UnprocessableEntityException(
                        "El estado del proceso no es válido para descarga.",
                        List.of("Estado actual: " + pdfProcesos.getEstado())
                );
            }

            // 5. Recuperar bytes
            byte[] etiquetas = pdfProcesos.getPdfEtiquetas();
            byte[] resumen = pdfProcesos.getPdfResumen();

            if (etiquetas == null || resumen == null) {
                throw new FileProcessingException("No se encontraron archivos PDF generados para: " + procesoIdRecibido, null);
            }

            // 6. Crear ZIP
            Map<String, byte[]> misPdfs = new HashMap<>();
            misPdfs.put("Imprimir_etiquetas.pdf", etiquetas);
            misPdfs.put("Resumen_entrega.pdf", resumen);

            return ZipService.crearZip(misPdfs);

        } catch (IOException e) {
            throw new FileProcessingException("Error de E/S al generar el ZIP: " + procesoIdRecibido, List.of(e.getMessage()));
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e; // Relanzar excepciones propias
            throw new FileProcessingException("Error inesperado en la generación de PDF.", List.of(e.getMessage()));
        }
    }

    @Transactional
    public void generarYGuardarPdfs(String procesoIdRecibido, PdfProcesos pdfProceso, SimulacionRequestDTO config) {

        // AQUI DEBERÍAS LLAMAR A TU DISTRIBUCION SERVICE PARA OBTENER DATOS REALES
        // Por ahora mantenemos los datos de ejemplo vacíos
        List<EtiquetaDTO> etiquetasVacias = List.of();
        List<ResumenDTO> resumenVacio = List.of();

        // Construimos un string de fechas para pasar al generador (o pasas las fechas sueltas si actualizas ese servicio)
        String fechasTexto = String.format("Seneté: %s | Telebingo: %s",
                config.getFechaSorteoSenete(), config.getFechaSorteoTelebingo());

        byte[] etiquetas = pdfEtiquetasService.generarEtiquetas(etiquetasVacias, fechasTexto);
        byte[] resumen = pdfResumenService.generarResumen(resumenVacio, fechasTexto);

        pdfProceso.setPdfEtiquetas(etiquetas);
        pdfProceso.setPdfResumen(resumen);
        pdfProceso.setEstado(EstadoEnum.COMPLETADO.getValue());
    }

    private void validarYFiltrarRangos(SimulacionRequestDTO config) {
        // Validar y limpiar Pool Seneté
        if (config.getPoolSenete() != null && !config.getPoolSenete().isEmpty()) {
            config.setPoolSenete(procesarListaRangos(config.getPoolSenete(), "Seneté"));
        }

        // Validar y limpiar Pool Telebingo
        if (config.getPoolTelebingo() != null && !config.getPoolTelebingo().isEmpty()) {
            config.setPoolTelebingo(procesarListaRangos(config.getPoolTelebingo(), "Telebingo"));
        }
    }

    /**
     * Método auxiliar para filtrar inválidos, ordenar y verificar solapamientos
     */
    private List<RangoCortadoDTO> procesarListaRangos(List<RangoCortadoDTO> rangos, String nombreJuego) {
        // 1. Filtrar los que son 0-0 o inválidos y Ordenar
        List<RangoCortadoDTO> validos = rangos.stream()
                .filter(r -> r.getInicio() > 0 && r.getFin() > 0 && r.getInicio() <= r.getFin())
                .sorted(Comparator.comparingInt(RangoCortadoDTO::getInicio))
                .collect(Collectors.toList());

        // 2. Revisar solapamientos
        reviewSolapamientos(validos, nombreJuego);

        return validos;
    }

    public void reviewSolapamientos(List<RangoCortadoDTO> validos, String nombreJuego) {
        for (int i = 0; i < validos.size() - 1; i++) {
            if (validos.get(i).getFin() >= validos.get(i + 1).getInicio()) {
                throw new PdfCreationException("Validación de Rangos (" + nombreJuego + ")",
                        List.of("Los rangos " + validos.get(i).getInicio() + "-" + validos.get(i).getFin() +
                                " y " + validos.get(i+1).getInicio() + "-" + validos.get(i+1).getFin() + " se solapan."));
            }
        }
    }
}