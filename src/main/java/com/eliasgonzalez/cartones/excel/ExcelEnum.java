package com.eliasgonzalez.cartones.excel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExcelEnum {

    HOJA_SISTEMA_ETIQUETAS("Sistema_Etiquetas"),
    VENDEDOR("Vendedores"),
    CANT_SENETE("Cantidad_Senete"),
    CANT_TELEBINGO("Cantidad_Telebingo"),
    RESULT_SENETE("Resultados_Senete"),
    RESULT_TELEBINGO("Resultados_Telebingo"),
    SALDO("Saldo");

    private final String value;
}
