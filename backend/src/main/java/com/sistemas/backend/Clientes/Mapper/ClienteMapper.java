package com.sistemas.backend.Clientes.Mapper;

import com.sistemas.backend.Clientes.DTO.ClienteDto;
import com.sistemas.backend.Clientes.Entity.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteDto.Request request) {
        return Cliente.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .email(request.email())
                .telefono(request.telefono())
                .numLicencia(request.numLicencia())
                .vencimientoLicencia(request.vencimientoLicencia())
                .build();
    }

    public ClienteDto.Response toResponse(Cliente entity) {
        String nombreCompleto = entity.getNombre() + " " + entity.getApellido();
        return new ClienteDto.Response(
                entity.getId(),
                nombreCompleto,
                entity.getEmail(),
                entity.getTelefono(),
                entity.getNumLicencia(),
                entity.getVencimientoLicencia(),
                entity.getActivo()
        );
    }
}