package com.sistemas.backend.dashboard.DTO;

import java.math.BigDecimal;

public record IngresoMensualDto(int anio,
                                int mes,
                                BigDecimal total) {
}
