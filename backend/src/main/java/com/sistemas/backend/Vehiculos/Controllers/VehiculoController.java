package com.sistemas.backend.Vehiculos.Controllers;

import com.sistemas.backend.Clientes.DTO.ClienteDto;
import com.sistemas.backend.Vehiculos.DTO.CategoriaDto;
import com.sistemas.backend.Vehiculos.DTO.VehiculoDto;
import com.sistemas.backend.Vehiculos.Entity.EstadoVehiculo;
import com.sistemas.backend.Vehiculos.Services.VehiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehiculos")
@Tag(name = "Vehículos", description = "Endpoints para la gestión de flota de vehículos y categorías")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @PostMapping("/categorias")
    @Operation(summary = "Crear categoría", description = "Registra una nueva categoría tarifaria para vehículos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o categoría duplicada")
    })
    public ResponseEntity<CategoriaDto.Response> crearCategoria(@Valid @RequestBody CategoriaDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.crearCategoria(request));
    }

    @GetMapping("/categorias")
    @Operation(summary = "Listar categorías", description = "Obtiene el catálogo completo de categorías de vehículos")
    @ApiResponse(responseCode = "200", description = "Consulta realizada correctamente")
    public ResponseEntity<List<CategoriaDto.Response>> listarCategorias() {
        return ResponseEntity.ok(vehiculoService.listarCategorias());
    }

    @GetMapping("/categorias/paginadas")
    @Operation(summary = "Listar categorías paginadas", description = "Obtiene el catálogo de categorías con soporte para búsqueda y paginación")
    @ApiResponse(responseCode = "200", description = "Consulta realizada correctamente")
    public ResponseEntity<Page<CategoriaDto.Response>> listarCategoriasPaginadas(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(vehiculoService.listarCategoriasPaginadas(termino, activo, pageable));
    }

    @PutMapping("/categorias/{id}")
    public ResponseEntity<CategoriaDto.Response> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaDto.Request request) {
        return ResponseEntity.ok(vehiculoService.actualizarCategoria(id, request));
    }

    @PatchMapping("/categorias/{id}/estado")
    @Operation(summary = "Cambiar estado de categoria", description = "Activa o inactiva en una categoria (Bloqueo administrativo)")
    public ResponseEntity<CategoriaDto.Response> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(vehiculoService.cambiarEstadoCategoria(id, activo));
    }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        vehiculoService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    // --- Vehículos ---

    @PostMapping
    @Operation(summary = "Registrar nuevo vehículo", description = "Crea un vehículo asociado a una categoría y sucursal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehículo registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "VIN o Placa ya existen en el sistema"),
            @ApiResponse(responseCode = "444", description = "Categoría o Sucursal especificada no existe")
    })
    public ResponseEntity<VehiculoDto.Response> crearVehiculo(@Valid @RequestBody VehiculoDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.crearVehiculo(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener vehículo por ID", description = "Retorna los detalles de un vehículo específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehículo encontrado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<VehiculoDto.Response> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(vehiculoService.obtenerPorId(id));
    }

    @GetMapping("/placa/{placa}")
    @Operation(summary = "Obtener vehículo por Placa", description = "Busca un vehículo de la flota por su número de placa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehículo encontrado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<VehiculoDto.Response> obtenerPorPlaca(@PathVariable String placa) {
        return ResponseEntity.ok(vehiculoService.obtenerPorPlaca(placa));
    }

    @GetMapping
    @Operation(summary = "Buscar vehículos con filtros y paginación", description = "Consulta paginada con filtros dinámicos por sucursal, estado, categoría y marca")
    @ApiResponse(responseCode = "200", description = "Página de vehículos obtenida exitosamente")
    public ResponseEntity<Page<VehiculoDto.Response>> buscarConFiltros(
            @RequestParam(required = false) Integer idSucursal,
            @RequestParam(required = false) EstadoVehiculo estado,
            @RequestParam(required = false) Integer idCategoria,
            @RequestParam(required = false) String marca,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return ResponseEntity.ok(vehiculoService.buscarConFiltros(idSucursal, estado, idCategoria, marca, pageable));
    }

    @GetMapping("/disponibles/sucursal/{idSucursal}")
    @Operation(summary = "Listar vehículos disponibles", description = "Obtiene los vehículos listos para rentar en una sucursal dada")
    @ApiResponse(responseCode = "200", description = "Lista devuelta correctamente")
    public ResponseEntity<List<VehiculoDto.Response>> listarDisponibles(@PathVariable Integer idSucursal) {
        return ResponseEntity.ok(vehiculoService.listarDisponiblesPorSucursal(idSucursal));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar vehículo", description = "Actualiza la información general modificable de un vehículo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehículo actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "La nueva placa ya pertenece a otro vehículo"),
            @ApiResponse(responseCode = "404", description = "Vehículo o Categoría no encontrada")
    })
    public ResponseEntity<VehiculoDto.Response> actualizarVehiculo(
            @PathVariable Integer id,
            @Valid @RequestBody VehiculoDto.UpdateRequest request) {

        return ResponseEntity.ok(vehiculoService.actualizarVehiculo(id, request));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado del vehículo", description = "Actualiza el estado operativo (DISPONIBLE, MANTENIMIENTO, etc.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<Void> cambiarEstado(@PathVariable Integer id, @RequestParam EstadoVehiculo nuevoEstado) {
        vehiculoService.cambiarEstado(id, nuevoEstado);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/trasladar")
    @Operation(summary = "Trasladar vehículo de sucursal", description = "Mueve administrativamente un vehículo a una nueva sucursal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vehículo trasladado correctamente"),
            @ApiResponse(responseCode = "400", description = "No se puede trasladar un vehículo que se encuentra en estado RENTADO"),
            @ApiResponse(responseCode = "404", description = "Vehículo o Sucursal no encontrada")
    })
    public ResponseEntity<Void> trasladarSucursal(@PathVariable Integer id, @RequestParam Integer idNuevaSucursal) {
        vehiculoService.trasladarSucursal(id, idNuevaSucursal);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/kilometraje")
    @Operation(summary = "Actualizar kilometraje", description = "Actualiza la lectura del odómetro del vehículo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Kilometraje actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "El nuevo kilometraje no puede ser inferior al registrado previamente"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<Void> actualizarKilometraje(
            @PathVariable Integer id,
            @RequestParam @PositiveOrZero(message = "El kilometraje debe ser mayor o igual a 0") Integer nuevoKilometraje) {

        vehiculoService.actualizarKilometraje(id, nuevoKilometraje);
        return ResponseEntity.noContent().build();
    }

}
