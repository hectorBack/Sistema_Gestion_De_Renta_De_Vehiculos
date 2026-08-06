package com.sistemas.backend.dashboard.DTO;

import java.time.LocalDateTime;

public record RentaResumenDto(Long idRenta,
                              String clienteNombre,
                              String vehiculoInfo,
                              LocalDateTime fechaInicio,
                              String estado) {
}
