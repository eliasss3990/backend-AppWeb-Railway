package com.eliasgonzalez.cartones.pdf.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendedorInputDTO {

    private Long id;
    private String nombre;

    // Configuración Senete
    private Integer cantidadSenete;
    private Integer terminacionSenete; // Puede ser null

    // Configuración Telebingo
    private Integer cantidadTelebingo;
    private Integer terminacionTelebingo; // Puede ser null

}