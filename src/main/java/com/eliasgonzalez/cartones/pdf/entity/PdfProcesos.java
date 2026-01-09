package com.eliasgonzalez.cartones.pdf.entity;

import com.eliasgonzalez.cartones.pdf.enums.EstadoEnum;
import jakarta.persistence.*;
import lombok.*;

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
    private String estado = EstadoEnum.PENDIENTE.getValue();

    @Column
    private byte[] pdfEtiquetas;

    @Column
    private byte[] pdfResumen;

}
