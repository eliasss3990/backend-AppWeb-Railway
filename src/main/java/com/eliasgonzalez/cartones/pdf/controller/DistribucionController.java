package com.eliasgonzalez.cartones.pdf.controller;

import com.eliasgonzalez.cartones.pdf.component.SaveInMemoryTemp;
import com.eliasgonzalez.cartones.pdf.dto.SimulacionRequestDTO;
import com.eliasgonzalez.cartones.pdf.dto.VendedorSimuladoDTO;
import com.eliasgonzalez.cartones.pdf.entity.PdfProcesos;
import com.eliasgonzalez.cartones.pdf.interfaces.IPdfService;
import com.eliasgonzalez.cartones.pdf.interfaces.PdfProcesosRepository;
import com.eliasgonzalez.cartones.pdf.service.DistribucionService;
import com.eliasgonzalez.cartones.pdf.service.ProcesoIdService;
import com.eliasgonzalez.cartones.shared.exception.ResourceNotFoundException;
import com.eliasgonzalez.cartones.shared.exception.UnprocessableEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/distribuciones")
@RequiredArgsConstructor
public class DistribucionController {

    private final DistribucionService distribucionService;
    private final IPdfService pdfService;
    private final PdfProcesosRepository pdfProcesosRepo;
    private final SaveInMemoryTemp saveInMemoryTemp;


    @PostMapping("/{procesoId}/simular")
    public ResponseEntity<List<VendedorSimuladoDTO>> simularDistribucion(
            @RequestBody SimulacionRequestDTO request,
            @PathVariable(name = "procesoId") String procesoIdRecibido) {

        PdfProcesos pdfProcesos = pdfProcesosRepo.findById(procesoIdRecibido)
                .orElseThrow(() -> new ResourceNotFoundException("El proceso con ID " + procesoIdRecibido + " no existe.", List.of()));

        ProcesoIdService.PendienteToVerificando(procesoIdRecibido, pdfProcesos);

        pdfProcesosRepo.save(pdfProcesos);

        List<VendedorSimuladoDTO> simulacion = distribucionService.simularDistribucion(request);

        // Guardar en memoria
        saveInMemoryTemp.guardar(simulacion);
        saveInMemoryTemp.setFechaSorteoSenete(request.getFechaSorteoSenete());
        saveInMemoryTemp.setFechaSorteoTelebingo(request.getFechaSorteoTelebingo());

        return ResponseEntity.ok(simulacion);
    }


    @GetMapping("/{procesoId}/pdfs")
    public ResponseEntity<Resource> guardarDistribucion(@PathVariable(name = "procesoId") String procesoIdRecibido){

        // Recuperar de memoria usando la instancia inyectada
        List<VendedorSimuladoDTO> distribucionAceptada = saveInMemoryTemp.getVendedorSimuladoDTOs();
        LocalDate fechaSorteoSenete = saveInMemoryTemp.getFechaSorteoSenete();
        LocalDate fechaSorteoTelebingo = saveInMemoryTemp.getFechaSorteoTelebingo();

        PdfProcesos pdfProcesos = pdfProcesosRepo.findById(procesoIdRecibido)
                .orElseThrow(() -> new ResourceNotFoundException("El proceso con ID " + procesoIdRecibido + " no existe.", List.of()));

        try{
            Resource zip = pdfService.obtenerZipPdfs(
                    procesoIdRecibido,
                    distribucionAceptada,
                    fechaSorteoSenete,
                    fechaSorteoTelebingo
            );

            // Actualizar estado después de éxito
            ProcesoIdService.VerificandoToCompletado(procesoIdRecibido, pdfProcesos);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pdfs-" + procesoIdRecibido + ".zip\"")
                    // Tamaño del archivo para la barra de carga
                    .contentLength(zip.contentLength())
                    .body(zip);
        } catch (IOException e){
            throw new UnprocessableEntityException("Error al procesar el archivo ZIP.", List.of(e.getMessage()));
        }
    }
}