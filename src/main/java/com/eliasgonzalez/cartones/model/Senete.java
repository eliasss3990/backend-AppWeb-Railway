package com.eliasgonzalez.cartones.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Senete {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Nuevo ID primario de la tabla Senete

    // Clave Foránea al Vendedor
    @ManyToOne
    @JoinColumn(name = "vendedor_id", nullable = false) // nombre de la FK en la tabla Senete
    private Vendedor vendedor;

    private Integer cantidadSenete;
    private Integer resultadoSenete;

}
