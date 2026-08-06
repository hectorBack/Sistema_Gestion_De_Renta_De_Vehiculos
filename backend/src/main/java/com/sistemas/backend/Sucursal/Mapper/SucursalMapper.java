package com.sistemas.backend.Sucursal.Mapper;

import com.sistemas.backend.Sucursal.DTO.SucursalDto;
import com.sistemas.backend.Sucursal.Entity.Sucursal;
import org.springframework.stereotype.Component;

@Component
public class SucursalMapper {

    public Sucursal toEntity(SucursalDto.Request request) {
        return Sucursal.builder()
                .nombre(request.nombre())
                .direccion(request.direccion())
                .telefono(request.telefono())
                .email(request.email())
                .build();
    }

    public SucursalDto.Response toResponse(Sucursal entity) {
        return new SucursalDto.Response(
                entity.getId(),
                entity.getNombre(),
                entity.getDireccion(),
                entity.getTelefono(),
                entity.getEmail()
        );
    }
}