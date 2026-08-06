package com.sistemas.backend.Pagos.DTO;

import com.sistemas.backend.Pagos.Entity.EstadoPago;
import com.sistemas.backend.Pagos.Entity.MetodoPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagoDto {

    public record CreateRequest(
            @NotNull(message = "La renta es obligatoria")
            Long idRenta,

            @NotNull(message = "El monto es obligatorio")
            @Positive(message = "El monto debe ser mayor a 0")
            BigDecimal monto,

            @NotNull(message = "El método de pago es obligatorio")
            MetodoPago metodoPago,

            String referenciaTransaccion
    ) {}

    public record ReembolsoRequest(
            @NotNull(message = "El monto del reembolso es obligatorio")
            @Positive(message = "El monto a reembolsar debe ser mayor a 0")
            BigDecimal montoReembolso,

            @Size(max = 255, message = "El motivo no puede exceder los 255 caracteres")
            String motivo
    ) {}

    public record Response(
            Long id,
            Long idRenta,
            BigDecimal monto,
            LocalDateTime fechaPago,
            MetodoPago metodoPago,
            String referenciaTransaccion,
            EstadoPago estado
    ) {}
}
