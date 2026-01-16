package com.eliasgonzalez.cartones.vendedor.interfaces;

import com.eliasgonzalez.cartones.vendedor.entity.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendedorRepository extends JpaRepository<Vendedor, Long> {

    @Query(value = "SELECT v FROM Vendedor v WHERE v.cantidadSenete > 0 OR v.cantidadTelebingo > 0")
    List<Vendedor> findVendedoresValidos();

}