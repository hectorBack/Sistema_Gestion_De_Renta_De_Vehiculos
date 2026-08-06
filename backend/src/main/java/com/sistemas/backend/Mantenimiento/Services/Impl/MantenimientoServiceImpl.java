package com.sistemas.backend.Mantenimiento.Services.Impl;

import com.sistemas.backend.Clientes.DTO.ClienteDto;
import com.sistemas.backend.Clientes.Entity.Cliente;
import com.sistemas.backend.Exception.BusinessRuleException;
import com.sistemas.backend.Exception.ResourceNotFoundException;
import com.sistemas.backend.Mantenimiento.DTO.MantenimientoDto;
import com.sistemas.backend.Mantenimiento.Entity.Mantenimiento;
import com.sistemas.backend.Mantenimiento.Mapper.MantenimientoMapper;
import com.sistemas.backend.Mantenimiento.Repository.MantenimientoRepository;
import com.sistemas.backend.Mantenimiento.Services.MantenimientoService;
import com.sistemas.backend.Vehiculos.Entity.EstadoVehiculo;
import com.sistemas.backend.Vehiculos.Entity.Vehiculo;
import com.sistemas.backend.Vehiculos.Repository.VehiculoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class MantenimientoServiceImpl implements MantenimientoService {

    private final MantenimientoRepository mantenimientoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final MantenimientoMapper mantenimientoMapper;

    public MantenimientoServiceImpl(MantenimientoRepository mantenimientoRepository,
                                    VehiculoRepository vehiculoRepository,
                                    MantenimientoMapper mantenimientoMapper) {
        this.mantenimientoRepository = mantenimientoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.mantenimientoMapper = mantenimientoMapper;
    }

    @Override
    @Transactional
    public MantenimientoDto.Response crearMantenimiento(MantenimientoDto.Request request) {
        log.info("Registrando nuevo mantenimiento para el vehículo ID: {}", request.idVehiculo());

        Vehiculo vehiculo = vehiculoRepository.findById(request.idVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("No existe el vehículo con ID: " + request.idVehiculo()));

        // 🔴 VALIDACIÓN: No enviar a mantenimiento un vehículo que está actualmente RENTADO
        if (vehiculo.getEstado() == EstadoVehiculo.RENTADO) {
            throw new BusinessRuleException("No se puede registrar un mantenimiento para el vehículo con placa "
                    + vehiculo.getPlaca() + " porque actualmente se encuentra RENTADO.");
        }

        // 🔴 LÍNEAS CLAVE: Cambiar el estado del vehículo
        vehiculo.setEstado(EstadoVehiculo.MANTENIMIENTO);
        vehiculoRepository.save(vehiculo);

        Mantenimiento mantenimiento = mantenimientoMapper.toEntity(request, vehiculo);
        mantenimiento.setActivo(true);
        Mantenimiento guardado = mantenimientoRepository.save(mantenimiento);

        return mantenimientoMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public MantenimientoDto.Response obtenerPorId(Long id) {
        Mantenimiento mantenimiento = mantenimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el mantenimiento con ID: " + id));

        return mantenimientoMapper.toResponse(mantenimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MantenimientoDto.Response> listarPaginados(String termino, Integer idVehiculo, Boolean activo, Pageable pageable) {
        String terminoLimpio = (termino != null && !termino.trim().isEmpty()) ? termino.trim() : null;

        return mantenimientoRepository.buscarPaginados(terminoLimpio, idVehiculo, activo, pageable)
                .map(mantenimientoMapper::toResponse);
    }

    @Override
    @Transactional
    public MantenimientoDto.Response actualizarMantenimiento(Long id, MantenimientoDto.Request request) {
        log.info("Actualizando mantenimiento con ID: {}", id);

        Mantenimiento mantenimiento = mantenimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el mantenimiento con ID: " + id));

        Vehiculo vehiculoActual = mantenimiento.getVehiculo();

        // Si el mantenimiento cambió de vehículo, actualizamos los estados de ambos
        if (!vehiculoActual.getId().equals(request.idVehiculo())) {
            Vehiculo nuevoVehiculo = vehiculoRepository.findById(request.idVehiculo())
                    .orElseThrow(() -> new ResourceNotFoundException("No existe el vehículo con ID: " + request.idVehiculo()));

            // El vehículo anterior vuelve a estar disponible (si el mantenimiento estaba activo)
            if (Boolean.TRUE.equals(mantenimiento.getActivo())) {
                vehiculoActual.setEstado(EstadoVehiculo.DISPONIBLE);
                vehiculoRepository.save(vehiculoActual);

                nuevoVehiculo.setEstado(EstadoVehiculo.MANTENIMIENTO);
                vehiculoRepository.save(nuevoVehiculo);
            }

            mantenimientoMapper.updateEntityFromDto(request, mantenimiento, nuevoVehiculo);
        } else {
            mantenimientoMapper.updateEntityFromDto(request, mantenimiento, vehiculoActual);
        }

        Mantenimiento actualizado = mantenimientoRepository.save(mantenimiento);

        return mantenimientoMapper.toResponse(actualizado);
    }

    @Override
    @Transactional
    public MantenimientoDto.Response cambiarEstadoMantenimiento(Long id, boolean activo) {
        log.info("Cambiando estado de actividad para el cliente ID: {} a {}", id, activo);

        Mantenimiento mantenimiento = mantenimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mantenimiento no encontrado con ID: " + id));

        mantenimiento.setActivo(activo);

        // Actualizar estado del vehículo asociado
        Vehiculo vehiculo = mantenimiento.getVehiculo();
        if (activo) {
            vehiculo.setEstado(EstadoVehiculo.MANTENIMIENTO);
        } else {
            vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        }
        vehiculoRepository.save(vehiculo);

        Mantenimiento guardado = mantenimientoRepository.save(mantenimiento);

        return mantenimientoMapper.toResponse(guardado);
    }

    @Override
    @Transactional
    public void eliminarMantenimiento(Long id) {
        log.info("Ejecutando borrado lógico para el mantenimiento con ID: {}", id);

        Mantenimiento mantenimiento = mantenimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el mantenimiento con ID: " + id));

        mantenimiento.setActivo(false);

        // El vehículo vuelve a quedar disponible
        Vehiculo vehiculo = mantenimiento.getVehiculo();
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculoRepository.save(vehiculo);

        mantenimientoRepository.save(mantenimiento);
    }
}
