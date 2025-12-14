package com.eliasgonzalez.cartones.dto;

import com.eliasgonzalez.cartones.model.Senete;
import com.eliasgonzalez.cartones.model.Telebingo;
import com.eliasgonzalez.cartones.model.Vendedor;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class VendedorDTO {
    private Vendedor vendedor;
    private Senete senete;
    private Telebingo telebingo;
}
