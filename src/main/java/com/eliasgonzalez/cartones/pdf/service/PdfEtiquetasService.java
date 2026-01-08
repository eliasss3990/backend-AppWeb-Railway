package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.dto.EtiquetaDTO;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfEtiquetasService {

    public byte[] generarEtiquetas(List<EtiquetaDTO> etiquetas, String fechaPersonalizada) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        document.open();
        PdfContentByte cb = writer.getDirectContent();

        // Configuración de dimensiones
        float width = PageSize.A4.getWidth();
        float height = PageSize.A4.getHeight();
        float margen = 20;
        float espacioVertical = 15;
        float altoEtiqueta = (height - 2 * margen - 2 * espacioVertical) / 3;
        float anchoMitad = (width - 2 * margen) / 2;

        // Fuentes
        BaseFont helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        BaseFont helveticaBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

        for (int i = 0; i < etiquetas.size(); i++) {
            EtiquetaDTO item = etiquetas.get(i);

            // Cálculo de Y (invertido en lógica visual, pero igual a coordenadas PDF)
            float y = height - margen - ((i % 3) + 1) * altoEtiqueta - (i % 3) * espacioVertical;

            // Dibujar recuadro
            cb.setLineWidth(0.8f);
            cb.rectangle(margen, y, width - 2 * margen, altoEtiqueta);
            cb.stroke();

            // Número de orden (#1, #2...)
            cb.beginText();
            cb.setFontAndSize(helveticaBold, 24);
            cb.setTextMatrix(width - margen - 45, y + altoEtiqueta - 245); // Ajuste manual de coordenadas
            cb.showText("#" + item.numeroVendedor);
            cb.endText();

            // Encabezado
            cb.beginText();
            cb.setFontAndSize(helveticaBold, 10);
            cb.setTextMatrix(margen + 10, y + altoEtiqueta - 20);
            cb.showText("ROBERTO GONZÁLEZ");
            cb.setTextMatrix(margen + 10, y + altoEtiqueta - 35);
            cb.showText("DIST - ITAUGUÁ - PY");
            cb.setTextMatrix(margen + 10, y + altoEtiqueta - 50);
            cb.showText("0983 433572");
            cb.endText();

            // Línea separadora encabezado
            cb.moveTo(margen, y + altoEtiqueta - 60);
            cb.lineTo(width - margen, y + altoEtiqueta - 60);
            cb.stroke();

            // Fecha (Alineada a la derecha)
            cb.beginText();
            cb.setFontAndSize(helveticaBold, 12);
            cb.showTextAligned(Element.ALIGN_RIGHT, "Fecha del sorteo: " + fechaPersonalizada, width - margen - 10, y + altoEtiqueta - 35, 0);
            cb.endText();

            // Nombre del vendedor (Centrado)
            cb.beginText();
            cb.setFontAndSize(helveticaBold, 16);
            String nombreMostrar = item.nombre != null ? item.nombre.toUpperCase() : "";
            cb.showTextAligned(Element.ALIGN_CENTER, nombreMostrar, width / 2, y + altoEtiqueta - 85, 0);
            cb.endText();

            // Sección SENETÉ
            cb.beginText();
            cb.setFontAndSize(helveticaBold, 12);
            cb.setTextMatrix(margen + 30, y + altoEtiqueta - 110);
            cb.showText("SENETÉ:");
            cb.setFontAndSize(helvetica, 11);
            cb.setTextMatrix(margen + 30, y + altoEtiqueta - 125);
            cb.showText(item.seneteRango + " (" + item.seneteCartones + " cartones)");
            cb.endText();

            // Sección TELEBINGO
            cb.beginText();
            cb.setFontAndSize(helveticaBold, 12);
            cb.setTextMatrix(margen + 30 + anchoMitad, y + altoEtiqueta - 110);
            cb.showText("TELEBINGO:");
            cb.setFontAndSize(helvetica, 11);
            cb.setTextMatrix(margen + 30 + anchoMitad, y + altoEtiqueta - 125);
            cb.showText(item.telebingoRango + " (" + item.telebingoCartones + " cartones)");
            cb.endText();

            // Títulos RESULTADOS
            cb.beginText();
            cb.setFontAndSize(helveticaBold, 12);
            cb.setTextMatrix(margen + 30, y + altoEtiqueta - 160);
            cb.showText("RESULTADOS:");
            cb.setTextMatrix(margen + 30 + anchoMitad, y + altoEtiqueta - 160);
            cb.showText("RESULTADOS:");
            cb.endText();

            // Valores RESULTADOS
            cb.beginText();
            cb.setFontAndSize(helvetica, 10);
            cb.setTextMatrix(margen + 30, y + altoEtiqueta - 175);
            cb.showText(item.resultadoSenete);
            cb.setTextMatrix(margen + 30 + anchoMitad, y + altoEtiqueta - 175);
            cb.showText(item.resultadoTelebingo);
            cb.endText();

            // SALDO
            cb.beginText();
            cb.setFontAndSize(helveticaBold, 12);
            cb.setTextMatrix(margen + 30, y + altoEtiqueta - 217);
            cb.showText("SALDO:");
            cb.setFontAndSize(helvetica, 11);
            cb.setTextMatrix(margen + 90, y + altoEtiqueta - 217);
            cb.showText(item.saldo);
            cb.endText();

            // Paginación: Si es múltiplo de 3 y no es el último elemento
            if ((i + 1) % 3 == 0 && (i + 1) < etiquetas.size()) {
                document.newPage();
            }
        }

        document.close();
        return baos.toByteArray();
    }
}
