package com.eliasgonzalez.cartones.bingo;

import com.eliasgonzalez.cartones.vendedor.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeneteRepository extends JpaRepository<Senete, Long> {
}
