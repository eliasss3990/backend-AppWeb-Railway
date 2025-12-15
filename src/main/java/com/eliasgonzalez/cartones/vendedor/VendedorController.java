package com.eliasgonzalez.cartones.vendedor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class VendedorController {

    @Autowired
    private IVendedorService vendedorService;

    @GetMapping("/vendedores")
    public ResponseEntity<List<VendedorResponseDTO>> listarVendedores (){
        return ResponseEntity.ok(vendedorService.listaVendedores());
    }

    // Agregar lo de fecha de sorteo
}
