package com.eliasgonzalez.cartones.pdf.interfaces;

import com.eliasgonzalez.cartones.pdf.dto.VendedorSimuladoDTO;
import org.springframework.core.io.Resource;

import java.time.LocalDate;
import java.util.List;

public interface IPdfService {

    Resource obtenerZipPdfs (
            String procesoIdRecibido,
            List<VendedorSimuladoDTO> config,
            LocalDate fechaSorteoSenete,
            LocalDate fechaSorteoTelebingo
    );

}
