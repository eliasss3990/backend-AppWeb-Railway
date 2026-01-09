package com.eliasgonzalez.cartones.pdf.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EstadoEnum {

    PENDIENTE("pendiente"),
    COMPLETADO("completado"),
    ERROR("error");

    private final String value;
}
