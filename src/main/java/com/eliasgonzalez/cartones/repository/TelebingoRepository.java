package com.eliasgonzalez.cartones.repository;

import com.eliasgonzalez.cartones.model.Telebingo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelebingoRepository extends JpaRepository<Telebingo, Long> {
}
