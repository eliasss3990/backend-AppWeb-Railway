package com.eliasgonzalez.cartones.pdf.service;

import com.eliasgonzalez.cartones.pdf.dto.*;
import com.eliasgonzalez.cartones.pdf.dto.RangoLogico;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DistribucionService {

    // Margen de seguridad
    private static final int MARGEN_SEGURIDAD = 100;

    public List<VendedorSimuladoDTO> simularDistribucion (SimulacionRequestDTO request) {

        validarConfiguracion(request);

        // 1. CALCULAR DEMANDA TOTAL (Para ajustar el horizonte infinito)
        int demandaTotalSenete = request.getVendedores().stream()
                .mapToInt(v -> v.getCantidadSenete() == null ? 0 : v.getCantidadSenete()).sum();

        int demandaTotalTelebingo = request.getVendedores().stream()
                .mapToInt(v -> v.getCantidadTelebingo() == null ? 0 : v.getCantidadTelebingo()).sum();

        // 2. CONVERTIR POOLS (Pasamos la demanda)
        LinkedList<RangoLogico> poolSenete = convertirPool(
                request.getPoolSenete(), request.getInicioSeneteGral(), demandaTotalSenete
        );

        LinkedList<RangoLogico> poolTelebingo = convertirPool(
                request.getPoolTelebingo(), request.getInicioTelebingoGral(), demandaTotalTelebingo
        );

        // 3. MAPAS DE RESULTADOS
        Map<Long, List<String>> resultadosSenete = new HashMap<>();
        Map<Long, List<String>> resultadosTelebingo = new HashMap<>();

        // 4. EJECUTAR LÓGICA
        ejecutarLogicaJuego(poolSenete, request.getVendedores(), true, request.isMezclar(), resultadosSenete);
        ejecutarLogicaJuego(poolTelebingo, request.getVendedores(), false, request.isMezclar(), resultadosTelebingo);

        // 5. RETORNAR DTOs (Ordenados por RANGO ASIGNADO)
        return request.getVendedores().stream()
                .map(v -> VendedorSimuladoDTO.builder()
                        .id(v.getId())
                        .nombre(v.getNombre())
                        .rangosSenete(resultadosSenete.getOrDefault(v.getId(), new ArrayList<>()))
                        .rangosTelebingo(resultadosTelebingo.getOrDefault(v.getId(), new ArrayList<>()))
                        .build())
                .sorted(Comparator.comparingInt(this::extraerInicioOrdenamiento)) // Orden visual numérico
                .collect(Collectors.toList());
    }

    private void ejecutarLogicaJuego(LinkedList<RangoLogico> pool,
                                     List<VendedorInputDTO> vendedores,
                                     boolean esSenete,
                                     boolean mezclar,
                                     Map<Long, List<String>> mapaResultados) {

        List<VendedorInputDTO> vips = new ArrayList<>();
        List<VendedorInputDTO> normales = new ArrayList<>();

        for (VendedorInputDTO v : vendedores) {
            int cantidad = esSenete ? (v.getCantidadSenete() == null ? 0 : v.getCantidadSenete())
                    : (v.getCantidadTelebingo() == null ? 0 : v.getCantidadTelebingo());
            Integer terminacion = esSenete ? v.getTerminacionSenete() : v.getTerminacionTelebingo();

            if (cantidad > 0) {
                if (terminacion != null && terminacion >= 0) {
                    vips.add(v);
                } else {
                    normales.add(v);
                }
            }
        }

        // 1. MEZCLAR LISTAS (Aleatoriedad)
        if (mezclar) {
            Collections.shuffle(vips);
            Collections.shuffle(normales);
        }

        // 2. FASE VIP (Anclaje Aleatorio)
        Random random = new Random();
        for (VendedorInputDTO vip : vips) {
            int cantidad = esSenete ? vip.getCantidadSenete() : vip.getCantidadTelebingo();
            int terminacion = esSenete ? vip.getTerminacionSenete() : vip.getTerminacionTelebingo();

            List<RangoLogico> candidatos = buscarCandidatos(pool, terminacion, cantidad);

            if (candidatos.isEmpty()) {
                mapaResultados.put(vip.getId(), List.of("ERROR: Sin cupo para " + terminacion));
                continue;
            }

            RangoLogico eleccion = candidatos.get(random.nextInt(candidatos.size()));
            cortarYAsignarVip(pool, eleccion);
            mapaResultados.put(vip.getId(), List.of(eleccion.getInicio() + "-" + eleccion.getFin()));
        }

        // 3. COMPACTAR (Limpieza de terreno antes de los normales)
        compactarPool(pool);

        // 4. FASE NORMALES (Best Fit)
        for (VendedorInputDTO normal : normales) {
            int cantidad = esSenete ? normal.getCantidadSenete() : normal.getCantidadTelebingo();
            mapaResultados.put(normal.getId(), consumirHuecosBestFit(pool, cantidad));
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private LinkedList<RangoLogico> convertirPool(List<RangoCortadoDTO> rangosCortados, Integer inicioGeneral, int demandaTotal) {
        if (rangosCortados != null && !rangosCortados.isEmpty()) {
            return rangosCortados.stream()
                    .map(d -> new RangoLogico(d.getInicio(), d.getFin()))
                    .collect(Collectors.toCollection(LinkedList::new));
        }
        if (inicioGeneral != null) {
            LinkedList<RangoLogico> poolAjustado = new LinkedList<>();
            // FÓRMULA: Inicio + Demanda + Margen(100)
            int finCalculado = inicioGeneral + demandaTotal + MARGEN_SEGURIDAD;
            poolAjustado.add(new RangoLogico(inicioGeneral, finCalculado));
            return poolAjustado;
        }
        return new LinkedList<>();
    }

    private void compactarPool(LinkedList<RangoLogico> pool) {
        if (pool.size() < 2) return;
        pool.sort(Comparator.comparingInt(RangoLogico::getInicio));
        ListIterator<RangoLogico> iterator = pool.listIterator();
        RangoLogico actual = iterator.next();
        while (iterator.hasNext()) {
            RangoLogico siguiente = iterator.next();
            if (actual.getFin() + 1 == siguiente.getInicio()) {
                actual.setFin(siguiente.getFin());
                iterator.remove();
            } else {
                actual = siguiente;
            }
        }
    }

    private List<String> consumirHuecosBestFit(LinkedList<RangoLogico> pool, int cantidadNecesaria) {
        List<String> reporte = new ArrayList<>();
        int mejorIndice = -1;
        int menorDesperdicio = Integer.MAX_VALUE;

        // BEST FIT SCAN
        for (int i = 0; i < pool.size(); i++) {
            RangoLogico rango = pool.get(i);
            if (rango.getCantidad() >= cantidadNecesaria) {
                int desperdicio = rango.getCantidad() - cantidadNecesaria;
                if (desperdicio == 0) { mejorIndice = i; break; }
                if (desperdicio < menorDesperdicio) {
                    menorDesperdicio = desperdicio;
                    mejorIndice = i;
                }
            }
        }

        if (mejorIndice != -1) {
            RangoLogico rangoElegido = pool.get(mejorIndice);
            int finCorte = rangoElegido.getInicio() + cantidadNecesaria - 1;
            reporte.add(rangoElegido.getInicio() + "-" + finCorte);
            rangoElegido.setInicio(finCorte + 1);
            if (rangoElegido.getInicio() > rangoElegido.getFin()) pool.remove(mejorIndice);
            return reporte;
        }

        // FALLBACK FRAGMENTADO
        while (cantidadNecesaria > 0 && !pool.isEmpty()) {
            RangoLogico rangoActual = pool.getFirst();
            if (rangoActual.getCantidad() <= cantidadNecesaria) {
                reporte.add(rangoActual.getInicio() + "-" + rangoActual.getFin());
                cantidadNecesaria -= rangoActual.getCantidad();
                pool.removeFirst();
            } else {
                int finCorte = rangoActual.getInicio() + cantidadNecesaria - 1;
                reporte.add(rangoActual.getInicio() + "-" + finCorte);
                rangoActual.setInicio(finCorte + 1);
                cantidadNecesaria = 0;
            }
        }
        if (cantidadNecesaria > 0) reporte.add("FALTAN: " + cantidadNecesaria);
        return reporte;
    }

    private List<RangoLogico> buscarCandidatos(LinkedList<RangoLogico> pool, int terminacion, int cantidad) {
        List<RangoLogico> opciones = new ArrayList<>();
        int target = terminacion % 100;
        for (RangoLogico rango : pool) {
            int actual = rango.getInicio();
            while (actual <= rango.getFin()) {
                if (actual % 100 == target) {
                    int finPropuesto = actual;
                    int inicioPropuesto = finPropuesto - cantidad + 1;
                    if (inicioPropuesto >= rango.getInicio()) {
                        opciones.add(new RangoLogico(inicioPropuesto, finPropuesto));
                    }
                }
                actual++;
            }
        }
        return opciones;
    }

    private void cortarYAsignarVip(LinkedList<RangoLogico> pool, RangoLogico asignacion) {
        ListIterator<RangoLogico> iterator = pool.listIterator();
        while (iterator.hasNext()) {
            RangoLogico rangoPadre = iterator.next();
            if (rangoPadre.contiene(asignacion.getInicio(), asignacion.getFin())) {
                iterator.remove();
                if (asignacion.getInicio() > rangoPadre.getInicio()) {
                    iterator.add(new RangoLogico(rangoPadre.getInicio(), asignacion.getInicio() - 1));
                }
                if (asignacion.getFin() < rangoPadre.getFin()) {
                    iterator.add(new RangoLogico(asignacion.getFin() + 1, rangoPadre.getFin()));
                }
                return;
            }
        }
    }

    private void validarConfiguracion(SimulacionRequestDTO request) {
        boolean tieneSenete = (request.getPoolSenete() != null && !request.getPoolSenete().isEmpty())
                || request.getInicioSeneteGral() != null;
        boolean tieneTelebingo = (request.getPoolTelebingo() != null && !request.getPoolTelebingo().isEmpty())
                || request.getInicioTelebingoGral() != null;

        if (!tieneSenete && !tieneTelebingo) {
            throw new IllegalArgumentException("Debes configurar al menos un rango para Seneté o Telebingo.");
        }
    }

    // Helper para ordenar visualmente el JSON final por el primer número asignado
    private int extraerInicioOrdenamiento(VendedorSimuladoDTO dto) {
        List<String> rangos = (dto.getRangosSenete() != null && !dto.getRangosSenete().isEmpty())
                ? dto.getRangosSenete() : dto.getRangosTelebingo();

        if (rangos == null || rangos.isEmpty()) return Integer.MAX_VALUE;
        String primerRango = rangos.get(0);
        if (!Character.isDigit(primerRango.charAt(0))) return Integer.MAX_VALUE;

        try {
            return Integer.parseInt(primerRango.split("-")[0].trim());
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }
}