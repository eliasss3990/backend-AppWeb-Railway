package com.eliasgonzalez.cartones.vendedor.interfaces;

import com.eliasgonzalez.cartones.vendedor.dto.VendedorResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface IVendedorService {

    // GET
    List<VendedorResponseDTO> listaVendedores ();

    List<VendedorResponseDTO> listarVendedoresValidos ();

    // DELETE
    void eliminarTodosLosVendedores ();

    // POST
    void procesarExcel(MultipartFile file);

    // Metodo para crear el ProcesoId
    String iniciarProceso ();

}
