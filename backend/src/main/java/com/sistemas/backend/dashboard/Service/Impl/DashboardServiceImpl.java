package com.sistemas.backend.dashboard.Service.Impl;

import com.sistemas.backend.Rentas.Entity.EstadoRenta;
import com.sistemas.backend.Vehiculos.Entity.EstadoVehiculo;
import com.sistemas.backend.dashboard.DTO.*;
import com.sistemas.backend.dashboard.Repository.DashboardRepository;
import com.sistemas.backend.dashboard.Service.DashboardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardServiceImpl(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public DashboardMetricsDto obtenerMetricasDashboard() {
        LocalDateTime ahora = LocalDateTime.now();

        // 1. Obtener KPIs
        long rentasActivas = dashboardRepository.countByEstado(EstadoRenta.ACTIVA);

        YearMonth mesActual = YearMonth.now();
        LocalDateTime inicioMes = mesActual.atDay(1).atStartOfDay();
        LocalDateTime finMes = mesActual.atEndOfMonth().atTime(LocalTime.MAX);
        BigDecimal ingresosMes = dashboardRepository.calcularIngresosRango(inicioMes, finMes);

        long vehiculosDisponibles = dashboardRepository.contarVehiculosPorEstado(EstadoVehiculo.DISPONIBLE);
        long vehiculosMantenimiento = dashboardRepository.contarVehiculosPorEstado(EstadoVehiculo.MANTENIMIENTO);
        long vehiculosRentados = dashboardRepository.contarVehiculosPorEstado(EstadoVehiculo.RENTADO);
        long totalFlota = dashboardRepository.count();

        KpiMetricsDto kpis = new KpiMetricsDto(
                rentasActivas,
                ingresosMes,
                vehiculosDisponibles,
                vehiculosMantenimiento
        );

        // 2. Ocupación de Flota
        double tasaOcupacion = totalFlota > 0
                ? Math.round(((double) vehiculosRentados / totalFlota) * 100.0 * 100.0) / 100.0
                : 0.0;

        OcupacionFlotaDto ocupacion = new OcupacionFlotaDto(
                tasaOcupacion,
                totalFlota,
                vehiculosDisponibles,
                vehiculosRentados,
                vehiculosMantenimiento
        );

        // 3. Gráficos
        LocalDateTime hace6Meses = LocalDate.now().minusMonths(6).withDayOfMonth(1).atStartOfDay();
        List<IngresoMensualDto> ingresosMensuales = dashboardRepository.obtenerIngresosMensuales(hace6Meses);
        List<VehiculoPopularDto> topVehiculos = dashboardRepository.obtenerTopVehiculos(PageRequest.of(0, 5));
        List<RentasPorSucursalDto> rentasPorSucursal = dashboardRepository.obtenerRentasPorSucursal();

        GraficosDto graficos = new GraficosDto(ingresosMensuales, topVehiculos, rentasPorSucursal);

        // 4. Alertas y Actividad
        long atrasadas = dashboardRepository.contarRentasAtrasadas(ahora);
        List<RentaResumenDto> ultimasRentas = dashboardRepository.obtenerUltimasRentas(PageRequest.of(0, 5));

        AlertasYActividadDto alertasYActividad = new AlertasYActividadDto(atrasadas, ultimasRentas);

        return new DashboardMetricsDto(kpis, ocupacion, graficos, alertasYActividad);
    }
}
