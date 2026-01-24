package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.component.SaveInMemoryTemp;
import com.eliasgonzalez.cartones.pdf.entity.PdfProcesos;
import com.eliasgonzalez.cartones.pdf.interfaces.IPdfService;
import com.eliasgonzalez.cartones.pdf.interfaces.PdfProcesosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Resource zip = pdfService.obtenerZipPdfs(
                procesoId,
                proceso,
                saveInMemoryTemp.getVendedorSimuladoDTOs(),
                saveInMemoryTemp.getFechaSorteoSenete(),
                saveInMemoryTemp.getFechaSorteoTelebingo()
        );

        // Finalizar proceso
        ProcesoIdService.VerificandoToCompletado(procesoId, proceso);
        pdfProcesosRepo.save(proceso);

        return zip;
    }
}