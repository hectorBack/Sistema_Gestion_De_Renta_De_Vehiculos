package com.sistemas.backend.Sucursal.Services.Impl;

import com.sistemas.backend.Exception.BusinessRuleException;
import com.sistemas.backend.Exception.ResourceNotFoundException;
import com.sistemas.backend.Sucursal.DTO.SucursalDto;
import com.sistemas.backend.Sucursal.Entity.Sucursal;
import com.sistemas.backend.Sucursal.Mapper.SucursalMapper;
import com.sistemas.backend.Sucursal.Repository.SucursalRepository;
import com.sistemas.backend.Sucursal.Services.SucursalService;
import com.sistemas.backend.Vehiculos.Repository.VehiculoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class SucursalServiceImpl implements SucursalService {

    private final SucursalRepository sucursalRepository;
    private final SucursalMapper sucursalMapper;
    private final VehiculoRepository vehiculoRepository;

    public SucursalServiceImpl(SucursalRepository sucursalRepository, SucursalMapper sucursalMapper, VehiculoRepository vehiculoRepository) {
        this.sucursalRepository = sucursalRepository;
        this.sucursalMapper = sucursalMapper;
        this.vehiculoRepository = vehiculoRepository;
    }

    @Override
    @Transactional
    public SucursalDto.Response crearSucursal(SucursalDto.Request request) {
        log.info("Creando sucursal: {}", request.nombre());

        if (sucursalRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new BusinessRuleException("Ya existe una sucursal con el nombre: " + request.nombre());
        }

        Sucursal sucursal = sucursalMapper.toEntity(request);
        Sucursal guardada = sucursalRepository.save(sucursal);

        return sucursalMapper.toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public SucursalDto.Response obtenerPorId(Integer id) {
        return sucursalRepository.findById(id)
                .map(sucursalMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto.Response> listarTodas() {
        return sucursalRepository.findAll().stream()
                .map(sucursalMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SucursalDto.Response actualizarSucursal(Integer id, SucursalDto.Request request) {
        log.info("Actualizando sucursal con ID: {}", id);

        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + id));

        // Validar no duplicar el nombre con OTRA sucursal existente
        if (!sucursal.getNombre().equalsIgnoreCase(request.nombre())
                && sucursalRepository.existsByNombreIgnoreCase(request.nombre())) {
            log.warn("Conflicto de actualización: El nombre {} ya está en uso por otra sucursal", request.nombre());
            throw new BusinessRuleException("Ya existe otra sucursal con el nombre: " + request.nombre());
        }

        sucursal.setNombre(request.nombre());
        sucursal.setDireccion(request.direccion());
        sucursal.setTelefono(request.telefono());
        sucursal.setEmail(request.email());

        Sucursal actualizada = sucursalRepository.save(sucursal);
        log.info("Sucursal ID: {} actualizada correctamente", actualizada.getId());

        return sucursalMapper.toResponse(actualizada);
    }

    @Override
    @Transactional
    public void eliminarSucursal(Integer id) {
        log.info("Iniciando proceso de eliminación para sucursal ID: {}", id);

        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + id));

        // Regla de integridad de negocio: No se borra si tiene vehículos radicados allí
        boolean tieneVehiculos = vehiculoRepository.existsBySucursalActualId(id);
        if (tieneVehiculos) {
            log.warn("No se puede eliminar la sucursal ID: {} porque posee vehículos asignados", id);
            throw new BusinessRuleException("No es posible eliminar la sucursal porque tiene vehículos asignados. Reasigne los vehículos antes de continuar.");
        }

        sucursalRepository.delete(sucursal);
        log.info("Sucursal ID: {} eliminada exitosamente", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto.Response> buscarPorNombre(String termino) {
        log.debug("Buscando sucursales por término: {}", termino);
        return sucursalRepository.findByNombreContainingIgnoreCase(termino).stream()
                .map(sucursalMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SucursalDto.Response> buscarPorNombre(String termino, Pageable pageable) {
        log.debug("Buscando sucursales paginadas por término: '{}'", termino);

        return sucursalRepository.findByNombreContainingIgnoreCase(termino, pageable)
                .map(sucursalMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SucursalDto.Response> listarPaginado(Pageable pageable) {
        log.debug("Consultando catálogo paginado de sucursales. Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return sucursalRepository.findAll(pageable)
                .map(sucursalMapper::toResponse);
    }

}
