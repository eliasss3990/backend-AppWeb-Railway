package com.eliasgonzalez.cartones.pdf.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PdfEnum {

    VENDEDOR("vendedor"),
    S_DEL("s_del"),
    S_AL("s_al"),
    T_DEL("t_del"),
    T_AL("t_al"),
    CANTIDAD_S("cantidad_S"),
    CANTIDAD_T("cantidad_T");

    private final String value;

}
