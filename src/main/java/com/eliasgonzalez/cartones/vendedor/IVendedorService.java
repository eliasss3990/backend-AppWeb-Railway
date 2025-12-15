package com.eliasgonzalez.cartones.vendedor;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IVendedorService {

    // GET
    List<VendedorResponseDTO> listaVendedores ();

    // POST
    void LeerExcel(MultipartFile file);
}
