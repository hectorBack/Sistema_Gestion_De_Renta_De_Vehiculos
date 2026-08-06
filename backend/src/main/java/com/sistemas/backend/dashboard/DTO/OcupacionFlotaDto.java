package com.sistemas.backend.dashboard.DTO;

public record OcupacionFlotaDto(double tasaOcupacionPorcentaje,
                                Long totalFlota,
                                Long disponibles,
                                Long rentados,
                                Long mantenimiento) {
}
