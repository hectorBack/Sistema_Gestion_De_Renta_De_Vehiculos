package com.sistemas.backend.Rentas.Mapper;

import com.sistemas.backend.Clientes.Entity.Cliente;
import com.sistemas.backend.Rentas.DTO.RentaDto;
import com.sistemas.backend.Rentas.Entity.EstadoRenta;
import com.sistemas.backend.Rentas.Entity.Renta;
import com.sistemas.backend.Sucursal.Entity.Sucursal;
import com.sistemas.backend.Vehiculos.Entity.Vehiculo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RentaMapper {

    public Renta toEntity(RentaDto.CreateRequest request, Vehiculo vehiculo, Cliente cliente,
                          Sucursal retiro, Sucursal devolucion, BigDecimal costoEstimado) {
        return Renta.builder()
                .vehiculo(vehiculo)
                .cliente(cliente)
                .sucursalRetiro(retiro)
                .sucursalDevolucion(devolucion)
                .fechaInicio(request.fechaInicio())
                .fechaFinEstimada(request.fechaFinEstimada())
                .kilometrajeInicial(request.kilometrajeInicial())
                .costoTotal(costoEstimado)
                .estado(EstadoRenta.RESERVADA)
                .build();
    }

    public RentaDto.Response toResponse(Renta entity) {
        String vehiculoDetalle = String.format("%s %s (%s)",
                entity.getVehiculo().getMarca(),
                entity.getVehiculo().getModelo(),
                entity.getVehiculo().getPlaca());

        String clienteNombre = entity.getCliente().getNombre() + " " + entity.getCliente().getApellido();

        return new RentaDto.Response(
                entity.getId(),
                vehiculoDetalle,
                clienteNombre,
                entity.getSucursalRetiro().getNombre(),
                entity.getSucursalDevolucion().getNombre(),
                entity.getFechaInicio(),
                entity.getFechaFinEstimada(),
                entity.getFechaDevolucionReal(),
                entity.getKilometrajeInicial(),
                entity.getKilometrajeFinal(),
                entity.getCostoTotal(),
                entity.getEstado()
        );
    }
}
