package com.eliasgonzalez.cartones.pdf.controller;

import com.eliasgonzalez.cartones.pdf.dto.ConfiguracionPdfDTO;
import com.eliasgonzalez.cartones.pdf.interfaces.IPdfService;
import com.eliasgonzalez.cartones.shared.util.Util;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/vendedores")
@AllArgsConstructor
public class PdfController {

    IPdfService pdfService;

    @GetMapping("/{procesoId}/pdfs")
    public ResponseEntity<Resource> descargarPdfs(@PathVariable String procesoIdRecibido,
                                                  @RequestBody ConfiguracionPdfDTO config) throws IOException {

        Resource zip = pdfService.obtenerZipPdfs(procesoIdRecibido, config);

        return ResponseEntity.ok()
                // 1. Tipo de contenido específico
                .contentType(MediaType.parseMediaType("application/zip"))
                // 2. Nombre del archivo
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pdfs-" + procesoIdRecibido + ".zip\"")
                // 3. (Opcional) Tamaño del archivo para la barra de carga
                .contentLength(zip.contentLength())
                .body(zip);
    }

}
