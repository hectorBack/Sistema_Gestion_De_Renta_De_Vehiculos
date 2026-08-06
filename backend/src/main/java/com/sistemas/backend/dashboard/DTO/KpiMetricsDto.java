package com.sistemas.backend.dashboard.DTO;

import java.math.BigDecimal;

public record KpiMetricsDto(Long rentasActivas,
                            BigDecimal ingresosMesActual,
                            Long vehiculosDisponibles,
                            Long vehiculosEnMantenimiento) {
}
