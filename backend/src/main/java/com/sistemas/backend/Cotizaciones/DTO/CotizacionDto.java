package com.sistemas.backend.Cotizaciones.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CotizacionDto {

    public record Request(
            Integer idCategoria,
            Integer idVehiculo,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Boolean incluyeSeguro,
            Boolean conductorAdicional
    ) {}

    public record Response(
            String vehiculoOcategoriaInfo,
            long diasTotales,
            long horasTotales,
            BigDecimal tarifaDiariaBase,
            BigDecimal subtotalRenta,
            BigDecimal costoSeguro,
            BigDecimal costoConductorAdicional,
            BigDecimal subtotalGeneral,
            BigDecimal impuestos,
            BigDecimal totalEstimado,
            BigDecimal depositoGarantiaSugerido
    ) {}
}
