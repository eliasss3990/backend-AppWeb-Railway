package com.eliasgonzalez.cartones.bingo;

import com.eliasgonzalez.cartones.vendedor.Vendedor;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "SENETE")
public class Senete {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Nuevo ID primario de la tabla Senete

    // Clave Foránea al Vendedor
    @ManyToOne
    @JoinColumn(name = "vendedor_id", nullable = false) // nombre de la FK en la tabla Senete
    private Vendedor vendedor;

    @Column(nullable = false)
    private Integer cantidadSenete = 0;

    @Column(nullable = false)
    private Integer resultadoSenete = 0;

}
