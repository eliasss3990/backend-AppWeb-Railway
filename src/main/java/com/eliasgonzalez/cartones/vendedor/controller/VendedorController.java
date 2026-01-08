package com.eliasgonzalez.cartones.vendedor.controller;

import com.eliasgonzalez.cartones.vendedor.dto.VendedorResponseDTO;
import com.eliasgonzalez.cartones.vendedor.interfaces.IVendedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/vendedores")
public class VendedorController {

    @Autowired
    private IVendedorService vendedorService;

    @GetMapping
    public ResponseEntity<List<VendedorResponseDTO>> listarVendedores (){
        return ResponseEntity.ok(vendedorService.listaVendedores());
    }

    @GetMapping("/validos")
    public ResponseEntity<List<VendedorResponseDTO>> listarVendedoresValidos (){

        return ResponseEntity.ok(vendedorService.listarVendedoresValidos());
    }

    @DeleteMapping("/deleteAll")
    public ResponseEntity<Void> eliminarTodosLosVendedores() {
        vendedorService.eliminarTodosLosVendedores();
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> cargarVendedoresDesdeExcel(@RequestParam("file") MultipartFile file,
                                                             @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSorteo) {
        String procesoIdCreado = vendedorService.iniciarProceso();

        vendedorService.procesarExcel(file, fechaSorteo, procesoIdCreado);


        return ResponseEntity.ok(procesoIdCreado);
    }


}
