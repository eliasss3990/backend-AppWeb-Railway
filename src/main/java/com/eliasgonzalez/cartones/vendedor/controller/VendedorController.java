package com.eliasgonzalez.cartones.vendedor.controller;

import com.eliasgonzalez.cartones.shared.util.Util;
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

    // TODO: eliminar luego de pruebas
    @GetMapping
    public ResponseEntity<List<VendedorResponseDTO>> listarVendedores (){
        return ResponseEntity.ok(vendedorService.listaVendedores());
    }

    @GetMapping("/validos")
    public ResponseEntity<List<VendedorResponseDTO>> listarVendedoresValidos (){
        List<VendedorResponseDTO> vendedores = vendedorService.listarVendedoresValidos();
        vendedorService.eliminarTodosLosVendedores();
        return ResponseEntity.ok(vendedores);
    }

    @DeleteMapping("/deleteAll")
    public ResponseEntity<Void> eliminarTodosLosVendedores() {
        vendedorService.eliminarTodosLosVendedores();
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> cargarVendedoresDesdeExcel(
            @RequestParam("file") MultipartFile file) {


        String procesoIdCreado = vendedorService.iniciarProceso();
        vendedorService.procesarExcel(file);

        return ResponseEntity.ok(procesoIdCreado);
    }


}
