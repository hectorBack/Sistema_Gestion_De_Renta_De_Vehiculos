package com.sistemas.backend.Cotizaciones.Mapper;

import com.sistemas.backend.Cotizaciones.DTO.CotizacionDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CotizacionMapper {

    /**
     * Construye el DTO de respuesta de la cotización a partir de los valores calculados.
     */
    public CotizacionDto.Response toResponse(
            String infoConcepto,
            long diasCobro,
            long horasTotales,
            BigDecimal tarifaDiariaBase,
            BigDecimal subtotalRenta,
            BigDecimal costoSeguro,
            BigDecimal costoConductorAdicional,
            BigDecimal subtotalGeneral,
            BigDecimal impuestos,
            BigDecimal totalEstimado,
            BigDecimal depositoGarantiaSugerido
    ) {
        return new CotizacionDto.Response(
                infoConcepto,
                diasCobro,
                horasTotales,
                tarifaDiariaBase,
                subtotalRenta,
                costoSeguro,
                costoConductorAdicional,
                subtotalGeneral,
                impuestos,
                totalEstimado,
                depositoGarantiaSugerido
        );
    }
}
