package com.sistemas.backend.Pagos.Services;

import com.sistemas.backend.Pagos.DTO.PagoDto;
import com.sistemas.backend.Pagos.Entity.EstadoPago;
import com.sistemas.backend.Pagos.Entity.MetodoPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface PagoService {

    PagoDto.Response registrarPago(PagoDto.CreateRequest request);

    // Reembolsar un pago previo
    PagoDto.Response procesarReembolso(Long idPago, PagoDto.ReembolsoRequest request);

    PagoDto.Response obtenerPorId(Long id);
    Page<PagoDto.Response> listarPaginado(Pageable pageable);

    Page<PagoDto.Response> buscarConFiltros(Long idRenta, EstadoPago estado, MetodoPago metodoPago, Pageable pageable);

    // Consulta de salud financiera de la renta
    BigDecimal obtenerSaldoPendiente(Long idRenta);
}
