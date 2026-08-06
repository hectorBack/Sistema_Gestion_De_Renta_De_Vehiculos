package com.sistemas.backend.Mantenimiento.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MantenimientoDto {

    public record Request(
            @NotNull(message = "El vehículo es obligatorio")
            Integer idVehiculo,

            @NotBlank(message = "El tipo de mantenimiento es obligatorio")
            @Size(max = 50, message = "Máximo 50 caracteres para el tipo")
            String tipo,

            @NotNull(message = "El costo es obligatorio")
            @PositiveOrZero(message = "El costo no puede ser negativo")
            BigDecimal costo,

            @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
            String descripcion,

            @NotNull(message = "La fecha de mantenimiento es obligatoria")
            LocalDate fechaMantenimiento
    ) {}

    public record Response(
            Long id,
            Integer idVehiculo,
            String placaVehiculo,
            String vehiculoModeloInfo, // Ej: "Toyota Corolla (ABC-123)"
            String tipo,
            BigDecimal costo,
            String descripcion,
            LocalDate fechaMantenimiento,
            Boolean activo
    ) {}
}
