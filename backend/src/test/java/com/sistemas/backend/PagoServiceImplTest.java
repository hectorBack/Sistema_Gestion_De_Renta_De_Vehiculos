package com.sistemas.backend;

import com.sistemas.backend.Exception.BusinessRuleException;
import com.sistemas.backend.Pagos.DTO.PagoDto;
import com.sistemas.backend.Pagos.Entity.EstadoPago;
import com.sistemas.backend.Pagos.Entity.MetodoPago;
import com.sistemas.backend.Pagos.Entity.Pago;
import com.sistemas.backend.Pagos.Mapper.PagoMapper;
import com.sistemas.backend.Pagos.Repository.PagoRepository;
import com.sistemas.backend.Pagos.Services.Impl.PagoServiceImpl;
import com.sistemas.backend.Rentas.Entity.Renta;
import com.sistemas.backend.Rentas.Repository.RentaRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PagoServiceImplTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private RentaRepository rentaRepository;

    @Mock
    private PagoMapper pagoMapper;

    @InjectMocks
    private PagoServiceImpl pagoService;

    private Renta rentaEjemplo;
    private Pago pagoEjemplo;
    private PagoDto.CreateRequest createRequest;
    private PagoDto.ReembolsoRequest reembolsoRequest;
    private PagoDto.Response responseEjemplo;

    @BeforeEach
    void setUp() {
        rentaEjemplo = new Renta();
        rentaEjemplo.setId(100L);
        rentaEjemplo.setCostoTotal(new BigDecimal("1500.00"));

        pagoEjemplo = new Pago();
        pagoEjemplo.setId(1L);
        pagoEjemplo.setRenta(rentaEjemplo);
        pagoEjemplo.setMonto(new BigDecimal("500.00"));
        pagoEjemplo.setFechaPago(LocalDateTime.now());
        pagoEjemplo.setMetodoPago(MetodoPago.TARJETA_CREDITO);
        pagoEjemplo.setEstado(EstadoPago.COMPLETADO);

        createRequest = new PagoDto.CreateRequest(
                100L,
                new BigDecimal("500.00"),
                MetodoPago.TARJETA_CREDITO,
                "REF-123456"
        );

        reembolsoRequest = new PagoDto.ReembolsoRequest(
                new BigDecimal("500.00"),
                "Cliente canceló con anticipación"
        );

        responseEjemplo = new PagoDto.Response(
                1L,
                100L,
                new BigDecimal("500.00"),
                LocalDateTime.now(),
                MetodoPago.TARJETA_CREDITO,
                "REF-123456",
                EstadoPago.COMPLETADO
        );
    }

    // =======================================================
    //                 REGISTRAR PAGO
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Registro de Pagos")
    class RegistrarPagoTests {

        @Test
        @DisplayName("Debe registrar un pago exitosamente cuando el monto es válido y no excede el saldo (Happy Path)")
        void registrarPago_Exito() {
            // Arrange
            when(rentaRepository.findById(100L)).thenReturn(Optional.of(rentaEjemplo));
            when(pagoRepository.sumMontoByRentaIdAndEstado(100L, EstadoPago.COMPLETADO))
                    .thenReturn(Optional.of(new BigDecimal("200.00"))); // Saldo pendiente = 1300.00
            when(pagoMapper.toEntity(createRequest, rentaEjemplo)).thenReturn(pagoEjemplo);
            when(pagoRepository.save(any(Pago.class))).thenReturn(pagoEjemplo);
            when(pagoMapper.toResponse(pagoEjemplo)).thenReturn(responseEjemplo);

            // Act
            PagoDto.Response resultado = pagoService.registrarPago(createRequest);

            // Assert
            assertNotNull(resultado);
            assertEquals(1L, resultado.id());
            assertEquals(EstadoPago.COMPLETADO, resultado.estado());
            verify(pagoRepository, times(1)).save(any(Pago.class));
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException cuando el monto es menor o igual a cero")
        void registrarPago_MontoInvalido_LanzaExcepcion() {
            // Arrange
            PagoDto.CreateRequest requestMontoCero = new PagoDto.CreateRequest(
                    100L, BigDecimal.ZERO, MetodoPago.EFECTIVO, "REF-000"
            );

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> pagoService.registrarPago(requestMontoCero)
            );

            assertEquals("El monto del pago debe ser mayor a cero.", ex.getMessage());
            verify(pagoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException cuando el monto a pagar excede el saldo pendiente")
        void registrarPago_ExcedeSaldoPendiente_LanzaExcepcion() {
            // Arrange
            PagoDto.CreateRequest requestExcesivo = new PagoDto.CreateRequest(
                    100L, new BigDecimal("2000.00"), MetodoPago.TARJETA_CREDITO, "REF-OVER"
            );

            when(rentaRepository.findById(100L)).thenReturn(Optional.of(rentaEjemplo));
            when(pagoRepository.sumMontoByRentaIdAndEstado(100L, EstadoPago.COMPLETADO))
                    .thenReturn(Optional.of(new BigDecimal("0.00"))); // Saldo pendiente = 1500.00

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> pagoService.registrarPago(requestExcesivo)
            );

            assertTrue(ex.getMessage().contains("excede el saldo pendiente actual"));
            verify(pagoRepository, never()).save(any());
        }
    }

    // =======================================================
    //                PROCESAR REEMBOLSO
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Procesamiento de Reembolsos")
    class ProcesarReembolsoTests {

        @Test
        @DisplayName("Debe procesar un reembolso total correctamente (Happy Path)")
        void procesarReembolso_Total_Exito() {
            // Arrange
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoEjemplo));
            when(pagoRepository.save(any(Pago.class))).thenReturn(pagoEjemplo);
            when(pagoMapper.toResponse(pagoEjemplo)).thenReturn(responseEjemplo);

            // Act
            PagoDto.Response resultado = pagoService.procesarReembolso(1L, reembolsoRequest);

            // Assert
            assertNotNull(resultado);
            assertEquals(EstadoPago.REEMBOLSADO, pagoEjemplo.getEstado());
            assertEquals("Cliente canceló con anticipación", pagoEjemplo.getMotivoReembolso());
            verify(pagoRepository, times(1)).save(pagoEjemplo);
        }

        @Test
        @DisplayName("Debe procesar un reembolso parcial si el monto es menor al original")
        void procesarReembolso_Parcial_Exito() {
            // Arrange
            PagoDto.ReembolsoRequest requestParcial = new PagoDto.ReembolsoRequest(
                    new BigDecimal("200.00"), "Devolución parcial por cobro duplicado"
            );

            when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoEjemplo));
            when(pagoRepository.save(any(Pago.class))).thenReturn(pagoEjemplo);
            when(pagoMapper.toResponse(pagoEjemplo)).thenReturn(responseEjemplo);

            // Act
            pagoService.procesarReembolso(1L, requestParcial);

            // Assert
            assertEquals(EstadoPago.PARCIALMENTE_REEMBOLSADO, pagoEjemplo.getEstado());
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException si se intenta reembolsar un pago que no está COMPLETADO")
        void procesarReembolso_EstadoInvalido_LanzaExcepcion() {
            // Arrange
            pagoEjemplo.setEstado(EstadoPago.PENDIENTE);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoEjemplo));

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> pagoService.procesarReembolso(1L, reembolsoRequest)
            );

            assertEquals("Solo se pueden reembolsar pagos en estado COMPLETADO.", ex.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException si el monto de reembolso supera el monto pagado")
        void procesarReembolso_MontoExcesivo_LanzaExcepcion() {
            // Arrange
            PagoDto.ReembolsoRequest requestInvalido = new PagoDto.ReembolsoRequest(
                    new BigDecimal("1000.00"), "Intento de reembolso mayor"
            );

            when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoEjemplo));

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> pagoService.procesarReembolso(1L, requestInvalido)
            );

            assertEquals("El monto a reembolsar no puede superar el monto pagado originalmente.", ex.getMessage());
        }
    }

    // =======================================================
    //             CONSULTAS Y SALDO PENDIENTE
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Consultas de Pagos")
    class ConsultasPagoTests {

        @Test
        @DisplayName("Debe calcular correctamente el saldo pendiente de una renta")
        void obtenerSaldoPendiente_Exito() {
            // Arrange
            when(rentaRepository.findById(100L)).thenReturn(Optional.of(rentaEjemplo)); // Costo: 1500.00
            when(pagoRepository.sumMontoByRentaIdAndEstado(100L, EstadoPago.COMPLETADO))
                    .thenReturn(Optional.of(new BigDecimal("500.00")));

            // Act
            BigDecimal saldo = pagoService.obtenerSaldoPendiente(100L);

            // Assert
            assertEquals(new BigDecimal("1000.00"), saldo);
        }

        @Test
        @DisplayName("Debe retornar lista paginada de pagos")
        void listarPaginado_Exito() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Pago> pagePagos = new PageImpl<>(List.of(pagoEjemplo));

            when(pagoRepository.findAll(pageable)).thenReturn(pagePagos);
            when(pagoMapper.toResponse(pagoEjemplo)).thenReturn(responseEjemplo);

            // Act
            Page<PagoDto.Response> resultado = pagoService.listarPaginado(pageable);

            // Assert
            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
        }
    }
}
