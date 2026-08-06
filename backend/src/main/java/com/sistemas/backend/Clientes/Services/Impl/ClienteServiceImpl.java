package com.sistemas.backend.Clientes.Services.Impl;

import com.sistemas.backend.Clientes.DTO.ClienteDto;
import com.sistemas.backend.Clientes.Entity.Cliente;
import com.sistemas.backend.Clientes.Mapper.ClienteMapper;
import com.sistemas.backend.Clientes.Repository.ClienteRepository;
import com.sistemas.backend.Clientes.Services.ClienteService;
import com.sistemas.backend.Exception.BusinessRuleException;
import com.sistemas.backend.Exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public ClienteServiceImpl(ClienteRepository clienteRepository, ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    @Override
    @Transactional
    public ClienteDto.Response registrarCliente(ClienteDto.Request request) {
        log.info("Registrando nuevo cliente con email: {}", request.email());

        validarUnicidad(request.email(), request.numLicencia(), null);
        validarVigenciaLicencia(request.vencimientoLicencia());

        Cliente cliente = clienteMapper.toEntity(request);
        cliente.setActivo(true); // Estado inicial activo

        Cliente guardado = clienteRepository.save(cliente);

        log.info("Cliente registrado exitosamente con ID: {}", guardado.getId());
        return clienteMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDto.Response obtenerPorId(Integer id) {
        return clienteRepository.findById(id)
                .map(clienteMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDto.Response obtenerPorLicencia(String numLicencia) {
        log.debug("Buscando cliente por número de licencia: {}", numLicencia);
        return clienteRepository.findByNumLicencia(numLicencia)
                .map(clienteMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con Licencia: " + numLicencia));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDto.Response> listarPaginado(Pageable pageable) {
        log.debug("Consultando catálogo paginado de clientes. Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return clienteRepository.findAll(pageable)
                .map(clienteMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDto.Response> buscarConFiltros(String termino, Boolean activo, Pageable pageable) {
        log.debug("Filtrando clientes por término: '{}' y activo: '{}'", termino, activo);
        return clienteRepository.buscarPorCriterios(termino, activo, pageable)
                .map(clienteMapper::toResponse);
    }

    @Override
    @Transactional
    public ClienteDto.Response actualizarCliente(Integer id, ClienteDto.Request request) {
        log.info("Actualizando cliente con ID: {}", id);

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        validarUnicidad(request.email(), request.numLicencia(), id);
        validarVigenciaLicencia(request.vencimientoLicencia());

        cliente.setNombre(request.nombre());
        cliente.setApellido(request.apellido());
        cliente.setEmail(request.email());
        cliente.setTelefono(request.telefono());
        cliente.setNumLicencia(request.numLicencia());
        cliente.setVencimientoLicencia(request.vencimientoLicencia());

        Cliente actualizado = clienteRepository.save(cliente);
        log.info("Cliente ID: {} actualizado correctamente", actualizado.getId());

        return clienteMapper.toResponse(actualizado);
    }

    @Override
    @Transactional
    public ClienteDto.Response cambiarEstadoCliente(Integer id, boolean activo) {
        log.info("Cambiando estado de actividad para el cliente ID: {} a {}", id, activo);

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        cliente.setActivo(activo);
        Cliente guardado = clienteRepository.save(cliente);

        return clienteMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public void validarAptoParaRentar(Integer idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + idCliente));

        if (!cliente.getActivo()) {
            throw new BusinessRuleException("El cliente está inactivo o bloqueado en el sistema.");
        }

        if (cliente.getVencimientoLicencia().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("La licencia de conducir del cliente está vencida. Fecha de vencimiento: "
                    + cliente.getVencimientoLicencia());
        }
    }

    // =======================================================
    //                  MÉTODOS PRIVADOS DE VALIDACIÓN
    // =======================================================

    private void validarUnicidad(String email, String numLicencia, Integer idActual) {
        clienteRepository.findByEmail(email).ifPresent(c -> {
            if (idActual == null || !c.getId().equals(idActual)) {
                throw new BusinessRuleException("El correo electrónico ya está registrado por otro cliente.");
            }
        });

        clienteRepository.findByNumLicencia(numLicencia).ifPresent(c -> {
            if (idActual == null || !c.getId().equals(idActual)) {
                throw new BusinessRuleException("El número de licencia ya está registrado por otro cliente.");
            }
        });
    }

    private void validarVigenciaLicencia(LocalDate fechaVencimiento) {
        if (fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("No se puede registrar/actualizar un cliente con una licencia de conducir ya vencida.");
        }
    }

}
