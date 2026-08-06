package com.sistemas.backend.Rentas.DTO;

import com.sistemas.backend.Rentas.Entity.EstadoRenta;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RentaDto {

    public record CreateRequest(
            @NotNull(message = "El vehículo es obligatorio")
            Integer idVehiculo,

            @NotNull(message = "El cliente es obligatorio")
            Integer idCliente,

            @NotNull(message = "La sucursal de retiro es obligatoria")
            Integer idSucursalRetiro,

            @NotNull(message = "La sucursal de devolución es obligatoria")
            Integer idSucursalDevolucion,

            @NotNull(message = "La fecha de inicio es obligatoria")
            @FutureOrPresent(message = "La fecha de inicio no puede ser en el pasado")
            LocalDateTime fechaInicio,

            @NotNull(message = "La fecha fin estimada es obligatoria")
            LocalDateTime fechaFinEstimada,

            @NotNull(message = "El kilometraje inicial es obligatorio")
            @PositiveOrZero
            Integer kilometrajeInicial
    ) {}

    public record DevolucionRequest(
            @NotNull(message = "La fecha real de devolución es obligatoria")
            LocalDateTime fechaDevolucionReal,

            @NotNull(message = "El kilometraje final es obligatorio")
            Integer kilometrajeFinal
    ) {}

    public record Response(
            Long id,
            String vehiculoDetalle, // Ej: "Toyota Corolla (ABC-123)"
            String clienteNombre,
            String sucursalRetiro,
            String sucursalDevolucion,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFinEstimada,
            LocalDateTime fechaDevolucionReal,
            Integer kilometrajeInicial,
            Integer kilometrajeFinal,
            BigDecimal costoTotal,
            EstadoRenta estado
    ) {}
}
