package com.eliasgonzalez.cartones.vendedor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendedorRepository extends JpaRepository<Vendedor, Long> {

    // ESTA QUERY ES LA SOLUCIÓN AL PROBLEMA DE LOS GETTERS FALTANTES
    @Query("SELECT new com.eliasgonzalez.cartones.vendedor.VendedorResponseDTO(" +
            "v.id, v.nombre, v.deuda, s.cantidadSenete, s.resultadoSenete, t.cantidadTelebingo, t.resultadoTelebingo) " +
            "FROM Vendedor v " +
            "LEFT JOIN Senete s ON s.vendedor = v " + // Usamos el campo 'vendedor' de la entidad Senete
            "LEFT JOIN Telebingo t ON t.vendedor = v") // Usamos el campo 'vendedor' de la entidad Telebingo
    List<VendedorResponseDTO> findAllVendedorDTOs();

}