package com.eliasgonzalez.cartones.pdf.component;

import com.eliasgonzalez.cartones.pdf.dto.VendedorSimuladoDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Getter @Setter
public class SaveInMemoryTemp {

    private List<VendedorSimuladoDTO> vendedorSimuladoDTOs;
    private LocalDate fechaSorteoSenete;
    private LocalDate fechaSorteoTelebingo;

    public void guardar(List<VendedorSimuladoDTO> vendedorSimuladoDTOs) {
        this.vendedorSimuladoDTOs = vendedorSimuladoDTOs;
    }

}
