package com.sistemas.backend.Clientes.Services;

import com.sistemas.backend.Clientes.DTO.ClienteDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ClienteService {

    ClienteDto.Response registrarCliente(ClienteDto.Request request);
    ClienteDto.Response obtenerPorId(Integer id);
    ClienteDto.Response obtenerPorLicencia(String numLicencia);
    Page<ClienteDto.Response> listarPaginado(Pageable pageable);
    Page<ClienteDto.Response> buscarConFiltros(String termino, Boolean activo, Pageable pageable);

    ClienteDto.Response actualizarCliente(Integer id, ClienteDto.Request request);

    ClienteDto.Response cambiarEstadoCliente(Integer id, boolean activo);

    void validarAptoParaRentar(Integer idCliente);
}
