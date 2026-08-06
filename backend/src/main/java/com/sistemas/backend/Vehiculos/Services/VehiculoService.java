package com.sistemas.backend.Vehiculos.Services;

import com.sistemas.backend.Vehiculos.DTO.CategoriaDto;
import com.sistemas.backend.Vehiculos.DTO.VehiculoDto;
import com.sistemas.backend.Vehiculos.Entity.EstadoVehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VehiculoService {

    // Categorías
    CategoriaDto.Response crearCategoria(CategoriaDto.Request request);
    List<CategoriaDto.Response> listarCategorias();
    Page<CategoriaDto.Response> listarCategoriasPaginadas(String termino, Boolean activo, Pageable pageable);
    CategoriaDto.Response actualizarCategoria(Integer id, CategoriaDto.Request request);
    CategoriaDto.Response cambiarEstadoCategoria(Integer id, boolean activo);
    void eliminarCategoria(Integer id); // Borrado lógico

    // Vehículos
    VehiculoDto.Response crearVehiculo(VehiculoDto.CreateRequest request);
    VehiculoDto.Response obtenerPorId(Integer id);
    List<VehiculoDto.Response> listarDisponiblesPorSucursal(Integer idSucursal);
    void cambiarEstado(Integer id, EstadoVehiculo nuevoEstado);
    VehiculoDto.Response actualizarVehiculo(Integer id, VehiculoDto.UpdateRequest request);
    void trasladarSucursal(Integer idVehiculo, Integer idNuevaSucursal);
    void actualizarKilometraje(Integer idVehiculo, Integer nuevoKilometraje);
    VehiculoDto.Response obtenerPorPlaca(String placa);

    Page<VehiculoDto.Response> buscarConFiltros(
            Integer idSucursal, EstadoVehiculo estado, Integer idCategoria, String marca, Pageable pageable);
}
