package com.sistemas.backend.Clientes.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ClienteDto {

    public record Request(
            @NotBlank(message = "El nombre es obligatorio")
            String nombre,

            @NotBlank(message = "El apellido es obligatorio")
            String apellido,

            @NotBlank(message = "El email es obligatorio")
            @Email(message = "El formato del email es incorrecto")
            String email,

            @NotBlank(message = "El teléfono es obligatorio")
            String telefono,

            @NotBlank(message = "La licencia de conducir es obligatoria")
            String numLicencia,

            @NotNull(message = "La fecha de vencimiento es obligatoria")
            @Future(message = "La licencia debe estar vigente")
            LocalDate vencimientoLicencia
    ) {}

    public record Response(
            Integer id,
            String nombreCompleto,
            String email,
            String telefono,
            String numLicencia,
            LocalDate vencimientoLicencia,
            Boolean activo
    ) {}
}
