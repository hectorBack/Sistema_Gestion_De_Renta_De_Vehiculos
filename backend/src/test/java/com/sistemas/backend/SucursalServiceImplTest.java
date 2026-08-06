package com.sistemas.backend;

import com.sistemas.backend.Exception.BusinessRuleException;
import com.sistemas.backend.Exception.ResourceNotFoundException;
import com.sistemas.backend.Sucursal.DTO.SucursalDto;
import com.sistemas.backend.Sucursal.Entity.Sucursal;
import com.sistemas.backend.Sucursal.Mapper.SucursalMapper;
import com.sistemas.backend.Sucursal.Repository.SucursalRepository;
import com.sistemas.backend.Sucursal.Services.Impl.SucursalServiceImpl;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SucursalServiceImplTest {

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private SucursalMapper sucursalMapper;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @InjectMocks
    private SucursalServiceImpl sucursalService;

    private Sucursal sucursalEjemplo;
    private SucursalDto.Request requestEjemplo;
    private SucursalDto.Response responseEjemplo;

    @BeforeEach
    void setUp() {
        sucursalEjemplo = new Sucursal();
        sucursalEjemplo.setId(1);
        sucursalEjemplo.setNombre("Sucursal Centro");
        sucursalEjemplo.setDireccion("Av. Principal #123");
        sucursalEjemplo.setTelefono("5551234567");
        sucursalEjemplo.setEmail("centro@rentacar.com");

        requestEjemplo = new SucursalDto.Request(
                "Sucursal Centro",
                "Av. Principal #123",
                "5551234567",
                "centro@rentacar.com"
        );

        responseEjemplo = new SucursalDto.Response(
                1,
                "Sucursal Centro",
                "Av. Principal #123",
                "5551234567",
                "centro@rentacar.com"
        );
    }

    // =======================================================
    //                 CREAR SUCURSAL
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Creación de Sucursal")
    class CrearSucursalTests {

        @Test
        @DisplayName("Debe crear una sucursal exitosamente cuando el nombre no existe")
        void crearSucursal_Exito() {
            // Arrange
            when(sucursalRepository.existsByNombreIgnoreCase("Sucursal Centro")).thenReturn(false);
            when(sucursalMapper.toEntity(requestEjemplo)).thenReturn(sucursalEjemplo);
            when(sucursalRepository.save(any(Sucursal.class))).thenReturn(sucursalEjemplo);
            when(sucursalMapper.toResponse(sucursalEjemplo)).thenReturn(responseEjemplo);

            // Act
            SucursalDto.Response resultado = sucursalService.crearSucursal(requestEjemplo);

            // Assert
            assertNotNull(resultado);
            assertEquals(1, resultado.id());
            assertEquals("Sucursal Centro", resultado.nombre());
            verify(sucursalRepository, times(1)).save(any(Sucursal.class));
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException al intentar crear con un nombre existente")
        void crearSucursal_NombreDuplicado_LanzaExcepcion() {
            // Arrange
            when(sucursalRepository.existsByNombreIgnoreCase("Sucursal Centro")).thenReturn(true);

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> sucursalService.crearSucursal(requestEjemplo)
            );

            assertEquals("Ya existe una sucursal con el nombre: Sucursal Centro", ex.getMessage());
            verify(sucursalRepository, never()).save(any());
        }
    }

    // =======================================================
    //               ACTUALIZAR SUCURSAL
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Actualización de Sucursal")
    class ActualizarSucursalTests {

        @Test
        @DisplayName("Debe actualizar correctamente cuando los datos son válidos")
        void actualizarSucursal_Exito() {
            // Arrange
            SucursalDto.Request requestNuevo = new SucursalDto.Request(
                    "Sucursal Centro Renovada",
                    "Calle Nueva #456",
                    "5559876543",
                    "renovada@rentacar.com"
            );

            when(sucursalRepository.findById(1)).thenReturn(Optional.of(sucursalEjemplo));
            when(sucursalRepository.existsByNombreIgnoreCase("Sucursal Centro Renovada")).thenReturn(false);
            when(sucursalRepository.save(sucursalEjemplo)).thenReturn(sucursalEjemplo);
            when(sucursalMapper.toResponse(sucursalEjemplo)).thenReturn(responseEjemplo);

            // Act
            SucursalDto.Response resultado = sucursalService.actualizarSucursal(1, requestNuevo);

            // Assert
            assertNotNull(resultado);
            verify(sucursalRepository, times(1)).save(sucursalEjemplo);
        }

        @Test
        @DisplayName("Debe permitir actualizar manteniendo el mismo nombre sin lanzar excepción por duplicado")
        void actualizarSucursal_MismoNombre_Exito() {
            // Arrange
            when(sucursalRepository.findById(1)).thenReturn(Optional.of(sucursalEjemplo));
            when(sucursalRepository.save(sucursalEjemplo)).thenReturn(sucursalEjemplo);
            when(sucursalMapper.toResponse(sucursalEjemplo)).thenReturn(responseEjemplo);

            // Act
            SucursalDto.Response resultado = sucursalService.actualizarSucursal(1, requestEjemplo);

            // Assert
            assertNotNull(resultado);
            // Verifica que NO se llamó a existeByNombreIgnoreCase porque el nombre es idéntico
            verify(sucursalRepository, never()).existsByNombreIgnoreCase(anyString());
            verify(sucursalRepository, times(1)).save(sucursalEjemplo);
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException al cambiar a un nombre que pertenece a otra sucursal")
        void actualizarSucursal_NombreConflictoOtraSucursal_LanzaExcepcion() {
            // Arrange
            SucursalDto.Request requestConflicto = new SucursalDto.Request(
                    "Sucursal Norte", // Nombre de otra sucursal existente
                    "Av. Principal #123",
                    "5551234567",
                    "centro@rentacar.com"
            );

            when(sucursalRepository.findById(1)).thenReturn(Optional.of(sucursalEjemplo));
            when(sucursalRepository.existsByNombreIgnoreCase("Sucursal Norte")).thenReturn(true);

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> sucursalService.actualizarSucursal(1, requestConflicto)
            );

            assertEquals("Ya existe otra sucursal con el nombre: Sucursal Norte", ex.getMessage());
            verify(sucursalRepository, never()).save(any());
        }
    }

    // =======================================================
    //                 ELIMINAR SUCURSAL
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Eliminación de Sucursal")
    class EliminarSucursalTests {

        @Test
        @DisplayName("Debe eliminar la sucursal cuando no posee vehículos asignados")
        void eliminarSucursal_Exito() {
            // Arrange
            when(sucursalRepository.findById(1)).thenReturn(Optional.of(sucursalEjemplo));
            when(vehiculoRepository.existsBySucursalActualId(1)).thenReturn(false);

            // Act
            sucursalService.eliminarSucursal(1);

            // Assert
            verify(sucursalRepository, times(1)).delete(sucursalEjemplo);
        }

        @Test
        @DisplayName("Debe lanzar BusinessRuleException si la sucursal aún posee vehículos radicados")
        void eliminarSucursal_ConVehiculosAsignados_LanzaExcepcion() {
            // Arrange
            when(sucursalRepository.findById(1)).thenReturn(Optional.of(sucursalEjemplo));
            when(vehiculoRepository.existsBySucursalActualId(1)).thenReturn(true);

            // Act & Assert
            BusinessRuleException ex = assertThrows(
                    BusinessRuleException.class,
                    () -> sucursalService.eliminarSucursal(1)
            );

            assertEquals("No es posible eliminar la sucursal porque tiene vehículos asignados. Reasigne los vehículos antes de continuar.", ex.getMessage());
            verify(sucursalRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException al intentar eliminar sucursal inexistente")
        void eliminarSucursal_NoEncontrada_LanzaExcepcion() {
            // Arrange
            when(sucursalRepository.findById(99)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> sucursalService.eliminarSucursal(99)
            );
        }
    }

    // =======================================================
    //                 CONSULTAS Y BUSQUEDAS
    // =======================================================
    @Nested
    @DisplayName("Pruebas de Consultas")
    class ConsultasSucursalTests {

        @Test
        @DisplayName("Debe retornar la sucursal por ID si existe")
        void obtenerPorId_Exito() {
            // Arrange
            when(sucursalRepository.findById(1)).thenReturn(Optional.of(sucursalEjemplo));
            when(sucursalMapper.toResponse(sucursalEjemplo)).thenReturn(responseEjemplo);

            // Act
            SucursalDto.Response resultado = sucursalService.obtenerPorId(1);

            // Assert
            assertNotNull(resultado);
            assertEquals(1, resultado.id());
        }

        @Test
        @DisplayName("Debe retornar la lista paginada de sucursales")
        void listarPaginado_Exito() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Sucursal> page = new PageImpl<>(List.of(sucursalEjemplo));

            when(sucursalRepository.findAll(pageable)).thenReturn(page);
            when(sucursalMapper.toResponse(sucursalEjemplo)).thenReturn(responseEjemplo);

            // Act
            Page<SucursalDto.Response> resultado = sucursalService.listarPaginado(pageable);

            // Assert
            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
        }

        @Test
        @DisplayName("Debe filtrar sucursales por término en el nombre")
        void buscarPorNombre_Exito() {
            // Arrange
            when(sucursalRepository.findByNombreContainingIgnoreCase("Centro"))
                    .thenReturn(List.of(sucursalEjemplo));
            when(sucursalMapper.toResponse(sucursalEjemplo)).thenReturn(responseEjemplo);

            // Act
            List<SucursalDto.Response> resultado = sucursalService.buscarPorNombre("Centro");

            // Assert
            assertFalse(resultado.isEmpty());
            assertEquals(1, resultado.size());
            assertEquals("Sucursal Centro", resultado.get(0).nombre());
        }
    }
}
