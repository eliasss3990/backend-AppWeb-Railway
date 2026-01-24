package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.entity.PdfProcesos;
import com.eliasgonzalez.cartones.pdf.enums.EstadoEnum;
import com.eliasgonzalez.cartones.shared.exception.UnprocessableEntityException;

import java.util.List;

public class ProcesoIdService {

    public static void PendienteToVerificando(String procesoIdRecibido, PdfProcesos pdfProcesos) {

        if (!EstadoEnum.PENDIENTE.getValue().equals(pdfProcesos.getEstado()) &&
                !EstadoEnum.VERIFICANDO.getValue().equals(pdfProcesos.getEstado())
        ) {
            throw new UnprocessableEntityException(
                    "El proceso no está en estado 'pendiente'.",
                    List.of("El proceso " + procesoIdRecibido + " tiene un estado '" + pdfProcesos.getEstado() + "'")
            );
        }

        pdfProcesos.setEstado(EstadoEnum.VERIFICANDO.getValue());

    }

    public static void VerificandoToCompletado (String procesoIdRecibido, PdfProcesos pdfProcesos){

        if (!EstadoEnum.VERIFICANDO.getValue().equals(pdfProcesos.getEstado())) {
            throw new UnprocessableEntityException(
                    "El proceso no está en estado VERIFICANDO.",
                    List.of("El proceso " + procesoIdRecibido + " tiene un estado " + pdfProcesos.getEstado())
            );
        }

        pdfProcesos.setEstado(EstadoEnum.COMPLETADO.getValue());

    }
}
