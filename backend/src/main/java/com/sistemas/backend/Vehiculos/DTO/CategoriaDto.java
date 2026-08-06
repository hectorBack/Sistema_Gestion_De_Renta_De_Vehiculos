package com.sistemas.backend.Vehiculos.DTO;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CategoriaDto {

    public record Request(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 50, message = "Máximo 50 caracteres")
            String nombre,

            String descripcion,

            @NotNull(message = "La tarifa diaria es obligatoria")
            @Positive(message = "La tarifa debe ser mayor a 0")
            BigDecimal tarifaDiariaBase,

            @NotNull(message = "El depósito de garantía es obligatorio")
            @PositiveOrZero(message = "El depósito no puede ser negativo")
            BigDecimal depositoGarantia
    ) {}

    public record Response(
            Integer id,
            String nombre,
            String descripcion,
            BigDecimal tarifaDiariaBase,
            BigDecimal depositoGarantia,
            Boolean activo
    ) {}
}
