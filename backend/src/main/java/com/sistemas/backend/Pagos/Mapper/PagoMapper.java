package com.sistemas.backend.Pagos.Mapper;

import com.sistemas.backend.Pagos.DTO.PagoDto;
import com.sistemas.backend.Pagos.Entity.EstadoPago;
import com.sistemas.backend.Pagos.Entity.Pago;
import com.sistemas.backend.Rentas.Entity.Renta;
import org.springframework.stereotype.Component;

@Component
public class PagoMapper {

    public Pago toEntity(PagoDto.CreateRequest request, Renta renta) {
        return Pago.builder()
                .renta(renta)
                .monto(request.monto())
                .metodoPago(request.metodoPago())
                .referenciaTransaccion(request.referenciaTransaccion())
                .estado(EstadoPago.COMPLETADO)
                .build();
    }

    public PagoDto.Response toResponse(Pago entity) {
        return new PagoDto.Response(
                entity.getId(),
                entity.getRenta().getId(),
                entity.getMonto(),
                entity.getFechaPago(),
                entity.getMetodoPago(),
                entity.getReferenciaTransaccion(),
                entity.getEstado()
        );
    }
}
