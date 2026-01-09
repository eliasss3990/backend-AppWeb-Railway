package com.eliasgonzalez.cartones.pdf.interfaces;

import com.eliasgonzalez.cartones.pdf.entity.PdfProcesos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PdfProcesosRepository extends JpaRepository<PdfProcesos, String> {
    
}
