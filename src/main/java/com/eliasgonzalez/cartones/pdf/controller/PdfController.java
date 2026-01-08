package com.eliasgonzalez.cartones.pdf.controller;

import com.eliasgonzalez.cartones.pdf.interfaces.IPdfService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vendedores")
public class PdfController {

    @Autowired
    IPdfService pdfService;

    @GetMapping("/{procesoId}/pdfs")
    public ResponseEntity<Resource> descargarPdfs(@PathVariable String procesoId) {

        Resource zip = pdfService.obtenerZipPdfs(procesoId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"pdfs-" + procesoId + ".zip\""
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }

}
