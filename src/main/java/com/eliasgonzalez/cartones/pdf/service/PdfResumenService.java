package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.dto.ResumenDTO;
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
import java.util.*;

@Service
@Transactional
public class PdfResumenService {

    // --- CONFIGURACIÓN DE DISEÑO ---
    private static final float MARGEN = 15;
    private static final float PADDING_Y = 6; // Espacio vertical interno en celdas

    // Mapa de Posiciones X (Definición de Columnas)
    private static final Map<String, Float> COLUMNAS_X = new HashMap<>();

    static {
        COLUMNAS_X.put("NUMERO", MARGEN + 5);        // Columna #
        COLUMNAS_X.put("VENDEDOR", MARGEN + 35);     // Nombre
        COLUMNAS_X.put("SENETE", MARGEN + 180);      // Rango Seneté
        COLUMNAS_X.put("TELEBINGO", MARGEN + 330);   // Rango Telebingo
        COLUMNAS_X.put("CANT_S", MARGEN + 480);      // Cant. Seneté
        COLUMNAS_X.put("CANT_T", MARGEN + 530);      // Cant. Telebingo
    }

    public byte[] generarResumen(List<ResumenDTO> vendedores, LocalDate fechaSenete, LocalDate fechaTelebingo) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            document.open();
            PdfContentByte cb = writer.getDirectContent();

            float width = PageSize.A4.getWidth();
            float height = PageSize.A4.getHeight();

            BaseFont helv = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont bold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

            // Formateador para mostrar fechas bonitas (dd/MM/yyyy) si lo deseas, o usar toString por defecto
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // --- 1. TÍTULO Y FECHAS (Lógica Dual) ---
            float y = height - MARGEN - 20;
            cb.beginText();
            cb.setFontAndSize(bold, 14);

            if (fechaSenete.isEqual(fechaTelebingo)) {
                // CASO A: Fechas Iguales -> Una sola línea
                cb.showTextAligned(Element.ALIGN_CENTER, "RESUMEN DE ENTREGA - " + fechaSenete.format(fmt), width / 2, y, 0);
                y -= 25;
            } else {
                // CASO B: Fechas Distintas -> Título + Subtítulo con ambas fechas
                cb.showTextAligned(Element.ALIGN_CENTER, "RESUMEN DE ENTREGA", width / 2, y, 0);
                cb.setFontAndSize(bold, 10);
                cb.showTextAligned(Element.ALIGN_CENTER,
                        "SENETÉ: " + fechaSenete.format(fmt) + "  |  TELEBINGO: " + fechaTelebingo.format(fmt),
                        width / 2, y - 15, 0);
                y -= 35;
            }
            cb.endText();

            // --- 2. ENCABEZADOS DE TABLA ---
            dibujarEncabezados(cb, y, width, bold);
            y -= 25;

            // --- 3. CUERPO DE LA TABLA ---
            for (int i = 0; i < vendedores.size(); i++) {
                ResumenDTO row = vendedores.get(i);

                // Calcular Altura Dinámica
                int lineasSenete = row.getSeneteDelAl().isEmpty() ? 1 : row.getSeneteDelAl().size();
                int lineasTelebingo = row.getTelebingoDelAl().isEmpty() ? 1 : row.getTelebingoDelAl().size();
                int maxLineas = Math.max(lineasSenete, lineasTelebingo);

                // Altura total = (líneas * 12px) + padding arriba/abajo
                float alturaFila = (maxLineas * 12) + PADDING_Y * 2;

                // Salto de Página si no cabe
                if (y - alturaFila < MARGEN + 20) {
                    document.newPage();
                    y = height - MARGEN - 40;
                    dibujarEncabezados(cb, y, width, bold);
                    y -= 25;
                }

                // Fondo Alternado (Gris/Blanco)
                if ((i + 1) % 2 == 0) {
                    cb.saveState();
                    cb.setRGBColorFill(240, 240, 240);
                    cb.rectangle(MARGEN, y - alturaFila, width - 2 * MARGEN, alturaFila);
                    cb.fill();
                    cb.restoreState();
                }

                // Escribir Datos Fijos (Nombre, #, Cantidades)
                cb.beginText();
                cb.setFontAndSize(helv, 9);

                // Alineamos el texto fijo arriba, coincidiendo con el primer rango
                float yTextoBase = y - 12 - PADDING_Y + 4;

                // Columna #
                cb.showTextAligned(Element.ALIGN_LEFT, "#" + row.getNumeroVendedor(), COLUMNAS_X.get("NUMERO"), yTextoBase, 0);

                // Columna Vendedor
                cb.showTextAligned(Element.ALIGN_LEFT, row.getNombre(), COLUMNAS_X.get("VENDEDOR"), yTextoBase, 0);

                // Columnas Cantidades
                cb.showTextAligned(Element.ALIGN_CENTER, String.valueOf(row.getCantidadSenete()), COLUMNAS_X.get("CANT_S") + 15, yTextoBase, 0);
                cb.showTextAligned(Element.ALIGN_CENTER, String.valueOf(row.getCantidadTelebingo()), COLUMNAS_X.get("CANT_T") + 15, yTextoBase, 0);

                cb.endText();

                // Dibujar Listas de Rangos
                dibujarMapaRangos(cb, row.getSeneteDelAl(), COLUMNAS_X.get("SENETE"), y - PADDING_Y - 8, helv);
                dibujarMapaRangos(cb, row.getTelebingoDelAl(), COLUMNAS_X.get("TELEBINGO"), y - PADDING_Y - 8, helv);

                // Avanzar cursor Y
                y -= alturaFila;

                // Línea separadora sutil
                cb.setLineWidth(0.5f);
                cb.setGrayStroke(0.8f);
                cb.moveTo(MARGEN, y);
                cb.lineTo(width - MARGEN, y);
                cb.stroke();
            }

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new PdfCreationException("Error generando Resumen", List.of(e.getMessage()));
        }
    }

    private void dibujarEncabezados(PdfContentByte cb, float y, float width, BaseFont font) {
        cb.beginText();
        cb.setFontAndSize(font, 9);

        cb.showTextAligned(Element.ALIGN_LEFT, "#", COLUMNAS_X.get("NUMERO"), y, 0);
        cb.showTextAligned(Element.ALIGN_LEFT, "VENDEDOR", COLUMNAS_X.get("VENDEDOR"), y, 0);

        cb.showTextAligned(Element.ALIGN_LEFT, "RANGOS SENETÉ (Del - Al)", COLUMNAS_X.get("SENETE"), y, 0);
        cb.showTextAligned(Element.ALIGN_LEFT, "RANGOS TELEBINGO (Del - Al)", COLUMNAS_X.get("TELEBINGO"), y, 0);

        cb.showTextAligned(Element.ALIGN_CENTER, "CANT. S", COLUMNAS_X.get("CANT_S") + 15, y, 0);
        cb.showTextAligned(Element.ALIGN_CENTER, "CANT. T", COLUMNAS_X.get("CANT_T") + 15, y, 0);

        cb.endText();

        cb.setLineWidth(1f);
        cb.setGrayStroke(0f);
        cb.moveTo(MARGEN, y - 5);
        cb.lineTo(width - MARGEN, y - 5);
        cb.stroke();
    }

    private void dibujarMapaRangos(PdfContentByte cb, Map<String, String> rangos, float xBase, float yInicio, BaseFont font) {
        if (rangos == null || rangos.isEmpty()) return;

        cb.beginText();
        cb.setFontAndSize(font, 9);

        float currentY = yInicio;

        // ORDENAMIENTO DE RANGOS:
        List<String> llavesOrdenadas = new ArrayList<>(rangos.keySet());
        Collections.sort(llavesOrdenadas);

        for (String inicio : llavesOrdenadas) {
            String fin = rangos.get(inicio);
            cb.showTextAligned(Element.ALIGN_LEFT, inicio + " - " + fin, xBase, currentY, 0);
            currentY -= 12;
        }

        cb.endText();
    }
}