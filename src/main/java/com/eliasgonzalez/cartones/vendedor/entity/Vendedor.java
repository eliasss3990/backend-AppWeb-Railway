package com.eliasgonzalez.cartones.vendedor.entity;

import com.eliasgonzalez.cartones.config.ListaRangosConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    // Vincula al vendedor con un sorteo/ticket específico
    @Column(name = "proceso_id", nullable = false)
    private String procesoId;

    @Column(nullable = false)
    private String nombre;

    // --- SENETÉ ---
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Integer cantidadSenete = 0;

    private Integer terminacionSenete;

    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Integer resultadoSenete = 0;

    // RANGOS (Se guardan como JSON: ["100-150", "200-210"])
    @Convert(converter = ListaRangosConverter.class)
    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private List<String> rangosSenete = new ArrayList<>();


    // --- TELEBINGO ---
    @Builder.Default
    @Setter (AccessLevel.NONE)
    private Integer cantidadTelebingo = 0;

    private Integer terminacionTelebingo;

    @Builder.Default
    @Setter (AccessLevel.NONE)
    private Integer resultadoTelebingo = 0;

    // RANGOS
    @Convert(converter = ListaRangosConverter.class)
    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private List<String> rangosTelebingo = new ArrayList<>();


    // --- DEUDA DEL VENDEDOR ---
    @Builder.Default
    private BigDecimal deuda = BigDecimal.ZERO;


    // --- SETTERS PERSONALIZADOS ---

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
