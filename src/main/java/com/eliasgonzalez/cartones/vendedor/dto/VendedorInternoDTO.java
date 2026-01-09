package com.eliasgonzalez.cartones.vendedor.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class VendedorInternoDTO {

    private String nombre;
    private Integer cantidadCartones;
    private Integer inicioSenete;
    private Integer finSenete;
    private Integer resultadoSenete;
    private Integer inicioTelebingo;
    private Integer finTelebingo;
    private Integer resultadoTelebingo;
    private String Saldo;

}
