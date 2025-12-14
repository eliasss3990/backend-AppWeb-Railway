package com.eliasgonzalez.cartones.repository;

import com.eliasgonzalez.cartones.model.Senete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeneteRepository extends JpaRepository<Senete, Long> {
}
