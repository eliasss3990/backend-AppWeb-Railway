package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.component.SaveInMemoryTemp;
import com.eliasgonzalez.cartones.pdf.dto.SimulacionRequestDTO;
import com.eliasgonzalez.cartones.pdf.dto.VendedorSimuladoDTO;
import com.eliasgonzalez.cartones.pdf.entity.PdfProcesos;
import com.eliasgonzalez.cartones.pdf.interfaces.PdfProcesosRepository;
import com.eliasgonzalez.cartones.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestionDistribucionService {

    private final DistribucionService distribucionService;
    private final PdfProcesosRepository pdfProcesosRepo;
    private final SaveInMemoryTemp saveInMemoryTemp;

    @Transactional
    public List<VendedorSimuladoDTO> procesarSimulacion(String procesoId, SimulacionRequestDTO solicitud) {
        // 1. Validar existencia del proceso
        PdfProcesos proceso = buscarProceso(procesoId);
        log.info("Proceso encontrado: {}", proceso.toString());

        // 2. Actualizar estado del proceso
        ProcesoIdService.PendienteToVerificando(procesoId, proceso);
        pdfProcesosRepo.save(proceso);

        // 3. Ejecutar la lógica para la simulación
        List<VendedorSimuladoDTO> resultado = distribucionService.simularDistribucion(solicitud);

        log.info("Proceso actualizado: {}", proceso.toString());

        // 4. Persistir temporalmente para la descarga posterior
        saveInMemoryTemp.guardar(resultado);
        saveInMemoryTemp.setFechaSorteoSenete(solicitud.getFechaSorteoSenete());
        saveInMemoryTemp.setFechaSorteoTelebingo(solicitud.getFechaSorteoTelebingo());

        return resultado;
    }

    public PdfProcesos buscarProceso(String procesoId) {
        return pdfProcesosRepo.findById(procesoId)
                .orElseThrow(() -> new ResourceNotFoundException("El proceso con ID " + procesoId + " no existe.", List.of()));
    }
}
