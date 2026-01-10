package com.eliasgonzalez.cartones.pdf.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class EtiquetaDTO {

    private int numeroVendedor;
    private String seneteRango;
    private String nombre;
    private String seneteCartones;
    private String telebingoRango;
    private String telebingoCartones;
    private String resultadoSenete;
    private String resultadoTelebingo;
    private String saldo;
    private List<String> fechaSorteo;

}