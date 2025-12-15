package com.eliasgonzalez.cartones.bingo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelebingoRepository extends JpaRepository<Telebingo, Long> {
}
