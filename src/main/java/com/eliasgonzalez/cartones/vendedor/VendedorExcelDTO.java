package com.eliasgonzalez.cartones.vendedor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO intermedio para transportar los datos leídos de una fila de Excel.
 */
@Getter
@Setter
@AllArgsConstructor
public class VendedorExcelDTO {

    // Información de seguimiento para errores
    private int filaActual;

    // Datos del Vendedor
    private final String nombre;
    private final String deudaStr;

    // Datos de Senete
    private final Integer cantidadSenete;
    private final Integer resultadoSenete;

    // Datos de Telebingo
    private final Integer cantidadTelebingo;
    private final Integer resultadoTelebingo;
}
