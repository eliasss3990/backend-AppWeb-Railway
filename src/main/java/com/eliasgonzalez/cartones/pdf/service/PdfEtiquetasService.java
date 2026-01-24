package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.dto.EtiquetaDTO;
import com.eliasgonzalez.cartones.shared.exception.PdfCreationException;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class PdfEtiquetasService {

    public byte[] generarEtiquetas(List<EtiquetaDTO> etiquetas, LocalDate fechaSenete, LocalDate fechaTelebingo) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            document.open();
            PdfContentByte cb = writer.getDirectContent();

            // --- CONFIGURACIÓN DE DIMENSIONES ---
            float width = PageSize.A4.getWidth();
            float height = PageSize.A4.getHeight();
            float margen = 20;
            float espV = 15; // Espacio vertical entre etiquetas
            float altoEt = (height - 2 * margen - 2 * espV) / 3;
            float mitad = (width - 2 * margen) / 2;

            // --- FUENTES ---
            BaseFont helv = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont bold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

            // --- PREPARACIÓN DE FECHAS ---
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String txtFechaSenete = fechaSenete.format(fmt);
            String txtFechaTelebingo = fechaTelebingo.format(fmt);
            boolean fechasIguales = fechaSenete.isEqual(fechaTelebingo);

            for (int i = 0; i < etiquetas.size(); i++) {
                EtiquetaDTO item = etiquetas.get(i);

                // Cálculo de la posición Y base de la etiqueta actual (invertida)
                float y = height - margen - ((i % 3) + 1) * altoEt - (i % 3) * espV;

                // 1. RECUADRO EXTERIOR
                cb.setLineWidth(1f);
                cb.rectangle(margen, y, width - 2 * margen, altoEt);
                cb.stroke();

                // 2. NÚMERO DE VENDEDOR (Esquina Superior Derecha)
                cb.beginText();
                cb.setFontAndSize(bold, 24);
                // Pegado al margen derecho (margen - 5)
                cb.showTextAligned(Element.ALIGN_RIGHT, "#" + item.getNumeroVendedor(), width - margen - 5, y + altoEt - 25, 0);
                cb.endText();

                // 3. CABECERA IZQUIERDA (Datos Distribuidor)
                cb.beginText();
                cb.setFontAndSize(bold, 10);
                cb.setTextMatrix(margen + 10, y + altoEt - 20);
                cb.showText("ROBERTO GONZÁLEZ");
                cb.setTextMatrix(margen + 10, y + altoEt - 35);
                cb.showText("DIST - ITAUGUÁ - PY");
                cb.setTextMatrix(margen + 10, y + altoEt - 50);
                cb.showText("0983 433572");

                // 4. CABECERA DERECHA (Fechas)
                float xFechas = width - margen - 65;

                if (fechasIguales) {
                    cb.showTextAligned(Element.ALIGN_RIGHT, "SORTEO: " + txtFechaSenete, xFechas, y + altoEt - 35, 0);
                } else {
                    cb.showTextAligned(Element.ALIGN_RIGHT, "SORTEO SENETÉ: " + txtFechaSenete, xFechas, y + altoEt - 25, 0);
                    cb.showTextAligned(Element.ALIGN_RIGHT, "SORTEO TELEBINGO: " + txtFechaTelebingo, xFechas, y + altoEt - 40, 0);
                }
                cb.endText();

                // LÍNEA SEPARADORA ENCABEZADO
                cb.moveTo(margen, y + altoEt - 60);
                cb.lineTo(width - margen, y + altoEt - 60);
                cb.stroke();

                // 5. NOMBRE DEL VENDEDOR (Grande y Centrado)
                cb.beginText();
                cb.setFontAndSize(bold, 15);
                cb.showTextAligned(Element.ALIGN_CENTER, item.getNombre() != null ? item.getNombre().toUpperCase() : "", width / 2, y + altoEt - 85, 0);
                cb.endText();

                // 6. COLUMNAS DINÁMICAS (Listas de Rangos)
                // Seneté (Izquierda)
                dibujarColumnaDinamica(cb, "SENETÉ", item.getSeneteRangos(), item.getSeneteCartones(), item.getResultadoSenete(),
                        margen + 20, y + altoEt - 110, helv, bold);

                // Telebingo (Derecha)
                dibujarColumnaDinamica(cb, "TELEBINGO", item.getTelebingoRangos(), item.getTelebingoCartones(), item.getResultadoTelebingo(),
                        margen + mitad + 20, y + altoEt - 110, helv, bold);


                // 7. SALDO (Fijo abajo al centro con prefijo "Gs.")
                cb.beginText();
                cb.setFontAndSize(bold, 12);
                cb.showTextAligned(Element.ALIGN_CENTER, "SALDO", width / 2, y + 45, 0);

                cb.setFontAndSize(helv, 11);
                // Agregamos "Gs."
                cb.showTextAligned(Element.ALIGN_CENTER, "Gs. " + (item.getSaldo() != null ? item.getSaldo() : "0"), width / 2, y + 30, 0);
                cb.endText();

                // PAGINACIÓN (Cada 3 etiquetas, nueva hoja)
                if ((i + 1) % 3 == 0 && (i + 1) < etiquetas.size()) {
                    document.newPage();
                }
            }

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new PdfCreationException("Error generando el PDF de etiquetas", List.of(e.getMessage()));
        }
    }

    /**
     * Dibuja una columna de producto (Título, Lista de Rangos, Total condicional y Resultados).
     * @param x Coordenada X base de la columna
     * @param yInicio Coordenada Y donde empieza el título
     */
    private void dibujarColumnaDinamica(PdfContentByte cb, String titulo, List<String> rangos, String total, String resultado,
                                        float x, float yInicio, BaseFont fNorm, BaseFont fBold) {
        float curY = yInicio;

        cb.beginText();

        // Título Columna
        cb.setFontAndSize(fBold, 11);
        cb.showTextAligned(Element.ALIGN_CENTER, titulo, x + 40, curY, 0);
        curY -= 15;

        // Lista de Rangos
        cb.setFontAndSize(fNorm, 10);
        if (rangos != null) {
            for (String r : rangos) {
                cb.showTextAligned(Element.ALIGN_LEFT, r, x, curY, 0);
                curY -= 12; // Desplazamiento vertical por cada rango
            }
        }

        // Lógica Dinámica de TOTAL
        // Solo mostramos la línea de total si hay más de 1 rango en la lista
        if (rangos != null && rangos.size() > 1) {
            cb.setFontAndSize(fBold, 10);
            cb.showTextAligned(Element.ALIGN_LEFT, "TOTAL ------------> (" + total + ")", x, curY, 0);
            curY -= 20; // Espacio extra antes de Resultados
        } else {
            curY -= 10; // Espacio normal
        }

        // Título RESULTADOS (Posición depende de la cantidad de rangos)
        cb.setFontAndSize(fBold, 11);
        cb.showTextAligned(Element.ALIGN_CENTER, "RESULTADOS", x + 40, curY, 0);

        // Valor RESULTADOS
        cb.setFontAndSize(fNorm, 11);
        cb.showTextAligned(Element.ALIGN_CENTER, resultado != null ? resultado : "0", x + 40, curY - 15, 0);

        cb.endText();
    }
}