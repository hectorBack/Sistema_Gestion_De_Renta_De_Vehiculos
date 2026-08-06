package com.sistemas.backend.Rentas.Services;

import com.sistemas.backend.Rentas.DTO.RentaDto;
import com.sistemas.backend.Rentas.DTO.RentaResumenDto;
import com.sistemas.backend.Rentas.Entity.EstadoRenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentaServices {

    RentaDto.Response crearReserva(RentaDto.CreateRequest request);

    RentaDto.Response registrarDevolucion(Long idRenta, RentaDto.DevolucionRequest request);

    RentaDto.Response obtenerPorId(Long id);

    Page<RentaDto.Response> listarPaginado(Pageable pageable);

    RentaDto.Response iniciarRenta(Long idRenta); // Entrega de llaves
    RentaDto.Response cancelarReserva(Long idRenta, String motivo);
    Page<RentaDto.Response> buscarConFiltros(EstadoRenta estado, Integer idCliente, Pageable pageable);
    RentaResumenDto obtenerResumenDashboard();
}
