package com.sistemas.backend.Rentas.Services.Impl;

import com.sistemas.backend.Clientes.Entity.Cliente;
import com.sistemas.backend.Clientes.Repository.ClienteRepository;
import com.sistemas.backend.Exception.BusinessRuleException;
import com.sistemas.backend.Exception.ResourceNotFoundException;
import com.sistemas.backend.Rentas.DTO.RentaDto;
import com.sistemas.backend.Rentas.Entity.EstadoRenta;
import com.sistemas.backend.Rentas.Entity.Renta;
import com.sistemas.backend.Rentas.Mapper.RentaMapper;
import com.sistemas.backend.Rentas.Repository.RentaRepository;
import com.sistemas.backend.Rentas.Services.RentaServices;
import com.sistemas.backend.Sucursal.Entity.Sucursal;
import com.sistemas.backend.Sucursal.Repository.SucursalRepository;
import com.sistemas.backend.Vehiculos.Entity.EstadoVehiculo;
import com.sistemas.backend.Vehiculos.Entity.Vehiculo;
import com.sistemas.backend.Vehiculos.Repository.VehiculoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
public class RentaServiceImpl implements RentaServices {

    private final RentaRepository rentaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ClienteRepository clienteRepository;
    private final SucursalRepository sucursalRepository;
    private final RentaMapper rentaMapper;

    public RentaServiceImpl(RentaRepository rentaRepository, VehiculoRepository vehiculoRepository, ClienteRepository clienteRepository, SucursalRepository sucursalRepository, RentaMapper rentaMapper) {
        this.rentaRepository = rentaRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.clienteRepository = clienteRepository;
        this.sucursalRepository = sucursalRepository;
        this.rentaMapper = rentaMapper;
    }

    @Override
    @Transactional
    public RentaDto.Response crearReserva(RentaDto.CreateRequest request) {
        log.info("Procesando creación de reserva para el cliente ID: {} con vehículo ID: {}",
                request.idCliente(), request.idVehiculo());

        // Validaciones del cliente y su licencia
        Cliente cliente = clienteRepository.findById(request.idCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado ID: " + request.idCliente()));

        if (cliente.getVencimientoLicencia().isBefore(LocalDate.now())) {
            log.warn("Rechazo de reserva: La licencia del cliente ID {} está vencida", cliente.getId());
            throw new BusinessRuleException("La licencia de conducir del cliente está vencida.");
        }

        // Estado del vehículo
        Vehiculo vehiculo = vehiculoRepository.findById(request.idVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado ID: " + request.idVehiculo()));

        if (vehiculo.getEstado() == EstadoVehiculo.FUERA_SERVICIO || vehiculo.getEstado() == EstadoVehiculo.MANTENIMIENTO) {
            throw new BusinessRuleException("El vehículo seleccionado no se encuentra operativo.");
        }

        // Verificación de solapamiento de fechas
        boolean estaOcupado = rentaRepository.existeSolapamientoReserva(
                request.idVehiculo(), request.fechaInicio(), request.fechaFinEstimada());

        if (estaOcupado) {
            throw new BusinessRuleException("El vehículo ya está reservado o rentado en el rango de fechas seleccionado.");
        }

        Sucursal retiro = sucursalRepository.findById(request.idSucursalRetiro())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal de retiro no encontrada ID: " + request.idSucursalRetiro()));

        Sucursal devolucion = sucursalRepository.findById(request.idSucursalDevolucion())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal de devolución no encontrada ID: " + request.idSucursalDevolucion()));

        // Cálculo de costo según tarifa base
        long diasRenta = Math.max(1, Duration.between(request.fechaInicio(), request.fechaFinEstimada()).toDays());
        BigDecimal costoEstimado = vehiculo.getCategoria().getTarifaDiariaBase()
                .multiply(BigDecimal.valueOf(diasRenta));

        // 🔴 CAMBIAR ESTADO A RENTADO DIRECTAMENTE
        vehiculo.setEstado(EstadoVehiculo.RENTADO);
        vehiculoRepository.save(vehiculo);

        Renta renta = rentaMapper.toEntity(request, vehiculo, cliente, retiro, devolucion, costoEstimado);
        renta.setEstado(EstadoRenta.ACTIVA);

        Renta guardada = rentaRepository.save(renta);
        log.info("Reserva #{} creada exitosamente. Total calculado: {}", guardada.getId(), costoEstimado);
        return rentaMapper.toResponse(guardada);
    }

    @Override
    @Transactional
    public RentaDto.Response registrarDevolucion(Long idRenta, RentaDto.DevolucionRequest request) {
        log.info("Registrando devolución para la renta #{}", idRenta);

        Renta renta = rentaRepository.findById(idRenta)
                .orElseThrow(() -> new ResourceNotFoundException("Renta no encontrada ID: " + idRenta));

        if (renta.getEstado() == EstadoRenta.COMPLETADA || renta.getEstado() == EstadoRenta.CANCELADA) {
            throw new BusinessRuleException("La renta no se encuentra activa para procesar devolución.");
        }

        if (request.kilometrajeFinal() < renta.getKilometrajeInicial()) {
            throw new BusinessRuleException("El kilometraje final no puede ser menor al kilometraje inicial.");
        }

        // Actualizar datos de renta
        renta.setFechaDevolucionReal(request.fechaDevolucionReal());
        renta.setKilometrajeFinal(request.kilometrajeFinal());

        // --- CÁLCULO DE RECARGOS POR MORA / TARDANZA ---
        BigDecimal costoFinal = calcularCostoFinalConMora(renta, request.fechaDevolucionReal());
        renta.setCostoTotal(costoFinal);
        renta.setEstado(EstadoRenta.COMPLETADA);

        // Actualizar vehículo a sucursal de llegada y liberar
        Vehiculo vehiculo = renta.getVehiculo();
        vehiculo.setKilometraje(request.kilometrajeFinal());
        vehiculo.setSucursalActual(renta.getSucursalDevolucion());
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);

        vehiculoRepository.save(vehiculo);
        Renta actualizada = rentaRepository.save(renta);

        log.info("Devolución de la renta #{} completada", idRenta);
        return rentaMapper.toResponse(actualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public RentaDto.Response obtenerPorId(Long id) {
        return rentaRepository.findByIdWithDetails(id)
                .map(rentaMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Renta no encontrada ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RentaDto.Response> listarPaginado(Pageable pageable) {
        log.debug("Consultando catálogo paginado de rentas. Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return rentaRepository.findAll(pageable)
                .map(rentaMapper::toResponse);
    }

    @Override
    @Transactional
    public RentaDto.Response iniciarRenta(Long idRenta) {
        log.info("Iniciando renta / entregando vehículo para la reserva #{}", idRenta);

        Renta renta = rentaRepository.findById(idRenta)
                .orElseThrow(() -> new ResourceNotFoundException("Renta no encontrada ID: " + idRenta));

        if (renta.getEstado() != EstadoRenta.RESERVADA) {
            throw new BusinessRuleException("Solo se pueden iniciar rentas en estado RESERVADA.");
        }

        Vehiculo vehiculo = renta.getVehiculo();

        // Registrar kilometraje de salida actual del odómetro
        renta.setKilometrajeInicial(vehiculo.getKilometraje());
        renta.setEstado(EstadoRenta.ACTIVA);

        // Cambiar estado del auto
        vehiculo.setEstado(EstadoVehiculo.RENTADO);
        vehiculoRepository.save(vehiculo);

        Renta actualizada = rentaRepository.save(renta);
        log.info("Renta #{} activada. Vehículo asignado con {} km.", idRenta, vehiculo.getKilometraje());
        return rentaMapper.toResponse(actualizada);
    }

    @Override
    @Transactional
    public RentaDto.Response cancelarReserva(Long idRenta, String motivo) {
        log.info("Cancelando reserva #{} por motivo: {}", idRenta, motivo);

        Renta renta = rentaRepository.findById(idRenta)
                .orElseThrow(() -> new ResourceNotFoundException("Renta no encontrada ID: " + idRenta));

        if (renta.getEstado() != EstadoRenta.RESERVADA) {
            throw new BusinessRuleException("Solo se pueden cancelar reservas que no hayan sido iniciadas aún.");
        }

        renta.setEstado(EstadoRenta.CANCELADA);
        Renta cancelada = rentaRepository.save(renta);

        log.info("Reserva #{} cancelada con éxito", idRenta);
        return rentaMapper.toResponse(cancelada);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RentaDto.Response> buscarConFiltros(EstadoRenta estado, Integer idCliente, Pageable pageable) {
        log.debug("Filtrando rentas por Estado: {} e idCliente: {}", estado, idCliente);
        return rentaRepository.buscarConFiltros(estado, idCliente, pageable)
                .map(rentaMapper::toResponse);
    }

    // --- Helper Method Privado de Dominio ---
    private BigDecimal calcularCostoFinalConMora(Renta renta, LocalDateTime fechaDevolucionReal) {
        BigDecimal costoBase = renta.getCostoTotal();

        // Si la entrega real supera la estimada por más de 1 hora, cobra recargo por día/hora extra
        if (fechaDevolucionReal.isAfter(renta.getFechaFinEstimada())) {
            long horasMora = Duration.between(renta.getFechaFinEstimada(), fechaDevolucionReal).toHours();
            if (horasMora > 1) {
                long diasExtra = (long) Math.ceil((double) horasMora / 24.0);
                BigDecimal tarifaDiaria = renta.getVehiculo().getCategoria().getTarifaDiariaBase();
                // Aplica 20% de penalización sobre los días extra entregados tarde
                BigDecimal recargo = tarifaDiaria.multiply(BigDecimal.valueOf(diasExtra)).multiply(BigDecimal.valueOf(1.20));

                log.warn("La renta #{} registra {} días de demora. Se aplica un recargo de {}",
                        renta.getId(), diasExtra, recargo);
                return costoBase.add(recargo);
            }
        }
        return costoBase;
    }
}
