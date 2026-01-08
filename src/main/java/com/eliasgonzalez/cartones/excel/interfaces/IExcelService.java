package com.eliasgonzalez.cartones.excel.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface IExcelService {
    void leerExcel(MultipartFile file, LocalDate fechaSorteo);
}
