package com.eliasgonzalez.cartones.pdf.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class ResumenDTO {

    public String nombre;
    public String seneteDel;
    public String seneteAl;
    public String telebingoDel;
    public String telebingoAl;
    public int cantidadSenete;
    public int cantidadTelebingo;

}