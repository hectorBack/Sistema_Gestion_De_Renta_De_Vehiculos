package com.sistemas.backend;

import com.sistemas.backend.Clientes.DTO.ClienteDto;
import com.sistemas.backend.Clientes.Entity.Cliente;
import com.sistemas.backend.Clientes.Mapper.ClienteMapper;
import com.sistemas.backend.Clientes.Repository.ClienteRepository;
import com.sistemas.backend.Clientes.Services.Impl.ClienteServiceImpl;
import com.sistemas.backend.Exception.BusinessRuleException;
import com.sistemas.backend.Exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente clienteEjemplo;
    private ClienteDto.Request requestEjemplo;
    private ClienteDto.Response responseEjemplo;

    @BeforeEach
    void setUp() {
        // Datos dummy para reusar en las pruebas
        LocalDate fechaFutura = LocalDate.now().plusYears(2);

        clienteEjemplo = Cliente.builder()
                .id(1)
                .nombre("Héctor")
                .apellido("Servín")
                .email("hector@ejemplo.com")
                .telefono("1234567890")
                .numLicencia("LIC-12345")
                .vencimientoLicencia(fechaFutura)
                .activo(true)
                .build();

        requestEjemplo = new ClienteDto.Request(
                "Héctor",
                "Servín",
                "hector@ejemplo.com",
                "1234567890",
                "LIC-12345",
                fechaFutura
        );

        responseEjemplo = new ClienteDto.Response(
                1,
                "Héctor Salvador Servin Perez",
                "hector@ejemplo.com",
                "1234567890",
                "LIC-12345",
                fechaFutura,
                true
        );
    }

    // =======================================================
    //             REGISTRAR CLIENTE
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Registro de Cliente")
    class RegistrarClienteTests {

        @Test
        @DisplayName("Debe registrar un cliente exitosamente (Happy Path)")
        void registrarCliente_Exito() {
            // Arrange
            when(clienteRepository.findByEmail(requestEjemplo.email())).thenReturn(Optional.empty());
            when(clienteRepository.findByNumLicencia(requestEjemplo.numLicencia())).thenReturn(Optional.empty());
            when(clienteMapper.toEntity(requestEjemplo)).thenReturn(clienteEjemplo);
            when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteEjemplo);
            when(clienteMapper.toResponse(clienteEjemplo)).thenReturn(responseEjemplo);

            // Act
            ClienteDto.Response resultado = clienteService.registrarCliente(requestEjemplo);

            // Assert
            assertNotNull(resultado);
            assertEquals("hector@ejemplo.com", resultado.email());
            assertTrue(resultado.activo());
            verify(clienteRepository, times(1)).save(any(Cliente.class));
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException cuando el email ya existe")
        void registrarCliente_EmailDuplicado_LanzaExcepcion() {
            // Arrange
            when(clienteRepository.findByEmail(requestEjemplo.email())).thenReturn(Optional.of(clienteEjemplo));

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> clienteService.registrarCliente(requestEjemplo)
            );

            assertEquals("El correo electrónico ya está registrado por otro cliente.", ex.getMessage());
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException si la licencia está vencida al registrar")
        void registrarCliente_LicenciaVencida_LanzaExcepcion() {
            // Arrange
            LocalDate fechaPasada = LocalDate.now().minusDays(1);
            ClienteDto.Request requestConLicenciaVencida = new ClienteDto.Request(
                    "Carlos", "Gómez", "carlos@ejemplo.com", "9876543210", "LIC-999", fechaPasada
            );

            when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(clienteRepository.findByNumLicencia(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> clienteService.registrarCliente(requestConLicenciaVencida)
            );

            assertEquals("No se puede registrar/actualizar un cliente con una licencia de conducir ya vencida.", ex.getMessage());
            verify(clienteRepository, never()).save(any());
        }
    }

    // =======================================================
    //             OBTENER POR ID / LICENCIA
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Búsqueda de Clientes")
    class ObtenerClienteTests {

        @Test
        @DisplayName("Debe retornar un cliente cuando existe el ID (Happy Path)")
        void obtenerPorId_Exito() {
            // Arrange
            when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteEjemplo));
            when(clienteMapper.toResponse(clienteEjemplo)).thenReturn(responseEjemplo);

            // Act
            ClienteDto.Response resultado = clienteService.obtenerPorId(1);

            // Assert
            assertNotNull(resultado);
            assertEquals(1, resultado.id());
            verify(clienteRepository, times(1)).findById(1);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando no encuentra el ID")
        void obtenerPorId_NoEncontrado_LanzaExcepcion() {
            // Arrange
            when(clienteRepository.findById(99)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> clienteService.obtenerPorId(99)
            );

            assertEquals("Cliente no encontrado con ID: 99", ex.getMessage());
        }
    }

    // =======================================================
    //             PAGINACIÓN Y FILTROS
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Listado y Filtros Paginados")
    class ListarClientesTests {

        @Test
        @DisplayName("Debe retornar lista paginada de clientes")
        void listarPaginado_Exito() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Cliente> pageClientes = new PageImpl<>(List.of(clienteEjemplo));

            when(clienteRepository.findAll(pageable)).thenReturn(pageClientes);
            when(clienteMapper.toResponse(clienteEjemplo)).thenReturn(responseEjemplo);

            // Act
            Page<ClienteDto.Response> resultado = clienteService.listarPaginado(pageable);

            // Assert
            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            verify(clienteRepository, times(1)).findAll(pageable);
        }
    }

    // =======================================================
    //             VALIDAR APTO PARA RENTAR
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Validación Apto para Rentar")
    class ValidarAptoParaRentarTests {

        @Test
        @DisplayName("No debe lanzar excepción si el cliente está activo y con licencia vigente")
        void validarAptoParaRentar_ClienteApto_Exito() {
            // Arrange
            when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteEjemplo));

            // Act & Assert
            assertDoesNotThrow(() -> clienteService.validarAptoParaRentar(1));
            verify(clienteRepository, times(1)).findById(1);
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException si el cliente está inactivo")
        void validarAptoParaRentar_ClienteInactivo_LanzaExcepcion() {
            // Arrange
            clienteEjemplo.setActivo(false);
            when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteEjemplo));

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> clienteService.validarAptoParaRentar(1)
            );

            assertEquals("El cliente está inactivo o bloqueado en el sistema.", ex.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException si la licencia está vencida al consultar aptitud")
        void validarAptoParaRentar_LicenciaVencida_LanzaExcepcion() {
            // Arrange
            clienteEjemplo.setVencimientoLicencia(LocalDate.now().minusDays(1));
            when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteEjemplo));

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> clienteService.validarAptoParaRentar(1)
            );

            assertTrue(ex.getMessage().contains("La licencia de conducir del cliente está vencida."));
        }
    }
}
