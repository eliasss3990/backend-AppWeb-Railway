package com.eliasgonzalez.cartones.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class VendedorDTO {

    private String nombre;
    private BigDecimal deuda;
}
