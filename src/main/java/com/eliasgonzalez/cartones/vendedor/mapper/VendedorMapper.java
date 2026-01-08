package com.eliasgonzalez.cartones.vendedor.mapper;

import com.eliasgonzalez.cartones.vendedor.entity.Vendedor;
import com.eliasgonzalez.cartones.vendedor.dto.VendedorResponseDTO;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VendedorMapper {

    // Constructor privado para evitar que se instancie esta clase de utilidad
    private VendedorMapper() {}

    public static VendedorResponseDTO toVendedorResponseDTO(Vendedor vendedor) {
        if (vendedor == null) {
            return null;
        }

        return VendedorResponseDTO
                .builder()
                .id(vendedor.getId())
                .nombre(vendedor.getNombre())
                .deuda(vendedor.getDeuda())
                .cantidadSenete(vendedor.getCantidadSenete())
                .resultadoSenete(vendedor.getResultadoSenete())
                .cantidadTelebingo(vendedor.getCantidadTelebingo())
                .resultadoTelebingo(vendedor.getResultadoTelebingo())
                .build();
    }

    public static List<VendedorResponseDTO> toVendedorResponseDTOs(List<Vendedor> vendedores) {
        if (vendedores == null || vendedores.isEmpty()) {
            return Collections.emptyList();
        }

        // Streams para mapear de forma limpia y concisa
        return vendedores.stream()
                .map(VendedorMapper::toVendedorResponseDTO) // Reutilizamos la lógica del método anterior
                .collect(Collectors.toList());
    }
}