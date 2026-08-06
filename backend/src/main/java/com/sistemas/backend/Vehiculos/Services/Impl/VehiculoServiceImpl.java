package com.sistemas.backend.Vehiculos.Services.Impl;

import com.sistemas.backend.Clientes.DTO.ClienteDto;
import com.sistemas.backend.Clientes.Entity.Cliente;
import com.sistemas.backend.Exception.BusinessRuleException;
import com.sistemas.backend.Exception.ResourceNotFoundException;
import com.sistemas.backend.Sucursal.Entity.Sucursal;
import com.sistemas.backend.Sucursal.Repository.SucursalRepository;
import com.sistemas.backend.Vehiculos.DTO.CategoriaDto;
import com.sistemas.backend.Vehiculos.DTO.VehiculoDto;
import com.sistemas.backend.Vehiculos.Entity.CategoriaVehiculo;
import com.sistemas.backend.Vehiculos.Entity.EstadoVehiculo;
import com.sistemas.backend.Vehiculos.Entity.Vehiculo;
import com.sistemas.backend.Vehiculos.Mapper.VehiculoMapper;
import com.sistemas.backend.Vehiculos.Repository.CategoriaVehiculoRepository;
import com.sistemas.backend.Vehiculos.Repository.VehiculoRepository;
import com.sistemas.backend.Vehiculos.Repository.VehiculoSpecification;
import com.sistemas.backend.Vehiculos.Services.VehiculoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final CategoriaVehiculoRepository categoriaRepository;
    private final SucursalRepository sucursalRepository;
    private final VehiculoMapper vehiculoMapper;

    public VehiculoServiceImpl(VehiculoRepository vehiculoRepository, CategoriaVehiculoRepository categoriaRepository, SucursalRepository sucursalRepository, VehiculoMapper vehiculoMapper) {
        this.vehiculoRepository = vehiculoRepository;
        this.categoriaRepository = categoriaRepository;
        this.sucursalRepository = sucursalRepository;
        this.vehiculoMapper = vehiculoMapper;
    }

    @Override
    @Transactional
    public CategoriaDto.Response crearCategoria(CategoriaDto.Request request) {
        log.info("Creando nueva categoría de vehículo: {}", request.nombre());

        if (categoriaRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new BusinessRuleException("Ya existe una categoría con el nombre: " + request.nombre());
        }

        CategoriaVehiculo categoria = vehiculoMapper.toEntity(request);
        CategoriaVehiculo guardada = categoriaRepository.save(categoria);

        return vehiculoMapper.toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDto.Response> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(vehiculoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoriaDto.Response> listarCategoriasPaginadas(String termino, Boolean activo, Pageable pageable) {
        String terminoLimpio = (termino != null && !termino.trim().isEmpty()) ? termino.trim() : null;

        return categoriaRepository.buscarCategoriasPaginadas(terminoLimpio, activo, pageable)
                .map(vehiculoMapper::toResponse);
    }

    @Override
    @Transactional
    public CategoriaDto.Response actualizarCategoria(Integer id, CategoriaDto.Request request) {
        log.info("Actualizando categoría de vehículo con ID: {}", id);

        CategoriaVehiculo categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la categoría con ID: " + id));

        // Validar nombre duplicado solo si cambió el nombre
        if (!categoria.getNombre().equalsIgnoreCase(request.nombre()) &&
                categoriaRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new BusinessRuleException("Ya existe otra categoría con el nombre: " + request.nombre());
        }

        // Actualizar campos usando el mapper
        vehiculoMapper.updateEntityFromDto(request, categoria);

        CategoriaVehiculo actualizada = categoriaRepository.save(categoria);
        return vehiculoMapper.toResponse(actualizada);
    }

    @Override
    @Transactional
    public CategoriaDto.Response cambiarEstadoCategoria(Integer id, boolean activo) {
        log.info("Cambiando estado de actividad para la categoria ID: {} a {}", id, activo);

        CategoriaVehiculo categoriaVehiculo = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con ID: " + id));

        categoriaVehiculo.setActivo(activo);
        CategoriaVehiculo guardado = categoriaRepository.save(categoriaVehiculo);

        return vehiculoMapper.toResponse(guardado);
    }

    @Override
    @Transactional
    public void eliminarCategoria(Integer id) {
        log.info("Realizando borrado lógico de la categoría con ID: {}", id);

        CategoriaVehiculo categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la categoría con ID: " + id));

        // Borrado Lógico: Marcamos la entidad como inactiva
        categoria.setActivo(false);
        categoriaRepository.save(categoria);
    }

    @Override
    @Transactional
    public VehiculoDto.Response crearVehiculo(VehiculoDto.CreateRequest request) {
        log.info("Iniciando creación de vehículo con VIN: {}", request.vin());

        if (vehiculoRepository.existsByVin(request.vin())) {
            throw new BusinessRuleException("El VIN " + request.vin() + " ya se encuentra registrado.");
        }

        if (vehiculoRepository.existsByPlaca(request.placa())) {
            throw new BusinessRuleException("La placa " + request.placa() + " ya se encuentra registrada.");
        }

        CategoriaVehiculo categoria = categoriaRepository.findById(request.idCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada ID: " + request.idCategoria()));

        Sucursal sucursal = sucursalRepository.findById(request.idSucursalActual())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada ID: " + request.idSucursalActual()));

        Vehiculo vehiculo = vehiculoMapper.toEntity(request, categoria, sucursal);
        Vehiculo guardado = vehiculoRepository.save(vehiculo);

        log.info("Vehículo creado exitosamente con ID: {}", guardado.getId());
        return vehiculoMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoDto.Response obtenerPorId(Integer id) {
        log.debug("Buscando vehículo por ID: {}", id);
        return vehiculoRepository.findByIdWithDetails(id)
                .map(vehiculoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoDto.Response> listarDisponiblesPorSucursal(Integer idSucursal) {
        log.debug("Consultando vehículos disponibles para sucursal ID: {}", idSucursal);
        return vehiculoRepository.findBySucursalAndEstado(idSucursal, EstadoVehiculo.DISPONIBLE)
                .stream()
                .map(vehiculoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, EstadoVehiculo nuevoEstado) {
        log.info("Cambiando estado del vehículo ID: {} a {}", id, nuevoEstado);
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));

        vehiculo.setEstado(nuevoEstado);
        vehiculoRepository.save(vehiculo);
    }

    @Override
    @Transactional
    public VehiculoDto.Response actualizarVehiculo(Integer id, VehiculoDto.UpdateRequest request) {
        log.info("Actualizando vehículo con ID: {}", id);

        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));

        // Validar duplicidad de placa si cambió
        if (!vehiculo.getPlaca().equalsIgnoreCase(request.placa()) && vehiculoRepository.existsByPlaca(request.placa())) {
            throw new BusinessRuleException("La nueva placa " + request.placa() + " ya está registrada en otro vehículo.");
        }

        if (!vehiculo.getCategoria().getId().equals(request.idCategoria())) {
            CategoriaVehiculo nuevaCategoria = categoriaRepository.findById(request.idCategoria())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.idCategoria()));
            vehiculo.setCategoria(nuevaCategoria);
        }

        vehiculo.setPlaca(request.placa());
        vehiculo.setMarca(request.marca());
        vehiculo.setModelo(request.modelo());
        vehiculo.setAnio(request.anio());

        Vehiculo actualizado = vehiculoRepository.save(vehiculo);
        return vehiculoMapper.toResponse(actualizado);
    }

    @Override
    @Transactional
    public void trasladarSucursal(Integer idVehiculo, Integer idNuevaSucursal) {
        log.info("Trasladando vehículo ID: {} a la sucursal ID: {}", idVehiculo, idNuevaSucursal);

        Vehiculo vehiculo = vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + idVehiculo));

        if (vehiculo.getEstado() == EstadoVehiculo.RENTADO) {
            throw new BusinessRuleException("No se puede trasladar un vehículo que actualmente está en estado RENTADO.");
        }

        Sucursal nuevaSucursal = sucursalRepository.findById(idNuevaSucursal)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + idNuevaSucursal));

        vehiculo.setSucursalActual(nuevaSucursal);
        vehiculoRepository.save(vehiculo);
    }

    @Override
    @Transactional
    public void actualizarKilometraje(Integer idVehiculo, Integer nuevoKilometraje) {
        log.info("Actualizando kilometraje del vehículo ID: {} a {} km", idVehiculo, nuevoKilometraje);

        Vehiculo vehiculo = vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + idVehiculo));

        if (nuevoKilometraje < vehiculo.getKilometraje()) {
            throw new BusinessRuleException("El nuevo kilometraje no puede ser inferior al registrado previamente (" + vehiculo.getKilometraje() + " km).");
        }

        vehiculo.setKilometraje(nuevoKilometraje);

        // Regla opcional de negocio: enviar a mantenimiento preventivo cada 10,000 km
        if (nuevoKilometraje % 10000 == 0 && vehiculo.getEstado() == EstadoVehiculo.DISPONIBLE) {
            log.warn("El vehículo ID {} alcanzó un kilometraje modular de mantenimiento ({})", idVehiculo, nuevoKilometraje);
            vehiculo.setEstado(EstadoVehiculo.MANTENIMIENTO);
        }

        vehiculoRepository.save(vehiculo);
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoDto.Response obtenerPorPlaca(String placa) {
        log.debug("Buscando vehículo por placa: {}", placa);
        return vehiculoRepository.findByPlacaIgnoreCase(placa)
                .map(vehiculoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con placa: " + placa));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehiculoDto.Response> buscarConFiltros(
            Integer idSucursal, EstadoVehiculo estado, Integer idCategoria, String marca, Pageable pageable) {

        log.debug("Filtrando vehículos - Sucursal: {}, Estado: {}, Categoría: {}", idSucursal, estado, idCategoria);

        Specification<Vehiculo> spec = VehiculoSpecification.conFiltros(idSucursal, estado, idCategoria, marca);

        return vehiculoRepository.findAll(spec, pageable)
                .map(vehiculoMapper::toResponse);
    }
}
