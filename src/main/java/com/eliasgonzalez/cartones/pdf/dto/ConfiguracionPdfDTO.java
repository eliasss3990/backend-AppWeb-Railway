package com.eliasgonzalez.cartones.pdf.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Getter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.ANY)
public class ConfiguracionPdfDTO {

    private Integer inicioSeneteGral;
    private Integer inicioTelebingoGral;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaSorteo = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

    private List<RangoCortadoDTO> rangosCortados = new ArrayList<>();

    public ConfiguracionPdfDTO() {
    }

    public ConfiguracionPdfDTO(Integer inicioSeneteGral, Integer inicioTelebingoGral, LocalDate fechaSorteo, List<RangoCortadoDTO> rangosCortados) {
        setInicioSeneteGral(inicioSeneteGral);
        setInicioTelebingoGral(inicioTelebingoGral);
        setFechaSorteo(fechaSorteo);
        setRangosCortados(rangosCortados);
    }

    @JsonProperty("inicioSenete")
    public void setInicioSeneteGral(Integer inicioSeneteGral) {
        this.inicioSeneteGral = inicioSeneteGral == null ? 0 : inicioSeneteGral;
    }

    @JsonProperty("inicioTelebingo")
    public void setInicioTelebingoGral(Integer inicioTelebingoGral) {
        this.inicioTelebingoGral = inicioTelebingoGral == null ? 0 : inicioTelebingoGral;
    }

    @JsonProperty("fechaSorteo")
    public void setFechaSorteo(LocalDate fechaSorteo) {
        this.fechaSorteo = fechaSorteo == null ? LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY)) : fechaSorteo;
    }

    @JsonProperty("rangosCortados")
    public void setRangosCortados(List<RangoCortadoDTO> rangosCortados) {
        this.rangosCortados = rangosCortados == null ? new ArrayList<>() : rangosCortados;
    }
}
