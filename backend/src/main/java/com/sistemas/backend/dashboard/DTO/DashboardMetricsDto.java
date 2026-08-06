package com.sistemas.backend.dashboard.DTO;

public record DashboardMetricsDto(KpiMetricsDto kpis,
                                  OcupacionFlotaDto ocupacion,
                                  GraficosDto graficos,
                                  AlertasYActividadDto alertasYActividad) {
}
