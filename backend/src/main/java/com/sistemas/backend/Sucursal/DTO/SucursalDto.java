package com.sistemas.backend.Sucursal.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SucursalDto {

    public record Request(
            @NotBlank(message = "El nombre es obligatorio")
            String nombre,

            @NotBlank(message = "La dirección es obligatoria")
            String direccion,

            String telefono,

            @Email(message = "El formato de email no es válido")
            String email
    ) {}

    public record Response(
            Integer id,
            String nombre,
            String direccion,
            String telefono,
            String email
    ) {}
}
