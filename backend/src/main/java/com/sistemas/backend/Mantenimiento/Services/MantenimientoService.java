package com.sistemas.backend.Mantenimiento.Services;

import com.sistemas.backend.Clientes.DTO.ClienteDto;
import com.sistemas.backend.Mantenimiento.DTO.MantenimientoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MantenimientoService {

    MantenimientoDto.Response crearMantenimiento(MantenimientoDto.Request request);
    MantenimientoDto.Response obtenerPorId(Long id);
    Page<MantenimientoDto.Response> listarPaginados(String termino, Integer idVehiculo, Boolean activo, Pageable pageable);
    MantenimientoDto.Response actualizarMantenimiento(Long id, MantenimientoDto.Request request);
    MantenimientoDto.Response cambiarEstadoMantenimiento(Long id, boolean activo);
    void eliminarMantenimiento(Long id); // Borrado lógico
}
