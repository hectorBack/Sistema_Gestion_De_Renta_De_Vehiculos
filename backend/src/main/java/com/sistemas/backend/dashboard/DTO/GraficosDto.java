package com.sistemas.backend.dashboard.DTO;

import java.util.List;

public record GraficosDto(List<IngresoMensualDto> ingresosMensuales,
                          List<VehiculoPopularDto> topVehiculosMasRentados,
                          List<RentasPorSucursalDto> rentasPorSucursal) {
}
