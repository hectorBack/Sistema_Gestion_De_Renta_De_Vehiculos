package com.sistemas.backend;

import com.sistemas.backend.Exception.BusinessRuleException;
import com.sistemas.backend.Exception.ResourceNotFoundException;
import com.sistemas.backend.Sucursal.Entity.Sucursal;
import com.sistemas.backend.Sucursal.Repository.SucursalRepository;
import com.sistemas.backend.Vehiculos.DTO.CategoriaDto;
import com.sistemas.backend.Vehiculos.DTO.VehiculoDto;
import com.sistemas.backend.Vehiculos.Entity.CategoriaVehiculo;
import com.sistemas.backend.Vehiculos.Entity.EstadoVehiculo;
import com.sistemas.backend.Vehiculos.Entity.Vehiculo;
import com.sistemas.backend.Vehiculos.Mapper.VehiculoMapper;
import com.sistemas.backend.Vehiculos.Repository.CategoriaVehiculoRepository;
import com.sistemas.backend.Vehiculos.Repository.VehiculoRepository;
import com.sistemas.backend.Vehiculos.Services.Impl.VehiculoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VehiculoServiceImplTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private CategoriaVehiculoRepository categoriaRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private VehiculoMapper vehiculoMapper;

    @InjectMocks
    private VehiculoServiceImpl vehiculoService;

    private CategoriaVehiculo categoriaEjemplo;
    private CategoriaDto.Request categoriaRequest;
    private CategoriaDto.Response categoriaResponse;

    private Sucursal sucursalEjemplo;
    private Sucursal sucursalNueva;

    private Vehiculo vehiculoEjemplo;
    private VehiculoDto.CreateRequest createRequest;
    private VehiculoDto.UpdateRequest updateRequest;
    private VehiculoDto.Response vehiculoResponse;

    @BeforeEach
    void setUp() {
        // Categoría Dummy
        categoriaEjemplo = new CategoriaVehiculo();
        categoriaEjemplo.setId(1);
        categoriaEjemplo.setNombre("SUV");
        categoriaEjemplo.setTarifaDiariaBase(new BigDecimal("150.00"));

        categoriaRequest = new CategoriaDto.Request(
                "SUV",
                "Camioneta Espaciosa",
                new BigDecimal("150.00"),
                new BigDecimal("500.00")
        );
        categoriaResponse = new CategoriaDto.Response(
                1,
                "SUV",
                "Camioneta Espaciosa",
                new BigDecimal("150.00"),
                new BigDecimal("500.00")
        );

        // Sucursales Dummy
        sucursalEjemplo = new Sucursal();
        sucursalEjemplo.setId(1);
        sucursalEjemplo.setNombre("Sucursal Centro");

        sucursalNueva = new Sucursal();
        sucursalNueva.setId(2);
        sucursalNueva.setNombre("Sucursal Aeropuerto");

        // Vehículo Dummy
        vehiculoEjemplo = new Vehiculo();
        vehiculoEjemplo.setId(10);
        vehiculoEjemplo.setVin("1HGCR2F83HA000000");
        vehiculoEjemplo.setPlaca("ABC-123");
        vehiculoEjemplo.setMarca("Honda");
        vehiculoEjemplo.setModelo("CR-V");
        vehiculoEjemplo.setAnio(2023);
        vehiculoEjemplo.setKilometraje(15000);
        vehiculoEjemplo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculoEjemplo.setCategoria(categoriaEjemplo);
        vehiculoEjemplo.setSucursalActual(sucursalEjemplo);

        createRequest = new VehiculoDto.CreateRequest(
                1,                    // idCategoria
                1,                    // idSucursalActual
                "1HGCR2F83HA000000", // vin
                "ABC-123",           // placa
                "Honda",             // marca
                "CR-V",               // modelo
                2023,                // anio
                15000                // kilometraje
        );

        updateRequest = new VehiculoDto.UpdateRequest(
                1,                   // idCategoria (Integer)
                "XYZ-999",           // placa (String)
                "Honda",             // marca (String)
                "CR-V Hybrid",       // modelo (String)
                2024                 // anio (Integer)
        );

        vehiculoResponse = new VehiculoDto.Response(
                10,                   // id
                "SUV",                // categoriaNombre
                "Sucursal Centro",    // sucursalNombre
                "1HGCR2F83HA000000", // vin
                "ABC-123",           // placa
                "Honda",             // marca
                "CR-V",               // modelo
                2023,                // anio
                15000,               // kilometraje
                EstadoVehiculo.DISPONIBLE // estado
        );
    }

    // =======================================================
    //                 CATEGORÍAS DE VEHÍCULO
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Categorías de Vehículos")
    class CategoriaTests {

        @Test
        @DisplayName("Debe crear una categoría exitosamente (Happy Path)")
        void crearCategoria_Exito() {
            // Arrange
            when(categoriaRepository.existsByNombreIgnoreCase("SUV")).thenReturn(false);
            when(vehiculoMapper.toEntity(categoriaRequest)).thenReturn(categoriaEjemplo);
            when(categoriaRepository.save(any(CategoriaVehiculo.class))).thenReturn(categoriaEjemplo);
            when(vehiculoMapper.toResponse(categoriaEjemplo)).thenReturn(categoriaResponse);

            // Act
            CategoriaDto.Response resultado = vehiculoService.crearCategoria(categoriaRequest);

            // Assert
            assertNotNull(resultado);
            assertEquals("SUV", resultado.nombre());
            verify(categoriaRepository, times(1)).save(any(CategoriaVehiculo.class));
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException al crear categoría duplicada")
        void crearCategoria_NombreDuplicado_LanzaExcepcion() {
            // Arrange
            when(categoriaRepository.existsByNombreIgnoreCase("SUV")).thenReturn(true);

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> vehiculoService.crearCategoria(categoriaRequest)
            );

            assertTrue(ex.getMessage().contains("Ya existe una categoría con el nombre"));
            verify(categoriaRepository, never()).save(any());
        }
    }

    // =======================================================
    //                 CREACIÓN DE VEHÍCULO
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Creación de Vehículo")
    class CrearVehiculoTests {

        @Test
        @DisplayName("Debe crear un vehículo exitosamente si VIN y Placa son únicos (Happy Path)")
        void crearVehiculo_Exito() {
            // Arrange
            when(vehiculoRepository.existsByVin(createRequest.vin())).thenReturn(false);
            when(vehiculoRepository.existsByPlaca(createRequest.placa())).thenReturn(false);
            when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoriaEjemplo));
            when(sucursalRepository.findById(1)).thenReturn(Optional.of(sucursalEjemplo));
            when(vehiculoMapper.toEntity(createRequest, categoriaEjemplo, sucursalEjemplo)).thenReturn(vehiculoEjemplo);
            when(vehiculoRepository.save(any(Vehiculo.class))).thenReturn(vehiculoEjemplo);
            when(vehiculoMapper.toResponse(vehiculoEjemplo)).thenReturn(vehiculoResponse);

            // Act
            VehiculoDto.Response resultado = vehiculoService.crearVehiculo(createRequest);

            // Assert
            assertNotNull(resultado);
            assertEquals("ABC-123", resultado.placa());
            verify(vehiculoRepository, times(1)).save(any(Vehiculo.class));
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException si el VIN ya existe")
        void crearVehiculo_VinDuplicado_LanzaExcepcion() {
            // Arrange
            when(vehiculoRepository.existsByVin(createRequest.vin())).thenReturn(true);

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> vehiculoService.crearVehiculo(createRequest)
            );

            assertTrue(ex.getMessage().contains("ya se encuentra registrado"));
            verify(vehiculoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si la categoría especificada no existe")
        void crearVehiculo_CategoriaNoEncontrada_LanzaExcepcion() {
            // Arrange
            when(vehiculoRepository.existsByVin(createRequest.vin())).thenReturn(false);
            when(vehiculoRepository.existsByPlaca(createRequest.placa())).thenReturn(false);
            when(categoriaRepository.findById(1)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> vehiculoService.crearVehiculo(createRequest)
            );
        }
    }

    // =======================================================
    //                 TRASLADO DE SUCURSAL
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Traslado de Sucursal")
    class TrasladarSucursalTests {

        @Test
        @DisplayName("Debe trasladar vehículo si su estado NO es RENTADO")
        void trasladarSucursal_Exito() {
            // Arrange
            when(vehiculoRepository.findById(10)).thenReturn(Optional.of(vehiculoEjemplo));
            when(sucursalRepository.findById(2)).thenReturn(Optional.of(sucursalNueva));

            // Act
            vehiculoService.trasladarSucursal(10, 2);

            // Assert
            assertEquals(sucursalNueva, vehiculoEjemplo.getSucursalActual());
            verify(vehiculoRepository, times(1)).save(vehiculoEjemplo);
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException al intentar trasladar un vehículo RENTADO")
        void trasladarSucursal_VehiculoRentado_LanzaExcepcion() {
            // Arrange
            vehiculoEjemplo.setEstado(EstadoVehiculo.RENTADO);
            when(vehiculoRepository.findById(10)).thenReturn(Optional.of(vehiculoEjemplo));

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> vehiculoService.trasladarSucursal(10, 2)
            );

            assertEquals("No se puede trasladar un vehículo que actualmente está en estado RENTADO.", ex.getMessage());
            verify(vehiculoRepository, never()).save(any());
        }
    }

    // =======================================================
    //               KILOMETRAJE Y MANTENIMIENTO
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Actualización de Kilometraje")
    class KilometrajeTests {

        @Test
        @DisplayName("Debe actualizar el kilometraje correctamente")
        void actualizarKilometraje_Exito() {
            // Arrange
            when(vehiculoRepository.findById(10)).thenReturn(Optional.of(vehiculoEjemplo));

            // Act (Aumenta de 15000 a 18000)
            vehiculoService.actualizarKilometraje(10, 18000);

            // Assert
            assertEquals(18000, vehiculoEjemplo.getKilometraje());
            assertEquals(EstadoVehiculo.DISPONIBLE, vehiculoEjemplo.getEstado());
            verify(vehiculoRepository, times(1)).save(vehiculoEjemplo);
        }

        @Test
        @DisplayName("Debe cambiar estado a MANTENIMIENTO al alcanzar múltiplo de 10,000 km")
        void actualizarKilometraje_MultiploDiezMil_EnviaAMantenimiento() {
            // Arrange
            when(vehiculoRepository.findById(10)).thenReturn(Optional.of(vehiculoEjemplo));

            // Act (Sube a 20,000 km)
            vehiculoService.actualizarKilometraje(10, 20000);

            // Assert
            assertEquals(20000, vehiculoEjemplo.getKilometraje());
            assertEquals(EstadoVehiculo.MANTENIMIENTO, vehiculoEjemplo.getEstado());
            verify(vehiculoRepository, times(1)).save(vehiculoEjemplo);
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException si el nuevo kilometraje es inferior al actual")
        void actualizarKilometraje_MenorAlActual_LanzaExcepcion() {
            // Arrange
            when(vehiculoRepository.findById(10)).thenReturn(Optional.of(vehiculoEjemplo));

            // Act & Assert (Intenta bajar de 15000 a 12000)
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> vehiculoService.actualizarKilometraje(10, 12000)
            );

            assertTrue(ex.getMessage().contains("no puede ser inferior al registrado previamente"));
            verify(vehiculoRepository, never()).save(any());
        }
    }

    // =======================================================
    //                 CONSULTAS Y FILTROS
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Búsqueda y Consultas")
    class ConsultaTests {

        @Test
        @DisplayName("Debe listar vehículos disponibles por sucursal")
        void listarDisponiblesPorSucursal_Exito() {
            // Arrange
            when(vehiculoRepository.findBySucursalAndEstado(1, EstadoVehiculo.DISPONIBLE))
                    .thenReturn(List.of(vehiculoEjemplo));
            when(vehiculoMapper.toResponse(vehiculoEjemplo)).thenReturn(vehiculoResponse);

            // Act
            List<VehiculoDto.Response> resultado = vehiculoService.listarDisponiblesPorSucursal(1);

            // Assert
            assertFalse(resultado.isEmpty());
            assertEquals(1, resultado.size());
            assertEquals(EstadoVehiculo.DISPONIBLE, resultado.get(0).estado());
        }

        @Test
        @DisplayName("Debe obtener vehículo por placa de forma case-insensitive")
        void obtenerPorPlaca_Exito() {
            // Arrange
            when(vehiculoRepository.findByPlacaIgnoreCase("abc-123"))
                    .thenReturn(Optional.of(vehiculoEjemplo));
            when(vehiculoMapper.toResponse(vehiculoEjemplo)).thenReturn(vehiculoResponse);

            // Act
            VehiculoDto.Response resultado = vehiculoService.obtenerPorPlaca("abc-123");

            // Assert
            assertNotNull(resultado);
            assertEquals("ABC-123", resultado.placa());
        }
    }
}
