package com.eliasgonzalez.cartones.pdf.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class EtiquetaDTO {

    private int numeroVendedor;
    private String nombre;
    private String saldo;

    private List<String> seneteRango;
    private String seneteCartones;
    private String resultadoSenete;
    private String fechaSorteoSenete;

    private List<String> telebingoRango;
    private String telebingoCartones;
    private String resultadoTelebingo;
    private String fechaSorteoTelebingo;

}