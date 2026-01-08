package com.eliasgonzalez.cartones.pdf.controller;

import com.eliasgonzalez.cartones.pdf.interfaces.IPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/vendedores")
public class PdfController {

    @Autowired
    IPdfService pdfService;

    @GetMapping("/{procesoId}/pdfs")
    public ResponseEntity<Resource> descargarPdfs(@PathVariable String procesoId) throws IOException {

        Resource zip = pdfService.obtenerZipPdfs(procesoId);

        return ResponseEntity.ok()
                // 1. Tipo de contenido específico
                .contentType(MediaType.parseMediaType("application/zip"))
                // 2. Nombre del archivo
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pdfs-" + procesoId + ".zip\"")
                // 3. (Opcional) Tamaño del archivo para la barra de carga
                .contentLength(zip.contentLength())
                .body(zip);
    }

}
