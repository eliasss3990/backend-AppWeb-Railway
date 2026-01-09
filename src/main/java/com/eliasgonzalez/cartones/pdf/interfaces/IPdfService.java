package com.eliasgonzalez.cartones.pdf.interfaces;

import com.eliasgonzalez.cartones.pdf.dto.ConfiguracionPdfDTO;
import org.springframework.core.io.Resource;

public interface IPdfService {

    Resource obtenerZipPdfs (String procesoIdRecibido, ConfiguracionPdfDTO config);

}
