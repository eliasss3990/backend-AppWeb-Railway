package com.eliasgonzalez.cartones.pdf.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class ConfiguracionPdfDTO {

    private Integer InicioSeneteGral;
    private Integer InicioTelebingoGral;
    private LocalDate fechaSorteo;
    private List<RangoCortadoDTO> rangosCortados;

}
