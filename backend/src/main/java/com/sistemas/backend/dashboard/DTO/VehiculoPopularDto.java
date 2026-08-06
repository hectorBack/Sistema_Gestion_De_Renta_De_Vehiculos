package com.sistemas.backend.dashboard.DTO;

public record VehiculoPopularDto(String marca,
                                 String modelo,
                                 String placa,
                                 Long totalRentas) {
}
