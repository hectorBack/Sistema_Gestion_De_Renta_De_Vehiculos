package com.sistemas.backend.Cotizaciones.Services.Impl;

import com.sistemas.backend.Cotizaciones.DTO.CotizacionDto;
import com.sistemas.backend.Cotizaciones.Services.CotizacionService;
import com.sistemas.backend.Exception.BusinessRuleException;
import com.sistemas.backend.Exception.ResourceNotFoundException;
import com.sistemas.backend.Vehiculos.Entity.CategoriaVehiculo;
import com.sistemas.backend.Vehiculos.Entity.Vehiculo;
import com.sistemas.backend.Vehiculos.Repository.CategoriaVehiculoRepository;
import com.sistemas.backend.Vehiculos.Repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Service
public class CotizacionServiceImpl implements CotizacionService {

    private final CategoriaVehiculoRepository categoriaRepository;
    private final VehiculoRepository vehiculoRepository;

    public CotizacionServiceImpl(CategoriaVehiculoRepository categoriaRepository, VehiculoRepository vehiculoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CotizacionDto.Response calcularCotizacion(CotizacionDto.Request request) {
        if (request.fechaFin().isBefore(request.fechaInicio())) {
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
            throw new BusinessRuleException("Debe seleccionar una categoría o un vehículo para calcular la cotización.");
        }

        // 2. Calcular tiempo y días cobrar (mínimo 1 día)
        Duration duracion = Duration.between(request.fechaInicio(), request.fechaFin());
        long horasTotales = duracion.toHours();
        long diasCobro = (long) Math.ceil((double) horasTotales / 24.0);
        if (diasCobro == 0) diasCobro = 1;

        // 3. Cálculos de subtotales
        BigDecimal subtotalRenta = tarifaDiariaBase.multiply(BigDecimal.valueOf(diasCobro));

        // Extras opcionales (Tarifas fijas de ejemplo por día)
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

        // Depósito sugerido (e.g. 20% del total o tarifa base x 2)
        BigDecimal depositoGarantiaSugerido = tarifaDiariaBase.multiply(new BigDecimal("2"));

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
