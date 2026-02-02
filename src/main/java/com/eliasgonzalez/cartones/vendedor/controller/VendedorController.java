package com.eliasgonzalez.cartones.vendedor.controller;

import com.eliasgonzalez.cartones.vendedor.dto.VendedorResponseDTO;
import com.eliasgonzalez.cartones.vendedor.interfaces.IVendedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vendedores")
@CrossOrigin(origins = "${app.cors.origins}")
public class VendedorController {

    private final IVendedorService vendedorService;

    @GetMapping("/{procesoId}")
    public ResponseEntity<List<VendedorResponseDTO>> listarVendedoresValidos (
            @PathVariable (name = "procesoId") String procesoIdRecibido
    ){

        return ResponseEntity.ok(vendedorService.listarVendedoresValidos(procesoIdRecibido));
    }

    @PostMapping(value = "/carga", consumes = "multipart/form-data")
    public ResponseEntity<String> cargarVendedoresDesdeExcel(
            @RequestParam("file") MultipartFile file) {


        String procesoIdCreado = vendedorService.iniciarProceso();
        vendedorService.procesarExcel(file, procesoIdCreado);

        return ResponseEntity.ok(procesoIdCreado);
    }


}
