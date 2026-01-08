package com.eliasgonzalez.cartones.pdf.interfaces;

import org.springframework.core.io.Resource;

public interface IPdfService {

    Resource obtenerZipPdfs (String procesoIdRecibido);

}
