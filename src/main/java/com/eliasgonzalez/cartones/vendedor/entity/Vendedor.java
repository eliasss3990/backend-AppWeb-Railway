package com.eliasgonzalez.cartones.vendedor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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
    @Setter (AccessLevel.NONE)
    private Integer cantidadSenete = 0;

    @Builder.Default
    @Setter (AccessLevel.NONE)
    private Integer resultadoSenete = 0;

    @Builder.Default
    @Setter (AccessLevel.NONE)
    private Integer cantidadTelebingo = 0;

    @Builder.Default
    @Setter (AccessLevel.NONE)
    private Integer resultadoTelebingo = 0;

    @Builder.Default
    private BigDecimal deuda = BigDecimal.ZERO;

    public void setCantidadSenete(Integer cantidadSenete) {
        this.cantidadSenete = cantidadSenete == null ? 0 : cantidadSenete;
    }

    public void setCantidadTelebingo(Integer cantidadTelebingo) {
        this.cantidadTelebingo = cantidadTelebingo == null ? 0 : cantidadTelebingo;
    }

    public void setResultadoSenete(Integer resultadoSenete) {
        this.resultadoSenete = resultadoSenete == null ? 0 : resultadoSenete;
    }

    public void setResultadoTelebingo(Integer resultadoTelebingo) {
        this.resultadoTelebingo = resultadoTelebingo == null ? 0 : resultadoTelebingo;
    }
}
