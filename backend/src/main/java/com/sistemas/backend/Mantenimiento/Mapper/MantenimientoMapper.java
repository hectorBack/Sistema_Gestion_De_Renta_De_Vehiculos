package com.sistemas.backend.Mantenimiento.Mapper;

import com.sistemas.backend.Mantenimiento.DTO.MantenimientoDto;
import com.sistemas.backend.Mantenimiento.Entity.Mantenimiento;
import com.sistemas.backend.Vehiculos.Entity.Vehiculo;
import org.springframework.stereotype.Component;

@Component
public class MantenimientoMapper {

    public Mantenimiento toEntity(MantenimientoDto.Request request, Vehiculo vehiculo) {
        if (request == null) return null;

        return Mantenimiento.builder()
                .vehiculo(vehiculo)
                .tipo(request.tipo())
                .costo(request.costo())
                .descripcion(request.descripcion())
                .fechaMantenimiento(request.fechaMantenimiento())
                .activo(true)
                .build();
    }

    public MantenimientoDto.Response toResponse(Mantenimiento entity) {
        if (entity == null) return null;

        String modeloInfo = entity.getVehiculo() != null
                ? entity.getVehiculo().getMarca() + " " + entity.getVehiculo().getModelo() + " (" + entity.getVehiculo().getPlaca() + ")"
                : "N/A";

        return new MantenimientoDto.Response(
                entity.getId(),
                entity.getVehiculo() != null ? entity.getVehiculo().getId() : null,
                entity.getVehiculo() != null ? entity.getVehiculo().getPlaca() : null,
                modeloInfo,
                entity.getTipo(),
                entity.getCosto(),
                entity.getDescripcion(),
                entity.getFechaMantenimiento(),
                entity.getActivo()
        );
    }

    public void updateEntityFromDto(MantenimientoDto.Request request, Mantenimiento entity, Vehiculo vehiculo) {
        if (request == null || entity == null) return;

        entity.setVehiculo(vehiculo);
        entity.setTipo(request.tipo());
        entity.setCosto(request.costo());
        entity.setDescripcion(request.descripcion());
        entity.setFechaMantenimiento(request.fechaMantenimiento());
    }
}
