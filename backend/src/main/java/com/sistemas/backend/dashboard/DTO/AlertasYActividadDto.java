package com.sistemas.backend.dashboard.DTO;

import java.util.List;

public record AlertasYActividadDto(Long devolucionesAtrasadasCount,
                                   List<RentaResumenDto> ultimasRentas) {
}
