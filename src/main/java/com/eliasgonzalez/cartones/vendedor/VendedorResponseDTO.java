package com.eliasgonzalez.cartones.vendedor;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class VendedorResponseDTO {

    Long id;
    String nombre;
    BigDecimal deuda;

    // Datos de Senete (pueden ser nulos si no existen)
    Integer cantidadSenete;
    Integer resultadoSenete;

    // Datos de Telebingo (pueden ser nulos si no existen)
    Integer cantidadTelebingo;
    Integer resultadoTelebingo;

}