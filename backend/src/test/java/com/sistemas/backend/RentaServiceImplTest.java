package com.sistemas.backend;

import com.sistemas.backend.Clientes.Entity.Cliente;
import com.sistemas.backend.Clientes.Repository.ClienteRepository;
import com.sistemas.backend.Exception.BusinessRuleException;
import com.sistemas.backend.Exception.ResourceNotFoundException;
import com.sistemas.backend.Rentas.DTO.RentaDto;
import com.sistemas.backend.Rentas.Entity.EstadoRenta;
import com.sistemas.backend.Rentas.Entity.Renta;
import com.sistemas.backend.Rentas.Mapper.RentaMapper;
import com.sistemas.backend.Rentas.Repository.RentaRepository;
import com.sistemas.backend.Rentas.Services.Impl.RentaServiceImpl;
import com.sistemas.backend.Sucursal.Entity.Sucursal;
import com.sistemas.backend.Sucursal.Repository.SucursalRepository;
import com.sistemas.backend.Vehiculos.Entity.CategoriaVehiculo;
import com.sistemas.backend.Vehiculos.Entity.EstadoVehiculo;
import com.sistemas.backend.Vehiculos.Entity.Vehiculo;
import com.sistemas.backend.Vehiculos.Repository.VehiculoRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RentaServiceImplTest {

    @Mock
    private RentaRepository rentaRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private RentaMapper rentaMapper;

    @InjectMocks
    private RentaServiceImpl rentaService;

    private Cliente clienteEjemplo;
    private Vehiculo vehiculoEjemplo;
    private CategoriaVehiculo categoriaEjemplo;
    private Sucursal sucursalRetiro;
    private Sucursal sucursalDevolucion;
    private Renta rentaEjemplo;
    private RentaDto.CreateRequest createRequest;
    private RentaDto.Response responseEjemplo;

    private final LocalDateTime fechaInicio = LocalDateTime.now().plusDays(1);
    private final LocalDateTime fechaFin = LocalDateTime.now().plusDays(4); // 3 días

    @BeforeEach
    void setUp() {
        clienteEjemplo = new Cliente();
        clienteEjemplo.setId(10);
        clienteEjemplo.setVencimientoLicencia(LocalDate.now().plusYears(2));

        categoriaEjemplo = new CategoriaVehiculo();
        categoriaEjemplo.setId(1);
        categoriaEjemplo.setTarifaDiariaBase(new BigDecimal("100.00"));

        vehiculoEjemplo = new Vehiculo();
        vehiculoEjemplo.setId(5);
        vehiculoEjemplo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculoEjemplo.setCategoria(categoriaEjemplo);
        vehiculoEjemplo.setKilometraje(15000);

        sucursalRetiro = new Sucursal();
        sucursalRetiro.setId(1);

        sucursalDevolucion = new Sucursal();
        sucursalDevolucion.setId(2);

        rentaEjemplo = new Renta();
        rentaEjemplo.setId(100L);
        rentaEjemplo.setCliente(clienteEjemplo);
        rentaEjemplo.setVehiculo(vehiculoEjemplo);
        rentaEjemplo.setSucursalRetiro(sucursalRetiro);
        rentaEjemplo.setSucursalDevolucion(sucursalDevolucion);
        rentaEjemplo.setFechaInicio(fechaInicio);
        rentaEjemplo.setFechaFinEstimada(fechaFin);
        rentaEjemplo.setCostoTotal(new BigDecimal("300.00"));
        rentaEjemplo.setKilometrajeInicial(15000);
        rentaEjemplo.setEstado(EstadoRenta.RESERVADA);

        createRequest = new RentaDto.CreateRequest(
                5,                      // 1. idVehiculo (Integer)
                10,                     // 2. idCliente (Integer)
                1,                      // 3. idSucursalRetiro (Integer)
                2,                      // 4. idSucursalDevolucion (Integer)
                fechaInicio,            // 5. fechaInicio (LocalDateTime)
                fechaFin,               // 6. fechaFinEstimada (LocalDateTime)
                15000                   // 7. kilometrajeInicial (Integer)
        );

        responseEjemplo = new RentaDto.Response(
                100L,                            // 1. id (Long)
                "Toyota Corolla (ABC-123)",      // 2. vehiculoDetalle (String)
                "Héctor Servín",                 // 3. clienteNombre (String)
                "Sucursal Centro",               // 4. sucursalRetiro (String)
                "Sucursal Aeropuerto",           // 5. sucursalDevolucion (String)
                fechaInicio,                     // 6. fechaInicio (LocalDateTime)
                fechaFin,                        // 7. fechaFinEstimada (LocalDateTime)
                null,                            // 8. fechaDevolucionReal (LocalDateTime)
                15000,                           // 9. kilometrajeInicial (Integer)
                null,                            // 10. kilometrajeFinal (Integer)
                new BigDecimal("300.00"),        // 11. costoTotal (BigDecimal)
                EstadoRenta.RESERVADA            // 12. estado (EstadoRenta)
        );
    }

    // =======================================================
    //                 CREAR RESERVA
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Creación de Reserva")
    class CrearReservaTests {

        @Test
        @DisplayName("Debe crear una reserva exitosamente si todas las validaciones pasan (Happy Path)")
        void crearReserva_Exito() {
            // Arrange
            when(clienteRepository.findById(10)).thenReturn(Optional.of(clienteEjemplo));
            when(vehiculoRepository.findById(5)).thenReturn(Optional.of(vehiculoEjemplo));
            when(rentaRepository.existeSolapamientoReserva(5, fechaInicio, fechaFin)).thenReturn(false);
            when(sucursalRepository.findById(1)).thenReturn(Optional.of(sucursalRetiro));
            when(sucursalRepository.findById(2)).thenReturn(Optional.of(sucursalDevolucion));

            BigDecimal costoEsperado = new BigDecimal("300.00"); // 3 días * $100.00
            when(rentaMapper.toEntity(eq(createRequest), eq(vehiculoEjemplo), eq(clienteEjemplo),
                    eq(sucursalRetiro), eq(sucursalDevolucion), eq(costoEsperado)))
                    .thenReturn(rentaEjemplo);
            when(rentaRepository.save(any(Renta.class))).thenReturn(rentaEjemplo);
            when(rentaMapper.toResponse(rentaEjemplo)).thenReturn(responseEjemplo);

            // Act
            RentaDto.Response resultado = rentaService.crearReserva(createRequest);

            // Assert
            assertNotNull(resultado);
            assertEquals(100L, resultado.id());
            assertEquals(EstadoRenta.RESERVADA, rentaEjemplo.getEstado());
            verify(rentaRepository, times(1)).save(any(Renta.class));
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException cuando la licencia del cliente está vencida")
        void crearReserva_LicenciaVencida_LanzaExcepcion() {
            // Arrange
            clienteEjemplo.setVencimientoLicencia(LocalDate.now().minusDays(1));
            when(clienteRepository.findById(10)).thenReturn(Optional.of(clienteEjemplo));

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> rentaService.crearReserva(createRequest)
            );

            assertEquals("La licencia de conducir del cliente está vencida.", ex.getMessage());
            verify(rentaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException cuando el vehículo está FUERA_SERVICIO o MANTENIMIENTO")
        void crearReserva_VehiculoNoOperativo_LanzaExcepcion() {
            // Arrange
            vehiculoEjemplo.setEstado(EstadoVehiculo.MANTENIMIENTO);
            when(clienteRepository.findById(10)).thenReturn(Optional.of(clienteEjemplo));
            when(vehiculoRepository.findById(5)).thenReturn(Optional.of(vehiculoEjemplo));

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> rentaService.crearReserva(createRequest)
            );

            assertEquals("El vehículo seleccionado no se encuentra operativo.", ex.getMessage());
            verify(rentaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException cuando el vehículo presenta solapamiento de fechas")
        void crearReserva_VehiculoOcupado_LanzaExcepcion() {
            // Arrange
            when(clienteRepository.findById(10)).thenReturn(Optional.of(clienteEjemplo));
            when(vehiculoRepository.findById(5)).thenReturn(Optional.of(vehiculoEjemplo));
            when(rentaRepository.existeSolapamientoReserva(5, fechaInicio, fechaFin)).thenReturn(true);

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> rentaService.crearReserva(createRequest)
            );

            assertEquals("El vehículo ya está reservado o rentado en el rango de fechas seleccionado.", ex.getMessage());
            verify(rentaRepository, never()).save(any());
        }
    }

    // =======================================================
    //                 INICIAR RENTA
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Inicio de Renta / Entrega de Vehículo")
    class IniciarRentaTests {

        @Test
        @DisplayName("Debe pasar de RESERVADA a ACTIVA y marcar vehículo como RENTADO")
        void iniciarRenta_Exito() {
            // Arrange
            rentaEjemplo.setEstado(EstadoRenta.RESERVADA);
            when(rentaRepository.findById(100L)).thenReturn(Optional.of(rentaEjemplo));
            when(rentaRepository.save(any(Renta.class))).thenReturn(rentaEjemplo);
            when(rentaMapper.toResponse(rentaEjemplo)).thenReturn(responseEjemplo);

            // Act
            RentaDto.Response resultado = rentaService.iniciarRenta(100L);

            // Assert
            assertNotNull(resultado);
            assertEquals(EstadoRenta.ACTIVA, rentaEjemplo.getEstado());
            assertEquals(EstadoVehiculo.RENTADO, vehiculoEjemplo.getEstado());
            assertEquals(15000, rentaEjemplo.getKilometrajeInicial());

            verify(vehiculoRepository, times(1)).save(vehiculoEjemplo);
            verify(rentaRepository, times(1)).save(rentaEjemplo);
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException si la renta no está en estado RESERVADA")
        void iniciarRenta_EstadoInvalido_LanzaExcepcion() {
            // Arrange
            rentaEjemplo.setEstado(EstadoRenta.ACTIVA);
            when(rentaRepository.findById(100L)).thenReturn(Optional.of(rentaEjemplo));

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> rentaService.iniciarRenta(100L)
            );

            assertEquals("Solo se pueden iniciar rentas en estado RESERVADA.", ex.getMessage());
        }
    }

    // =======================================================
    //                 REGISTRAR DEVOLUCIÓN
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Registro de Devolución")
    class RegistrarDevolucionTests {

        @Test
        @DisplayName("Debe procesar devolución a tiempo sin mora y actualizar vehículo a DISPONIBLE")
        void registrarDevolucion_SinMora_Exito() {
            // Arrange
            rentaEjemplo.setEstado(EstadoRenta.ACTIVA);
            rentaEjemplo.setKilometrajeInicial(15000);
            rentaEjemplo.setFechaFinEstimada(fechaFin);

            RentaDto.DevolucionRequest devolucionRequest = new RentaDto.DevolucionRequest(
                    fechaFin, // Entregado exactamente a tiempo
                    15250
            );

            when(rentaRepository.findById(100L)).thenReturn(Optional.of(rentaEjemplo));
            when(rentaRepository.save(any(Renta.class))).thenReturn(rentaEjemplo);
            when(rentaMapper.toResponse(rentaEjemplo)).thenReturn(responseEjemplo);

            // Act
            rentaService.registrarDevolucion(100L, devolucionRequest);

            // Assert
            assertEquals(EstadoRenta.COMPLETADA, rentaEjemplo.getEstado());
            assertEquals(15250, vehiculoEjemplo.getKilometraje());
            assertEquals(sucursalDevolucion, vehiculoEjemplo.getSucursalActual());
            assertEquals(EstadoVehiculo.DISPONIBLE, vehiculoEjemplo.getEstado());
            assertEquals(new BigDecimal("300.00"), rentaEjemplo.getCostoTotal()); // Sin recargo

            verify(vehiculoRepository, times(1)).save(vehiculoEjemplo);
            verify(rentaRepository, times(1)).save(rentaEjemplo);
        }

        @Test
        @DisplayName("Debe aplicar recargo del 20% sobre días extra cuando supera la tolerancia de mora (> 1 hora)")
        void registrarDevolucion_ConMora_AplicaRecargo() {
            // Arrange
            rentaEjemplo.setEstado(EstadoRenta.ACTIVA);
            rentaEjemplo.setKilometrajeInicial(15000);
            rentaEjemplo.setCostoTotal(new BigDecimal("300.00")); // Costo base previo
            rentaEjemplo.setFechaFinEstimada(fechaFin);

            // Devolución con 26 horas de retraso (cuenta como 2 días extra)
            LocalDateTime fechaDevolucionConMora = fechaFin.plusHours(26);
            RentaDto.DevolucionRequest devolucionRequest = new RentaDto.DevolucionRequest(
                    fechaDevolucionConMora,
                    15400
            );

            when(rentaRepository.findById(100L)).thenReturn(Optional.of(rentaEjemplo));
            when(rentaRepository.save(any(Renta.class))).thenReturn(rentaEjemplo);

            // Act
            rentaService.registrarDevolucion(100L, devolucionRequest);

            // Assert
            // Tarifa diaria = 100.00.
            // Días extra = ceil(26/24) = 2 días.
            // Recargo = 2 * 100.00 * 1.20 = 240.00.
            // Costo final = 300.00 + 240.00 = 540.00.
            assertEquals(0, new BigDecimal("540.00").compareTo(rentaEjemplo.getCostoTotal()));
            assertEquals(EstadoRenta.COMPLETADA, rentaEjemplo.getEstado());
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException si el kilometraje final es inferior al inicial")
        void registrarDevolucion_KilometrajeInvalido_LanzaExcepcion() {
            // Arrange
            rentaEjemplo.setEstado(EstadoRenta.ACTIVA);
            rentaEjemplo.setKilometrajeInicial(15000);

            RentaDto.DevolucionRequest devolucionInvalida = new RentaDto.DevolucionRequest(
                    fechaFin,
                    14999 // Menor al inicial
            );

            when(rentaRepository.findById(100L)).thenReturn(Optional.of(rentaEjemplo));

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> rentaService.registrarDevolucion(100L, devolucionInvalida)
            );

            assertEquals("El kilometraje final no puede ser menor al kilometraje inicial.", ex.getMessage());
        }
    }

    // =======================================================
    //                 CANCELAR Y CONSULTAR
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Cancelación y Consultas")
    class CancelarYConsultasTests {

        @Test
        @DisplayName("Debe cancelar reserva que aún está en estado RESERVADA")
        void cancelarReserva_Exito() {
            // Arrange
            rentaEjemplo.setEstado(EstadoRenta.RESERVADA);
            when(rentaRepository.findById(100L)).thenReturn(Optional.of(rentaEjemplo));
            when(rentaRepository.save(any(Renta.class))).thenReturn(rentaEjemplo);
            when(rentaMapper.toResponse(rentaEjemplo)).thenReturn(responseEjemplo);

            // Act
            RentaDto.Response resultado = rentaService.cancelarReserva(100L, "Cambio de planes");

            // Assert
            assertNotNull(resultado);
            assertEquals(EstadoRenta.CANCELADA, rentaEjemplo.getEstado());
        }

        @Test
        @DisplayName("Debe retornar la lista paginada de rentas")
        void listarPaginado_Exito() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Renta> pageRentas = new PageImpl<>(List.of(rentaEjemplo));

            when(rentaRepository.findAll(pageable)).thenReturn(pageRentas);
            when(rentaMapper.toResponse(rentaEjemplo)).thenReturn(responseEjemplo);

            // Act
            Page<RentaDto.Response> resultado = rentaService.listarPaginado(pageable);

            // Assert
            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException al buscar por ID inexistente")
        void obtenerPorId_NoEncontrado_LanzaExcepcion() {
            // Arrange
            when(rentaRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> rentaService.obtenerPorId(999L)
            );
        }
    }
}
