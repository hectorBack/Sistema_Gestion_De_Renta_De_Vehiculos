package com.sistemas.backend.Cotizaciones.Controllers;

import com.sistemas.backend.Cotizaciones.DTO.CotizacionDto;
import com.sistemas.backend.Cotizaciones.Services.CotizacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cotizaciones")
public class CotizacionController {

    private final CotizacionService cotizacionService;

    public CotizacionController(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    @PostMapping("/calcular")
    public ResponseEntity<CotizacionDto.Response> calcularCotizacion(@RequestBody CotizacionDto.Request request) {
        return ResponseEntity.ok(cotizacionService.calcularCotizacion(request));
    }
}
