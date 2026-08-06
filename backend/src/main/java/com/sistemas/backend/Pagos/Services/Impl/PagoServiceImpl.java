package com.sistemas.backend.Pagos.Services.Impl;

import com.sistemas.backend.Exception.BusinessRuleException;
import com.sistemas.backend.Exception.ResourceNotFoundException;
import com.sistemas.backend.Pagos.DTO.PagoDto;
import com.sistemas.backend.Pagos.Entity.EstadoPago;
import com.sistemas.backend.Pagos.Entity.MetodoPago;
import com.sistemas.backend.Pagos.Entity.Pago;
import com.sistemas.backend.Pagos.Mapper.PagoMapper;
import com.sistemas.backend.Pagos.Repository.PagoRepository;
import com.sistemas.backend.Pagos.Services.PagoService;
import com.sistemas.backend.Rentas.Entity.Renta;
import com.sistemas.backend.Rentas.Repository.RentaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final RentaRepository rentaRepository;
    private final PagoMapper pagoMapper;

    public PagoServiceImpl(PagoRepository pagoRepository, RentaRepository rentaRepository, PagoMapper pagoMapper) {
        this.pagoRepository = pagoRepository;
        this.rentaRepository = rentaRepository;
        this.pagoMapper = pagoMapper;
    }

    @Override
    @Transactional
    public PagoDto.Response registrarPago(PagoDto.CreateRequest request) {
        log.info("Iniciando procesamiento de pago de {} para la renta #{}", request.monto(), request.idRenta());

        if (request.monto() == null || request.monto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("El monto del pago debe ser mayor a cero.");
        }

        Renta renta = rentaRepository.findById(request.idRenta())
                .orElseThrow(() -> new ResourceNotFoundException("Renta no encontrada ID: " + request.idRenta()));

        BigDecimal saldoPendiente = calcularSaldoPendienteInterno(renta);

        if (request.monto().compareTo(saldoPendiente) > 0) {
            throw new BusinessRuleException(String.format(
                    "El monto a pagar (%s) excede el saldo pendiente actual (%s) de la renta.",
                    request.monto(), saldoPendiente));
        }

        Pago pago = pagoMapper.toEntity(request, renta);
        pago.setFechaPago(LocalDateTime.now());
        pago.setEstado(EstadoPago.COMPLETADO);

        Pago guardado = pagoRepository.save(pago);
        log.info("Pago #{} registrado exitosamente. Saldo restante en renta #{}: {}",
                guardado.getId(), renta.getId(), saldoPendiente.subtract(request.monto()));

        return pagoMapper.toResponse(guardado);
    }

    @Override
    @Transactional
    public PagoDto.Response procesarReembolso(Long idPago, PagoDto.ReembolsoRequest request) {
        log.info("Procesando solicitud de reembolso para el Pago #{}", idPago);

        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado ID: " + idPago));

        if (pago.getEstado() != EstadoPago.COMPLETADO) {
            throw new BusinessRuleException("Solo se pueden reembolsar pagos en estado COMPLETADO.");
        }

        if (request.montoReembolso().compareTo(pago.getMonto()) > 0) {
            throw new BusinessRuleException("El monto a reembolsar no puede superar el monto pagado originalmente.");
        }

        boolean esTotal = request.montoReembolso().compareTo(pago.getMonto()) == 0;
        pago.setEstado(esTotal ? EstadoPago.REEMBOLSADO : EstadoPago.PARCIALMENTE_REEMBOLSADO);
        pago.setMotivoReembolso(request.motivo());

        Pago actualizado = pagoRepository.save(pago);
        log.info("Reembolso procesado para Pago #{}. Tipo: {}", idPago, pago.getEstado());

        return pagoMapper.toResponse(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoDto.Response obtenerPorId(Long id) {
        return pagoRepository.findById(id)
                .map(pagoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PagoDto.Response> listarPaginado(Pageable pageable) {
        log.debug("Consultando catálogo paginado de pagos. Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return pagoRepository.findAll(pageable)
                .map(pagoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PagoDto.Response> buscarConFiltros(Long idRenta, EstadoPago estado, MetodoPago metodoPago, Pageable pageable) {
        return pagoRepository.buscarConFiltros(idRenta, estado, metodoPago, pageable)
                .map(pagoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerSaldoPendiente(Long idRenta) {
        Renta renta = rentaRepository.findById(idRenta)
                .orElseThrow(() -> new ResourceNotFoundException("Renta no encontrada ID: " + idRenta));
        return calcularSaldoPendienteInterno(renta);
    }

    // --- Helper de negocio ---
    private BigDecimal calcularSaldoPendienteInterno(Renta renta) {
        BigDecimal totalPagado = pagoRepository.sumMontoByRentaIdAndEstado(renta.getId(), EstadoPago.COMPLETADO)
                .orElse(BigDecimal.ZERO);

        BigDecimal costoTotal = renta.getCostoTotal() != null ? renta.getCostoTotal() : BigDecimal.ZERO;
        BigDecimal saldo = costoTotal.subtract(totalPagado);

        return saldo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : saldo;
    }
}
