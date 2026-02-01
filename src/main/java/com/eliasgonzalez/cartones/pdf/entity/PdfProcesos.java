package com.eliasgonzalez.cartones.pdf.entity;

import com.eliasgonzalez.cartones.pdf.enums.EstadoEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Arrays;

@Entity
@Table(name = "PROCESOS_PDF")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class PdfProcesos {

    @Id
    private String procesoId;

    @Builder.Default
    @Setter (AccessLevel.NONE)
    private String estado = EstadoEnum.PENDIENTE.getValue();

    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "pdf_etiquetas")
    private byte[] pdfEtiquetas;

    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "pdf_resumen")
    private byte[] pdfResumen;

    public void setEstado(String estadoEnum) {
        this.estado = estadoEnum == null ? EstadoEnum.PENDIENTE.getValue() : estadoEnum;
    }

    @Override
    public String toString() {
        return "PdfProcesos{" +
                "procesoId='" + procesoId + '\'' +
                ", estado='" + estado + '\'' +
                ", pdfEtiquetas=" + Arrays.toString(pdfEtiquetas) +
                ", pdfResumen=" + Arrays.toString(pdfResumen) +
                '}';
    }
}
