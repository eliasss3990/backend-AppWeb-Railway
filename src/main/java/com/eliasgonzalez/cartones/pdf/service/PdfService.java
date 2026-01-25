package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.dto.*;
import com.eliasgonzalez.cartones.pdf.entity.PdfProcesos;
import com.eliasgonzalez.cartones.pdf.enums.EstadoEnum;
import com.eliasgonzalez.cartones.pdf.interfaces.IPdfService;
import com.eliasgonzalez.cartones.pdf.mapper.PdfMapper;
import com.eliasgonzalez.cartones.shared.exception.FileProcessingException;
import com.eliasgonzalez.cartones.shared.exception.UnprocessableEntityException;
import com.eliasgonzalez.cartones.vendedor.entity.Vendedor;
import com.eliasgonzalez.cartones.vendedor.interfaces.VendedorRepository;
import com.eliasgonzalez.cartones.zip.ZipService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdfService implements IPdfService {

    private final PdfEtiquetasService pdfEtiquetasService;
    private final PdfResumenService pdfResumenService;
    private final VendedorRepository vendedorRepo;

    private static final String ETIQUETAS = "etiquetas";
    private static final String RESUMEN = "resumen";

    @Override
    @Transactional
    public Resource obtenerZipPdfs(
            String procesoIdRecibido,
            PdfProcesos proceso,
            List<VendedorSimuladoDTO> config,
            LocalDate fechaSorteoSenete,
            LocalDate fechaSorteoTelebingo
    ) {
        try {
            // Generamos los PDFs
            Map<String, byte[]> pdfsGenerados = generarPdfs(config, fechaSorteoSenete, fechaSorteoTelebingo, procesoIdRecibido);
            byte[] etiquetas = pdfsGenerados.get(ETIQUETAS);
            byte[] resumen = pdfsGenerados.get(RESUMEN);

            if (!EstadoEnum.VERIFICANDO.getValue().equals(proceso.getEstado())) {
                throw new UnprocessableEntityException("El estado del proceso no es válido para descarga.", List.of("Estado: " + proceso.getEstado()));
            }

            Map<String, byte[]> misPdfs = new HashMap<>();
            misPdfs.put("Imprimir_etiquetas.pdf", etiquetas);
            misPdfs.put("Resumen_entrega.pdf", resumen);

            proceso.setPdfEtiquetas(pdfsGenerados.get(ETIQUETAS));
            proceso.setPdfResumen(pdfsGenerados.get(RESUMEN));

            return ZipService.crearZip(misPdfs);

        } catch (IOException e) {
            throw new FileProcessingException("Error generando ZIP", List.of(e.getMessage()));
        } catch (UnprocessableEntityException e) {
            throw e; // Relanzar la excepción específica
        } catch (Exception e) {
            throw new FileProcessingException("Error inesperado en PDF Service", List.of(e.getMessage()));
        }
    }

    public Map<String, byte[]> generarPdfs(List<VendedorSimuladoDTO> config,
                            LocalDate fechaSorteoSenete, LocalDate fechaSorteoTelebingo,
                            String procesoIdRecibido
    ) {
        List<Vendedor> vendedoresList = vendedorRepo.findAllByProcesoId(procesoIdRecibido);

        // CONVERSIÓN DE LISTA A MAPA
        // Se realizan estas validaciones solo por si acaso, ya que config ya viene con valores correctos
        Map<Long, Vendedor> vendedoresMap = vendedoresList.stream()
                .filter(v -> v.getId() != null) // Evitas nulos accidentales
                .collect(Collectors.toMap(
                        Vendedor::getId,
                        vendedor -> vendedor,
                        (existente, reemplazo) -> existente // Si hay IDs duplicados, mantiene el primero y no lanza excepción
                ));
        List<EtiquetaDTO> etiquetasMapeado = PdfMapper.toEtiquetaDTOs(config, vendedoresMap);
        List<ResumenDTO> resumenMapeado = PdfMapper.toResumenDTOs(config, vendedoresMap);

        byte[] etiquetas = pdfEtiquetasService.generarEtiquetas(etiquetasMapeado, fechaSorteoSenete, fechaSorteoTelebingo);
        byte[] resumen = pdfResumenService.generarResumen(resumenMapeado, fechaSorteoSenete, fechaSorteoTelebingo);

        if (etiquetas == null) {
            throw new FileProcessingException("Error: El PDF de etiquetas no pudo ser generado.", List.of());
        }
        if (resumen == null) {
            throw new FileProcessingException("Error: El PDF de resumen no pudo ser generado.", List.of());
        }

        Map<String, byte[]> resultado = new HashMap<>();
        resultado.put(ETIQUETAS, etiquetas);
        resultado.put(RESUMEN, resumen);

        return resultado;
    }
}