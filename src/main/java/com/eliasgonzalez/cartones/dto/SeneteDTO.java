package com.eliasgonzalez.cartones.dto;

import com.eliasgonzalez.cartones.model.Vendedor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class SeneteDTO {

    private Vendedor vendedor;
    private Integer cantidadSenete;
    private Integer resultadoSenete;

}
