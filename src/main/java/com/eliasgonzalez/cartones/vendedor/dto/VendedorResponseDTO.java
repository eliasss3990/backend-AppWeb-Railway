package com.eliasgonzalez.cartones.vendedor.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class VendedorResponseDTO {

    private Long id;
    private String nombre;
    private BigDecimal deuda;

    // Datos de Senete (pueden ser nulos si no existen)
    private Integer cantidadSenete;
    private Integer resultadoSenete;

    @JsonIgnore
    private Integer inicioSenete;

    @JsonIgnore
    private Integer finSenete;

    // Datos de Telebingo (pueden ser nulos si no existen)
    private Integer cantidadTelebingo;
    private Integer resultadoTelebingo;

    @JsonIgnore
    private Integer inicioTelebingo;

    @JsonIgnore
    private Integer finTelebingo;

    public String getRangoSenete() {
        if (inicioSenete == null || finSenete == null || cantidadSenete == null) {
            return "Datos incompletos";
        }
        return String.format("%s-%s (%s)", inicioSenete, finSenete, cantidadSenete);
    }

    public String getRangoTelebingo() {
        if (inicioTelebingo == null || finTelebingo == null || cantidadTelebingo == null) {
            return "Datos incompletos";
        }
        return String.format("%s-%s (%s)", inicioTelebingo, finTelebingo, cantidadTelebingo);
    }

}