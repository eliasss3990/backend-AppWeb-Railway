package com.eliasgonzalez.cartones.pdf.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class RangoCortadoDTO {

    private int inicio;
    private int fin;

    // Validación lógica simple
    public boolean isValido() {
        return inicio > 0 && fin > 0 && inicio <= fin;
    }
}
