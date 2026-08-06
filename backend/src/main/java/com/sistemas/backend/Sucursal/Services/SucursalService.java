package com.sistemas.backend.Sucursal.Services;

import com.sistemas.backend.Sucursal.DTO.SucursalDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SucursalService {

    SucursalDto.Response crearSucursal(SucursalDto.Request request);
    SucursalDto.Response obtenerPorId(Integer id);
    List<SucursalDto.Response> listarTodas();

    SucursalDto.Response actualizarSucursal(Integer id, SucursalDto.Request request);
    void eliminarSucursal(Integer id);
    List<SucursalDto.Response> buscarPorNombre(String termino);

    // Búsqueda paginada
    Page<SucursalDto.Response> buscarPorNombre(String termino, Pageable pageable);
    Page<SucursalDto.Response> listarPaginado(Pageable pageable);
}
