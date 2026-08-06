package com.sistemas.backend.Cotizaciones.Services.Impl;

import com.sistemas.backend.Cotizaciones.DTO.CotizacionDto;
import com.sistemas.backend.Cotizaciones.Mapper.CotizacionMapper;
import com.sistemas.backend.Cotizaciones.Services.CotizacionService;
import com.sistemas.backend.Exception.BusinessRuleException;
import com.sistemas.backend.Exception.ResourceNotFoundException;
import com.sistemas.backend.Vehiculos.Entity.CategoriaVehiculo;
import com.sistemas.backend.Vehiculos.Entity.Vehiculo;
import com.sistemas.backend.Vehiculos.Repository.CategoriaVehiculoRepository;
import com.sistemas.backend.Vehiculos.Repository.VehiculoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Slf4j
@Service
public class CotizacionServiceImpl implements CotizacionService {

    private final CategoriaVehiculoRepository categoriaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final CotizacionMapper cotizacionMapper;

    public CotizacionServiceImpl(CategoriaVehiculoRepository categoriaRepository, VehiculoRepository vehiculoRepository, CotizacionMapper cotizacionMapper) {
        this.categoriaRepository = categoriaRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.cotizacionMapper = cotizacionMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public CotizacionDto.Response calcularCotizacion(CotizacionDto.Request request) {
        log.info("Iniciando cálculo de cotización. Vehículo ID: {}, Categoría ID: {}",
                request.idVehiculo(), request.idCategoria());

        if (request.fechaFin().isBefore(request.fechaInicio())) {
            log.warn("Solicitud de cotización rechazada: La fecha de devolución ({}) es anterior a la de retiro ({})",
                    request.fechaFin(), request.fechaInicio());
            throw new BusinessRuleException("La fecha de devolución debe ser posterior a la fecha de retiro.");
        }

        // 1. Obtener la tarifa diaria de la categoría o del vehículo específico
        BigDecimal tarifaDiariaBase;
        String infoConcepto;

        if (request.idVehiculo() != null) {
            Vehiculo vehiculo = vehiculoRepository.findByIdWithDetails(request.idVehiculo())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + request.idVehiculo()));
            tarifaDiariaBase = vehiculo.getCategoria().getTarifaDiariaBase();
            infoConcepto = vehiculo.getMarca() + " " + vehiculo.getModelo() + " (" + vehiculo.getPlaca() + ")";
        } else if (request.idCategoria() != null) {
            CategoriaVehiculo categoria = categoriaRepository.findById(request.idCategoria())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.idCategoria()));
            tarifaDiariaBase = categoria.getTarifaDiariaBase();
            infoConcepto = "Categoría: " + categoria.getNombre();
        } else {
            log.warn("Solicitud de cotización rechazada: No se proporcionó ni categoría ni vehículo.");
            throw new BusinessRuleException("Debe seleccionar una categoría o un vehículo para calcular la cotización.");
        }

        // 2. Calcular tiempo y días a cobrar (mínimo 1 día)
        Duration duracion = Duration.between(request.fechaInicio(), request.fechaFin());
        long horasTotales = duracion.toHours();
        long diasCobro = (long) Math.ceil((double) horasTotales / 24.0);
        if (diasCobro == 0) {
            diasCobro = 1;
        }

        // 3. Cálculos de subtotales
        BigDecimal subtotalRenta = tarifaDiariaBase.multiply(BigDecimal.valueOf(diasCobro));

        // Extras opcionales (Tarifas fijas por día)
        BigDecimal costoSeguro = Boolean.TRUE.equals(request.incluyeSeguro())
                ? new BigDecimal("150.00").multiply(BigDecimal.valueOf(diasCobro))
                : BigDecimal.ZERO;

        BigDecimal costoConductorAdicional = Boolean.TRUE.equals(request.conductorAdicional())
                ? new BigDecimal("80.00").multiply(BigDecimal.valueOf(diasCobro))
                : BigDecimal.ZERO;

        BigDecimal subtotalGeneral = subtotalRenta.add(costoSeguro).add(costoConductorAdicional);

        // 16% de IVA
        BigDecimal impuestos = subtotalGeneral.multiply(new BigDecimal("0.16")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalEstimado = subtotalGeneral.add(impuestos);

        // Depósito sugerido (Tarifa base x 2)
        BigDecimal depositoGarantiaSugerido = tarifaDiariaBase.multiply(new BigDecimal("2"));

        log.info("Cotización calculada exitosamente para '{}'. Total estimado: {}", infoConcepto, totalEstimado);

        return cotizacionMapper.toResponse(
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
