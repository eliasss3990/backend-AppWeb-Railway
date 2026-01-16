package com.eliasgonzalez.cartones.vendedor.controller;

import com.eliasgonzalez.cartones.vendedor.dto.VendedorResponseDTO;
import com.eliasgonzalez.cartones.vendedor.interfaces.IVendedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/vendedores")
public class VendedorController {

    @Autowired
    private IVendedorService vendedorService;

    // TODO: eliminar luego de pruebas
    @GetMapping("/all")
    public ResponseEntity<List<VendedorResponseDTO>> listarVendedores (){
        return ResponseEntity.ok(vendedorService.listaVendedores());
    }

    @GetMapping
    public ResponseEntity<List<VendedorResponseDTO>> listarVendedoresValidos (){
        List<VendedorResponseDTO> vendedores = vendedorService.listarVendedoresValidos();
        return ResponseEntity.ok(vendedores);
    }

    @DeleteMapping("/deleteAll")
    public ResponseEntity<Void> eliminarTodosLosVendedores() {
        vendedorService.eliminarTodosLosVendedores();
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/carga", consumes = "multipart/form-data")
    public ResponseEntity<String> cargarVendedoresDesdeExcel(
            @RequestParam("file") MultipartFile file) {


        String procesoIdCreado = vendedorService.iniciarProceso();
        vendedorService.procesarExcel(file, procesoIdCreado);

        return ResponseEntity.ok(procesoIdCreado);
    }


}
