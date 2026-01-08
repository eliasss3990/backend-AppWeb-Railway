package com.eliasgonzalez.cartones.pdf.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class EtiquetaDTO {

    public int numeroVendedor;
    public String nombre;
    public String seneteRango;
    public String seneteCartones;
    public String telebingoRango;
    public String telebingoCartones;
    public String resultadoSenete;
    public String resultadoTelebingo;
    public String saldo;

}