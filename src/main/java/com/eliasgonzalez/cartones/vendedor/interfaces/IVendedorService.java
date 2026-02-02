package com.eliasgonzalez.cartones.vendedor.interfaces;

import com.eliasgonzalez.cartones.vendedor.dto.VendedorResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IVendedorService {

    // GET
    List<VendedorResponseDTO> listaVendedores ();

    List<VendedorResponseDTO> listarVendedoresValidos (String procesoIdRecibido);

    // POST
    void procesarExcel(MultipartFile file, String procesoIdCreado);

    // Metodo para crear el ProcesoId
    String iniciarProceso ();

}
