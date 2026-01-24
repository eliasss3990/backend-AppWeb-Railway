package com.eliasgonzalez.cartones.pdf.mapper;

import com.eliasgonzalez.cartones.pdf.dto.EtiquetaDTO;
import com.eliasgonzalez.cartones.pdf.dto.ResumenDTO;
import com.eliasgonzalez.cartones.pdf.dto.VendedorSimuladoDTO;
import com.eliasgonzalez.cartones.vendedor.entity.Vendedor;

import java.util.*;

public class PdfMapper {

    public static List<EtiquetaDTO> toEtiquetaDTOs(
            List<VendedorSimuladoDTO> vendedorSimuladoDTOs,
            Map<Long, Vendedor> vendedoresMap
    ) {
        List<EtiquetaDTO> etiquetaDTOs = new ArrayList<>();

        for (int i = 0; i < vendedorSimuladoDTOs.size(); i++){
            VendedorSimuladoDTO simulado = vendedorSimuladoDTOs.get(i);

            // Recuperamos la entidad real usando el ID del DTO
            Vendedor vendedor = vendedoresMap.get(simulado.getId());

            // Validación de seguridad por si no existe el ID en el mapa (evita NullPointerException)
            String cantSenete = (vendedor != null && vendedor.getCantidadSenete() != null) ? vendedor.getCantidadSenete().toString() : "0";
            String resSenete = (vendedor != null && vendedor.getResultadoSenete() != null) ? vendedor.getResultadoSenete().toString() : "0";
            String cantTelebingo = (vendedor != null && vendedor.getCantidadTelebingo() != null) ? vendedor.getCantidadTelebingo().toString() : "0";
            String resTelebingo = (vendedor != null && vendedor.getResultadoTelebingo() != null) ? vendedor.getResultadoTelebingo().toString() : "0";

            etiquetaDTOs.add(EtiquetaDTO.builder()
                    .numeroVendedor(i + 1)
                    .nombre(simulado.getNombre())
                    .saldo(simulado.getDeuda())

                    // Datos Seneté
                    .seneteRangos(simulado.getRangosSenete())
                    .seneteCartones(cantSenete)
                    .resultadoSenete(resSenete)

                    // Datos Telebingo
                    .telebingoRangos(simulado.getRangosTelebingo())
                    .telebingoCartones(cantTelebingo)
                    .resultadoTelebingo(resTelebingo)

                    .build()
            );
        }
        return etiquetaDTOs;
    }

    public static List<ResumenDTO> toResumenDTOs(
            List<VendedorSimuladoDTO> vendedorSimuladoDTOs,
            Map<Long, Vendedor> vendedoresMap
    ) {

        List<ResumenDTO> resumenDTOs = new ArrayList<>();

        for (int i = 0; i < vendedorSimuladoDTOs.size(); i++){
            VendedorSimuladoDTO simulado = vendedorSimuladoDTOs.get(i);

            // Recuperamos la entidad real
            Vendedor vendedor = vendedoresMap.get(simulado.getId());

            // Validación de nulos para enteros
            int cantSenete = (vendedor != null && vendedor.getCantidadSenete() != null) ? vendedor.getCantidadSenete() : 0;
            int cantTelebingo = (vendedor != null && vendedor.getCantidadTelebingo() != null) ? vendedor.getCantidadTelebingo() : 0;

            Map<String, String> rangosSenete = extraerRangos(simulado.getRangosSenete());
            Map<String, String> rangosTelebingo = extraerRangos(simulado.getRangosTelebingo());

            resumenDTOs.add(ResumenDTO.builder()
                    .numeroVendedor(i + 1)
                    .nombre(simulado.getNombre())

                    .seneteDelAl(rangosSenete)
                    .cantidadSenete(cantSenete)

                    .telebingoDelAl(rangosTelebingo)
                    .cantidadTelebingo(cantTelebingo)

                    .build()
            );
        }

        return resumenDTOs;
    }

    private static Map<String, String> extraerRangos (List<String> rangos){
        Map<String, String> inicioFinRango = new HashMap<>();
        if (rangos == null) return inicioFinRango;

        for (String rango : rangos){
            String[] x = obtenerExtremos(rango);
            if (x.length >= 2) {
                inicioFinRango.put(x[0], x[1]);
            }
        }
        return inicioFinRango;
    }

    private static String[] obtenerExtremos(String rango){
        return rango.split("\\s*-\\s*");
    }
}