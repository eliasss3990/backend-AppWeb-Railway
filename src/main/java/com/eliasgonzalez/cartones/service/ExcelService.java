package com.eliasgonzalez.cartones.service;

import com.eliasgonzalez.cartones.model.Senete;
import com.eliasgonzalez.cartones.model.Telebingo;
import com.eliasgonzalez.cartones.model.Vendedor;
import com.eliasgonzalez.cartones.repository.SeneteRepository;
import com.eliasgonzalez.cartones.repository.TelebingoRepository;
import com.eliasgonzalez.cartones.repository.VendedorRepository;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
@AllArgsConstructor
public class ExcelService {

    private final VendedorRepository vendedorRepo;
    private final TelebingoRepository telebingoRepo;
    private final SeneteRepository seneteRepo;

    @Transactional
    public void leerExcel(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream();
             Workbook wb = WorkbookFactory.create(is)) {

            int aux = wb.getSheetIndex(ExcelEnum.HOJA_SISTEMA_ETIQUETAS.getValue());
            Sheet sheet = wb.getSheetAt(aux);
            Iterator<Row> rows = sheet.iterator();
            if (!rows.hasNext()) return;

            // leer encabezado y mapear nombres normalizados a índices
            Row header = rows.next();
            Map<String, Integer> idx = new HashMap<>();
            for (Cell c : header) {
                String name = c.getStringCellValue();
                if (name != null) idx.put(Util.normalize(name), c.getColumnIndex());
            }

            // Leer todos los datos del excel y guardarlos en la db
            while (rows.hasNext()) {
                Row r = rows.next();
                if (Util.isRowEmpty(r)) continue;

                String nombre = Util.getStringCell(r, idx.get(ExcelEnum.VENDEDOR.getValue()));
                String deudaStr = Util.getStringCell(r, idx.get(ExcelEnum.SALDO.getValue()));

                BigDecimal deuda = (deudaStr == null || deudaStr.isBlank()) ?
                        BigDecimal.ZERO : new BigDecimal(deudaStr.trim());

                Vendedor v = new Vendedor();
                v.setNombre(nombre);
                v.setDeuda(deuda);
                Vendedor savedV = vendedorRepo.save(v);

                Integer cantidadSenete = Util.getIntCell(r, idx.get(ExcelEnum.CANT_SENETE.getValue()));
                Integer resultadoSenete = Util.getIntCell(r, idx.get(ExcelEnum.RESULT_SENETE.getValue()));
                if (cantidadSenete != null || resultadoSenete != null) {
                    Senete s = new Senete();
                    s.setVendedor(savedV);
                    s.setCantidadSenete(cantidadSenete);
                    s.setResultadoSenete(resultadoSenete);
                    seneteRepo.save(s);
                }

                Integer cantidadTelebingo = Util.getIntCell(r, idx.get(ExcelEnum.CANT_TELEBINGO.getValue()));
                Integer resultadoTelebingo = Util.getIntCell(r, idx.get(ExcelEnum.RESULT_TELEBINGO.getValue()));
                if (cantidadTelebingo != null || resultadoTelebingo != null) {
                    Telebingo t = new Telebingo();
                    t.setVendedor(savedV);
                    t.setCantidadTelebingo(cantidadTelebingo);
                    t.setResultadoTelebingo(resultadoTelebingo);
                    telebingoRepo.save(t);
                }
            }
        }
    }
}
