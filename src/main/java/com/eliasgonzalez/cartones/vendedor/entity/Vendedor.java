package com.eliasgonzalez.cartones.vendedor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Table(name = "VENDEDORES")
@Builder
public class Vendedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Builder.Default
    private Integer cantidadSenete = 0;

    @Builder.Default
    private Integer resultadoSenete = 0;

    @Builder.Default
    private Integer cantidadTelebingo = 0;

    @Builder.Default
    private Integer resultadoTelebingo = 0;

    @Builder.Default
    private BigDecimal deuda = BigDecimal.ZERO;

    @Builder.Default
    private LocalDate fechaSorteo = LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));

}
