package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.component.SaveInMemoryTemp;
import com.eliasgonzalez.cartones.pdf.entity.PdfProcesos;
import com.eliasgonzalez.cartones.pdf.interfaces.IPdfService;
import com.eliasgonzalez.cartones.pdf.interfaces.PdfProcesosRepository;
import com.eliasgonzalez.cartones.shared.exception.UnprocessableEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GestionArchivoPdfService {

    private final IPdfService pdfService;
    private final SaveInMemoryTemp saveInMemoryTemp;
    private final PdfProcesosRepository pdfProcesosRepo;
    private final GestionDistribucionService gestionDistribucionService;

    @Transactional
    public Resource generarPaqueteZip(String procesoId) {
        PdfProcesos proceso = gestionDistribucionService.buscarProcesoOError(procesoId);

        try {
            Resource zip = pdfService.obtenerZipPdfs(
                    procesoId,
                    saveInMemoryTemp.getVendedorSimuladoDTOs(),
                    saveInMemoryTemp.getFechaSorteoSenete(),
                    saveInMemoryTemp.getFechaSorteoTelebingo()
            );

            // Finalizar proceso
            ProcesoIdService.VerificandoToCompletado(procesoId, proceso);
            pdfProcesosRepo.save(proceso);

            return zip;
        } catch (IOException e) {
            throw new UnprocessableEntityException("Error al generar el archivo comprimido de etiquetas.", List.of(e.getMessage()));
        }
    }
}