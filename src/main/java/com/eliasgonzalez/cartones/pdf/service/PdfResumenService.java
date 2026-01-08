package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.dto.ResumenDTO;
import com.eliasgonzalez.cartones.pdf.enums.PdfEnum;
import com.eliasgonzalez.cartones.shared.exception.PdfCreationException;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfResumenService {

    // Configuración de diseño constante
    private static final float MARGEN = 15;
    private static final float ALTURA_LINEA = 20;
    private static final float ESPACIO_ENTRE_LINEAS = 4;
    private static final float PADDING_DERECHO_NUMEROS = 15f;
    private static final Map<String, Float> POS_COLUMNAS = new HashMap<>();

    static {
        POS_COLUMNAS.put(PdfEnum.VENDEDOR.getValue(), MARGEN + 15);
        POS_COLUMNAS.put(PdfEnum.S_DEL.getValue(), MARGEN + 260);
        POS_COLUMNAS.put(PdfEnum.S_AL.getValue(), MARGEN + 310);
        POS_COLUMNAS.put(PdfEnum.T_DEL.getValue(), MARGEN + 360);
        POS_COLUMNAS.put(PdfEnum.T_AL.getValue(), MARGEN + 410);
        POS_COLUMNAS.put(PdfEnum.CANTIDAD_S.getValue(), MARGEN + 460);
        POS_COLUMNAS.put(PdfEnum.CANTIDAD_T.getValue(), MARGEN + 510);
    }

    public byte[] generarResumen(List<ResumenDTO> vendedores, String fechaPersonalizada) {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            document.open();
            PdfContentByte cb = writer.getDirectContent();

            float width = PageSize.A4.getWidth();
            float height = PageSize.A4.getHeight();

            // Fuentes
            BaseFont helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont helveticaBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

            // Título del documento
            cb.beginText();
            cb.setFontAndSize(helveticaBold, 14);
            cb.showTextAligned(Element.ALIGN_CENTER, "RESUMEN DE ENTREGA - " + fechaPersonalizada, width / 2, height - MARGEN - 15, 0);
            cb.endText();

            // Posición inicial Y para encabezados
            float y = height - MARGEN - 40;

            // Dibujar encabezados iniciales
            dibujarEncabezados(cb, y, width, helveticaBold);

            // Ajustar Y para empezar datos
            y -= (ALTURA_LINEA + ESPACIO_ENTRE_LINEAS + 12);

            // --- CÁLCULO DE LÍMITES DE COLUMNA PARA ALINEACIÓN ---
            // Esto se calcula una vez y se usa dentro del bucle
            float xInicioSenete = POS_COLUMNAS.get(PdfEnum.CANTIDAD_S.getValue());
            float xInicioTelebingo = POS_COLUMNAS.get(PdfEnum.CANTIDAD_T.getValue());
            float xFinTabla = width - MARGEN;

            float anchoColumnaS = xInicioTelebingo - xInicioSenete;
            float anchoColumnaT = xFinTabla - xInicioTelebingo;

            // Posiciones X donde terminan los números (alineados a la derecha con padding)
            float xDataSenete = xInicioSenete + anchoColumnaS - PADDING_DERECHO_NUMEROS;
            float xDataTelebingo = xInicioTelebingo + anchoColumnaT - PADDING_DERECHO_NUMEROS;
            // -----------------------------------------------------

            for (int i = 0; i < vendedores.size(); i++) {
                ResumenDTO row = vendedores.get(i);

                // Sombreado alternado (filas pares visualmente)
                if ((i + 1) % 2 == 0) {
                    cb.saveState();
                    cb.setRGBColorFill(242, 242, 242);
                    cb.rectangle(MARGEN, y - ESPACIO_ENTRE_LINEAS, width - 2 * MARGEN, ALTURA_LINEA + ESPACIO_ENTRE_LINEAS);
                    cb.fill();
                    cb.restoreState();
                }

                // Escribir datos
                cb.beginText();
                cb.setFontAndSize(helvetica, 9);

                // 1. Nombre vendedor
                cb.setTextMatrix(POS_COLUMNAS.get(PdfEnum.VENDEDOR.getValue()), y);
                cb.showText(row.nombre != null ? row.nombre : "");

                // 2. Datos Rangos (Alineados manualmente como en el original)
                cb.showTextAligned(Element.ALIGN_RIGHT, row.seneteDel, POS_COLUMNAS.get(PdfEnum.S_DEL.getValue()) + 15, y, 0);
                cb.showTextAligned(Element.ALIGN_RIGHT, row.seneteAl, POS_COLUMNAS.get(PdfEnum.S_AL.getValue()) + 15, y, 0);
                cb.showTextAligned(Element.ALIGN_RIGHT, row.telebingoDel, POS_COLUMNAS.get(PdfEnum.T_DEL.getValue()) + 15, y, 0);
                cb.showTextAligned(Element.ALIGN_RIGHT, row.telebingoAl, POS_COLUMNAS.get(PdfEnum.T_AL.getValue()) + 15, y, 0);

                // 3. Cantidades (Usando la alineación dinámica calculada)
                cb.showTextAligned(Element.ALIGN_RIGHT, String.valueOf(row.cantidadSenete), xDataSenete, y, 0);
                cb.showTextAligned(Element.ALIGN_RIGHT, String.valueOf(row.cantidadTelebingo), xDataTelebingo, y, 0);

                cb.endText();

                // Avanzar posición Y
                y -= (ALTURA_LINEA + ESPACIO_ENTRE_LINEAS);

                // Verificar salto de página
                if (y < 50) {
                    document.newPage();
                    y = height - MARGEN - 40; // Reiniciar Y

                    // Redibujar encabezados en nueva página
                    y -= 20;
                    dibujarEncabezados(cb, y, width, helveticaBold);
                    y -= (ALTURA_LINEA + ESPACIO_ENTRE_LINEAS + 10);
                }
            }

            document.close();
            return baos.toByteArray();

        } catch (Exception e){
            throw new PdfCreationException("Error al generar el PDF de Resumen", List.of(e.getMessage()));
        }
    }

//    public byte[] generarResumen(List<ResumenDTO> vendedores, String fechaPersonalizada) throws Exception {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        Document document = new Document(PageSize.A4);
//        PdfWriter writer = PdfWriter.getInstance(document, baos);
//
//        document.open();
//        PdfContentByte cb = writer.getDirectContent();
//
//        float width = PageSize.A4.getWidth();
//        float height = PageSize.A4.getHeight();
//
//        // Fuentes
//        BaseFont helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
//        BaseFont helveticaBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
//
//        // Título del documento
//        cb.beginText();
//        cb.setFontAndSize(helveticaBold, 14);
//        cb.showTextAligned(Element.ALIGN_CENTER, "RESUMEN DE ENTREGA - " + fechaPersonalizada, width / 2, height - MARGEN - 15, 0);
//        cb.endText();
//
//        // Posición inicial Y para encabezados
//        float y = height - MARGEN - 40;
//
//        // Dibujar encabezados iniciales
//        dibujarEncabezados(cb, y, width, helveticaBold);
//
//        // Ajustar Y para empezar datos
//        y -= (ALTURA_LINEA + ESPACIO_ENTRE_LINEAS + 12);
//
//        for (int i = 0; i < vendedores.size(); i++) {
//            ResumenDTO row = vendedores.get(i);
//
//            // Sombreado alternado (filas pares en índice base 1 del código original => i % 2 == 1 en índice 0)
//            if ((i + 1) % 2 == 0) {
//                cb.saveState();
//                cb.setRGBColorFill(242, 242, 242); // RGB(0.95) aprox
//                cb.rectangle(MARGEN, y - ESPACIO_ENTRE_LINEAS, width - 2 * MARGEN, ALTURA_LINEA + ESPACIO_ENTRE_LINEAS);
//                cb.fill();
//                cb.restoreState();
//            }
//
//            // Escribir datos
//            cb.beginText();
//            cb.setFontAndSize(helvetica, 9);
//
//            // Nombre vendedor
//            cb.setTextMatrix(POS_COLUMNAS.get(ColumnsResumenEnum.VENDEDOR.getValue()), y);
//            cb.showText(row.nombre != null ? row.nombre : "");
//
//            // Datos Seneté
//            cb.showTextAligned(Element.ALIGN_RIGHT, row.seneteDel, POS_COLUMNAS.get(ColumnsResumenEnum.S_DEL.getValue()) + 15, y, 0);
//            cb.showTextAligned(Element.ALIGN_RIGHT, row.seneteAl, POS_COLUMNAS.get(ColumnsResumenEnum.S_AL.getValue()) + 15, y, 0);
//
//            // Datos Telebingo
//            cb.showTextAligned(Element.ALIGN_RIGHT, row.telebingoDel, POS_COLUMNAS.get(ColumnsResumenEnum.T_DEL.getValue()) + 15, y, 0);
//            cb.showTextAligned(Element.ALIGN_RIGHT, row.telebingoAl, POS_COLUMNAS.get(ColumnsResumenEnum.T_AL.getValue()) + 15, y, 0);
//
//            // Cantidades
//            cb.showTextAligned(Element.ALIGN_RIGHT, String.valueOf(row.cantidadSenete), POS_COLUMNAS.get(ColumnsResumenEnum.CANTIDAD_S.getValue()) + 15, y, 0);
//            cb.showTextAligned(Element.ALIGN_RIGHT, String.valueOf(row.cantidadTelebingo), POS_COLUMNAS.get(ColumnsResumenEnum.CANTIDAD_T.getValue()) + 15, y, 0);
//
//            cb.endText();
//
//            // Avanzar posición Y
//            y -= (ALTURA_LINEA + ESPACIO_ENTRE_LINEAS);
//
//            // Verificar salto de página
//            if (y < 50) {
//                document.newPage();
//                y = height - MARGEN - 40; // Reiniciar Y
//
//                // Redibujar encabezados en nueva página
//                y -= 20;
//                dibujarEncabezados(cb, y, width, helveticaBold);
//                y -= (ALTURA_LINEA + ESPACIO_ENTRE_LINEAS + 10);
//            }
//        }
//
//        document.close();
//        return baos.toByteArray();
//    }

    private void dibujarEncabezados(PdfContentByte cb, float y, float width, BaseFont font) {
        cb.saveState();

        // 1. Líneas
        cb.setLineWidth(1f);
        cb.moveTo(MARGEN, y);
        cb.lineTo(width - MARGEN, y);
        cb.stroke();

        cb.moveTo(MARGEN, y - ALTURA_LINEA - ESPACIO_ENTRE_LINEAS);
        cb.lineTo(width - MARGEN, y - ALTURA_LINEA - ESPACIO_ENTRE_LINEAS);
        cb.stroke();

        // 2. Textos
        cb.beginText();
        cb.setFontAndSize(font, 10);

        cb.setTextMatrix(POS_COLUMNAS.get(PdfEnum.VENDEDOR.getValue()), y - 15);
        cb.showText(PdfEnum.VENDEDOR.getValue().toUpperCase());

        // --- LÓGICA DE CENTRADO DINÁMICO ---
        float xInicioSenete = POS_COLUMNAS.get(PdfEnum.CANTIDAD_S.getValue());
        float xInicioTelebingo = POS_COLUMNAS.get(PdfEnum.CANTIDAD_T.getValue());
        float xFinTabla = width - MARGEN;

        // Anchos de columna
        float anchoColumnaS = xInicioTelebingo - xInicioSenete;
        float anchoColumnaT = xFinTabla - xInicioTelebingo;

        // Centros geométricos
        float centroS = xInicioSenete + (anchoColumnaS / 2);
        float centroT = xInicioTelebingo + (anchoColumnaT / 2);

        // Dibujar encabezados CANTIDAD centrados perfectamente
        cb.showTextAligned(Element.ALIGN_CENTER, "SENETÉ", centroS, y - 15, 0);
        cb.showTextAligned(Element.ALIGN_CENTER, "TELEBINGO", centroT, y - 15, 0);

        // Encabezados RANGOS (Del/Al)
        float xSDel = POS_COLUMNAS.get(PdfEnum.S_DEL.getValue());
        float xSAl = POS_COLUMNAS.get(PdfEnum.S_AL.getValue());
        float centerSeneteGroup = (xSDel + xSAl) / 2; // Centro del grupo Seneté Rangos

        float xTDel = POS_COLUMNAS.get(PdfEnum.T_DEL.getValue());
        float xTAl = POS_COLUMNAS.get(PdfEnum.T_AL.getValue());
        float centerTelebingoGroup = (xTDel + xTAl) / 2; // Centro del grupo Telebingo Rangos

        cb.showTextAligned(Element.ALIGN_CENTER, "SENETÉ", centerSeneteGroup, y - 11, 0);
        cb.showTextAligned(Element.ALIGN_CENTER, "DEL", xSDel, y - 20, 0);
        cb.showTextAligned(Element.ALIGN_CENTER, "AL", xSAl, y - 20, 0);

        cb.showTextAligned(Element.ALIGN_CENTER, "TELEBINGO", centerTelebingoGroup, y - 11, 0);
        cb.showTextAligned(Element.ALIGN_CENTER, "DEL", xTDel, y - 20, 0);
        cb.showTextAligned(Element.ALIGN_CENTER, "AL", xTAl, y - 20, 0);

        cb.endText();
        cb.restoreState();
    }

//    private void dibujarEncabezados(PdfContentByte cb, float y, float width, BaseFont font) {
//        cb.saveState();
//
//        // Líneas
//        cb.setLineWidth(1f);
//        cb.moveTo(MARGEN, y);
//        cb.lineTo(width - MARGEN, y);
//        cb.stroke();
//
//        cb.moveTo(MARGEN, y - ALTURA_LINEA - ESPACIO_ENTRE_LINEAS);
//        cb.lineTo(width - MARGEN, y - ALTURA_LINEA - ESPACIO_ENTRE_LINEAS);
//        cb.stroke();
//
//        // Textos
//        cb.beginText();
//        cb.setFontAndSize(font, 10);
//
//        cb.setTextMatrix(POS_COLUMNAS.get(ColumnsResumenEnum.VENDEDOR.getValue()), y - 15);
//        cb.showText(ColumnsResumenEnum.VENDEDOR.getValue().toUpperCase());
//
//        // Encabezados Seneté
//        float centerSenete = (POS_COLUMNAS.get(ColumnsResumenEnum.S_DEL.getValue()) + POS_COLUMNAS.get(ColumnsResumenEnum.S_AL.getValue())) / 2;
//        cb.showTextAligned(Element.ALIGN_CENTER, "SENETÉ", centerSenete, y - 11, 0);
//        cb.showTextAligned(Element.ALIGN_CENTER, "DEL", POS_COLUMNAS.get(ColumnsResumenEnum.S_DEL.getValue()), y - 20, 0);
//        cb.showTextAligned(Element.ALIGN_CENTER, "AL", POS_COLUMNAS.get(ColumnsResumenEnum.S_AL.getValue()), y - 20, 0);
//
//        // Encabezados Telebingo
//        float centerTelebingo = (POS_COLUMNAS.get(ColumnsResumenEnum.T_DEL.getValue()) + POS_COLUMNAS.get(ColumnsResumenEnum.T_AL.getValue())) / 2;
//        cb.showTextAligned(Element.ALIGN_CENTER, "TELEBINGO", centerTelebingo, y - 11, 0);
//        cb.showTextAligned(Element.ALIGN_CENTER, "DEL", POS_COLUMNAS.get(ColumnsResumenEnum.T_DEL.getValue()), y - 20, 0);
//        cb.showTextAligned(Element.ALIGN_CENTER, "AL", POS_COLUMNAS.get(ColumnsResumenEnum.T_AL.getValue()), y - 20, 0);
//
//        cb.showTextAligned(Element.ALIGN_CENTER, "      SENETÉ  ", POS_COLUMNAS.get(ColumnsResumenEnum.CANTIDAD_S.getValue()), y - 15, 0);
//        cb.showTextAligned(Element.ALIGN_CENTER, "        TELEBINGO", POS_COLUMNAS.get(ColumnsResumenEnum.CANTIDAD_T.getValue()), y - 15, 0);
//
//        cb.endText();
//        cb.restoreState();
//    }
}
