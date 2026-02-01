package com.eliasgonzalez.cartones.pdf.entity;

import com.eliasgonzalez.cartones.pdf.enums.EstadoEnum;
import jakarta.persistence.*;
import lombok.*;

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
    @Column(columnDefinition = "BYTEA")
    private byte[] pdfEtiquetas;

    @Lob
    @Column(columnDefinition = "BYTEA")
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
