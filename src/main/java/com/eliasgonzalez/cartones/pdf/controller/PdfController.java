package com.eliasgonzalez.cartones.pdf.controller;

import com.eliasgonzalez.cartones.pdf.dto.ConfiguracionPdfDTO;
import com.eliasgonzalez.cartones.pdf.interfaces.IPdfService;
import com.eliasgonzalez.cartones.shared.exception.UnprocessableEntityException;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/vendedores")
@AllArgsConstructor
public class PdfController {

    IPdfService pdfService;

    @PostMapping("/{procesoId}/pdfs")
    public ResponseEntity<Resource> descargarPdfs(@PathVariable(name = "procesoId") String procesoIdRecibido,
                                                  @RequestBody(required = false) ConfiguracionPdfDTO config){

        try{
            if (config == null) {
                config = new ConfiguracionPdfDTO();
            }

            Resource zip = pdfService.obtenerZipPdfs(procesoIdRecibido, config);

            return ResponseEntity.ok()
                    // 1. Tipo de contenido específico
                    .contentType(MediaType.parseMediaType("application/zip"))
                    // 2. Nombre del archivo
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pdfs-" + procesoIdRecibido + ".zip\"")
                    // 3. (Opcional) Tamaño del archivo para la barra de carga
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
