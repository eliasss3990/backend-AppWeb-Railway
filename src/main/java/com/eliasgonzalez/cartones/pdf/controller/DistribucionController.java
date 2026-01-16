package com.eliasgonzalez.cartones.pdf.controller;

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
import java.util.List;

@RestController
@RequestMapping("/api/vendedores")
@RequiredArgsConstructor
public class DistribucionController {

    private final DistribucionService distribucionService;
    private final IPdfService pdfService;
    private final PdfProcesosRepository pdfProcesosRepo;

    /**
     * Endpoint 1: SIMULAR / MEZCLAR
     * Recibe la configuración y los inputs del usuario, devuelve la tabla simulada.
     * NO guarda en BD.
     */
    @PostMapping("/{procesoId}/distribucion/simular")
    public ResponseEntity<List<VendedorSimuladoDTO>> simularDistribucion(
            @RequestBody SimulacionRequestDTO request,
            @PathVariable String procesoIdRecibido) {

        // Verificamos el procesoId
        PdfProcesos pdfProcesos = pdfProcesosRepo.findById(procesoIdRecibido)
                .orElseThrow(() -> new ResourceNotFoundException("El proceso con ID " + procesoIdRecibido + " no existe.", null));

        ProcesoIdService.PendienteToVerificando(procesoIdRecibido, pdfProcesos);

        // Pasamos el procesoIdRecibido al servicio por si necesita validar o buscar datos originales
        List<VendedorSimuladoDTO> simulacion = distribucionService.simularDistribucion(request);

        return ResponseEntity.ok(simulacion);
    }

    /**
     * Endpoint 2: CREAR PDF
     * El usuario aceptó la tabla visualizada. Aquí se generan y guardan los PDFs en BD.
     */
    @PostMapping("/{procesoId}/distribucion/crear-pdf")
    public ResponseEntity<Resource> guardarDistribucion(@RequestBody SimulacionRequestDTO distribucionAceptada,
                                                        @PathVariable(name = "procesoId") String procesoIdRecibido){

        // Validamos que la lista no venga vacía
        if (distribucionAceptada == null) {
            distribucionAceptada = new SimulacionRequestDTO();
        }

        PdfProcesos pdfProcesos = pdfProcesosRepo.findById(procesoIdRecibido)
                .orElseThrow(() -> new ResourceNotFoundException("El proceso con ID " + procesoIdRecibido + " no existe.", null));

        ProcesoIdService.VerificandoToCompletado(procesoIdRecibido, pdfProcesos);

        try{
            Resource zip = pdfService.obtenerZipPdfs(procesoIdRecibido, distribucionAceptada);

            return ResponseEntity.ok()
                    // 1. Tipo de contenido específico
                    .contentType(MediaType.parseMediaType("application/zip"))
                    // 2. Nombre del archivo
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pdfs-" + procesoIdRecibido + ".zip\"")
                    // 3. Tamaño del archivo para la barra de carga
                    .contentLength(zip.contentLength())
                    .body(zip);
        } catch (IOException e){
            throw new UnprocessableEntityException("Error al procesar el archivo ZIP.", List.of(e.getMessage()));
        } catch (Exception e) {
            System.out.println("LOG DE PRUEBA: " + e.getMessage());
            throw new UnprocessableEntityException("Error al procesar.", List.of(e.getMessage()));
        }
    }
}