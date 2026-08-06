package com.sistemas.backend.Rentas.DTO;

import java.math.BigDecimal;

public record RentaResumenDto(long totalRentasHoy,
                              long activas,
                              long completadas,
                              long canceladas,
                              long reservadas,
                              BigDecimal ingresosDelDia) {
}
