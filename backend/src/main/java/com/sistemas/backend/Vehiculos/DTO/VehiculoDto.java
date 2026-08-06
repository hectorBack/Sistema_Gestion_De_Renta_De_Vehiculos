package com.sistemas.backend.Vehiculos.DTO;


import com.sistemas.backend.Vehiculos.Entity.EstadoVehiculo;
import jakarta.validation.constraints.*;

public class VehiculoDto {

    public record CreateRequest(
            @NotNull(message = "La categoría es obligatoria")
            Integer idCategoria,

            @NotNull(message = "La sucursal es obligatoria")
            Integer idSucursalActual,

            @NotBlank(message = "El VIN es obligatorio")
            @Size(min = 17, max = 17, message = "El VIN debe tener exactamente 17 caracteres")
            String vin,

            @NotBlank(message = "La placa es obligatoria")
            String placa,

            @NotBlank(message = "La marca es obligatoria")
            String marca,

            @NotBlank(message = "El modelo es obligatorio")
            String modelo,

            @NotNull(message = "El año es obligatorio")
            @Min(value = 2000, message = "Año no permitido")
            Integer anio,

            @NotNull(message = "El kilometraje es obligatorio")
            @PositiveOrZero(message = "El kilometraje debe ser mayor o igual a 0")
            Integer kilometraje
    ) {}

    public record UpdateRequest(
            @NotNull(message = "La categoría es obligatoria")
            Integer idCategoria,

            @NotBlank(message = "La placa es obligatoria")
            String placa,

            @NotBlank(message = "La marca es obligatoria")
            String marca,

            @NotBlank(message = "El modelo es obligatorio")
            String modelo,

            @NotNull(message = "El año es obligatorio")
            @Min(value = 2000, message = "Año no permitido")
            Integer anio
    ) {}

    public record Response(
            Integer id,
            String categoriaNombre,
            String sucursalNombre,
            String vin,
            String placa,
            String marca,
            String modelo,
            Integer anio,
            Integer kilometraje,
            EstadoVehiculo estado
    ) {}
}
