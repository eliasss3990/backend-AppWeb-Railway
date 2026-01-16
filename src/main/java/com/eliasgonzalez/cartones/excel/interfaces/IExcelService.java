package com.eliasgonzalez.cartones.excel.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface IExcelService {
    void leerExcel(MultipartFile file, String procesoIdCreado);
}
