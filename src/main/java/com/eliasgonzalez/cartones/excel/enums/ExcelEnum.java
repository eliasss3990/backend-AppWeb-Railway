package com.eliasgonzalez.cartones.excel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum para representar los datos del archivo Excel.
 * No es necesario que los nombres estén exactamente igual que en el excel, en términos de mayúsculas y minúsculas.
 */
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
