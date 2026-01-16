package com.eliasgonzalez.cartones.pdf.mapper;

import com.eliasgonzalez.cartones.pdf.dto.EtiquetaDTO;
import com.eliasgonzalez.cartones.pdf.dto.ResumenDTO;
import com.eliasgonzalez.cartones.pdf.dto.VendedorSimuladoDTO;
import com.eliasgonzalez.cartones.vendedor.entity.Vendedor;
import com.eliasgonzalez.cartones.vendedor.interfaces.VendedorRepository;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class PdfMapper {

    private final VendedorRepository vendedorRepo;

    public List<EtiquetaDTO> toEtiquetaDTOs(VendedorSimuladoDTO vendedorSimuladoDTO, Vendedor v) {

        return null;
    }

    public List<ResumenDTO> toResumenDTOs() {
        return null;
    }
}
