package com.sistemas.backend.Cotizaciones.Services;

import com.sistemas.backend.Cotizaciones.DTO.CotizacionDto;

public interface CotizacionService {

    CotizacionDto.Response calcularCotizacion(CotizacionDto.Request request);
}
