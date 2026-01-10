package com.eliasgonzalez.cartones.vendedor.service;

import com.eliasgonzalez.cartones.vendedor.dto.VendedorResponseDTO;
import com.eliasgonzalez.cartones.vendedor.interfaces.IVendedorService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class MezclarVendedoresService {

    private IVendedorService vendedorService;

    public List<VendedorResponseDTO> mezclarVendedores(){

        List<VendedorResponseDTO> vendedores = vendedorService.listarVendedoresValidos();

        return null;
    }

    private static void mezclarAleatorio () {

    }

    private static boolean isCorrelative () {

        return false;
    }

    private static void findRangeValid(){

    }

}
