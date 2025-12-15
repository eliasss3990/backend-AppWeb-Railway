package com.eliasgonzalez.cartones.vendedor.mapper;

import com.eliasgonzalez.cartones.bingo.Senete;
import com.eliasgonzalez.cartones.bingo.Telebingo;
import com.eliasgonzalez.cartones.vendedor.Vendedor;
import com.eliasgonzalez.cartones.vendedor.VendedorResponseDTO;

public class Mapper {

    // Mapeo de Vendedor a VendedorResponseDTO
    public static VendedorResponseDTO toVendedorResponseDTO(Vendedor vendedor, Senete senete, Telebingo telebingo) {
        VendedorResponseDTO dto = new VendedorResponseDTO();
        dto.setId(vendedor.getId());
        dto.setNombre(vendedor.getNombre());
        dto.setDeuda(vendedor.getDeuda());
        dto.setCantidadSenete(senete.getCantidadSenete());
        dto.setResultadoSenete(senete.getResultadoSenete());
        dto.setCantidadTelebingo(telebingo.getCantidadTelebingo());
        dto.setResultadoTelebingo(telebingo.getResultadoTelebingo());
        return dto;
    }
}
