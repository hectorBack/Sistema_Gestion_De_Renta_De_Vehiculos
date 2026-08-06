package com.sistemas.backend.Vehiculos.Mapper;

import com.sistemas.backend.Sucursal.Entity.Sucursal;
import com.sistemas.backend.Vehiculos.DTO.CategoriaDto;
import com.sistemas.backend.Vehiculos.DTO.VehiculoDto;
import com.sistemas.backend.Vehiculos.Entity.CategoriaVehiculo;
import com.sistemas.backend.Vehiculos.Entity.EstadoVehiculo;
import com.sistemas.backend.Vehiculos.Entity.Vehiculo;
import org.springframework.stereotype.Component;

@Component
public class VehiculoMapper {

    // --- Categoría ---
    public CategoriaVehiculo toEntity(CategoriaDto.Request request) {
        return CategoriaVehiculo.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .tarifaDiariaBase(request.tarifaDiariaBase())
                .depositoGarantia(request.depositoGarantia())
                .build();
    }

    public CategoriaDto.Response toResponse(CategoriaVehiculo entity) {
        return new CategoriaDto.Response(
                entity.getId(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getTarifaDiariaBase(),
                entity.getDepositoGarantia(),
                entity.getActivo()
        );
    }

    // Si haces el mapper de forma manual:
    public void updateEntityFromDto(CategoriaDto.Request request, CategoriaVehiculo entity) {
        if (request == null || entity == null) return;

        entity.setNombre(request.nombre());
        entity.setDescripcion(request.descripcion());
        entity.setTarifaDiariaBase(request.tarifaDiariaBase());
        entity.setDepositoGarantia(request.depositoGarantia());
    }

    // --- Vehículo ---
    public Vehiculo toEntity(VehiculoDto.CreateRequest request, CategoriaVehiculo categoria, Sucursal sucursal) {
        return Vehiculo.builder()
                .categoria(categoria)
                .sucursalActual(sucursal)
                .vin(request.vin())
                .placa(request.placa())
                .marca(request.marca())
                .modelo(request.modelo())
                .anio(request.anio())
                .kilometraje(request.kilometraje())
                .estado(EstadoVehiculo.DISPONIBLE)
                .build();
    }

    public VehiculoDto.Response toResponse(Vehiculo entity) {
        return new VehiculoDto.Response(
                entity.getId(),
                entity.getCategoria().getNombre(),
                entity.getSucursalActual().getNombre(),
                entity.getVin(),
                entity.getPlaca(),
                entity.getMarca(),
                entity.getModelo(),
                entity.getAnio(),
                entity.getKilometraje(),
                entity.getEstado()
        );
    }
}
