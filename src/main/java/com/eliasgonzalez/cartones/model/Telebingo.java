package com.eliasgonzalez.cartones.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "TELEBINGO")
public class Telebingo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Nuevo ID primario de la tabla Telebingo

    // Clave Foránea al Vendedor
    @ManyToOne
    @JoinColumn(name = "vendedor_id", nullable = false) // nombre de la FK en la tabla Telebingo
    private Vendedor vendedor;

    @Column(nullable = false)
    private Integer cantidadTelebingo;

    @Column(nullable = false)
    private Integer resultadoTelebingo;

}