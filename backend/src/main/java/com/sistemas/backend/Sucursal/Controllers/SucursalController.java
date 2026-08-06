package com.sistemas.backend.Sucursal.Controllers;

import com.sistemas.backend.Sucursal.DTO.SucursalDto;
import com.sistemas.backend.Sucursal.Services.SucursalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sucursales")
@Tag(name = "Sucursales", description = "Endpoints para administración de oficinas y puntos de retiro/devolución")
public class SucursalController {

    private final SucursalService sucursalService;

    public SucursalController(SucursalService sucursalService) {
        this.sucursalService = sucursalService;
    }

    @PostMapping
    @Operation(summary = "Crear sucursal", description = "Registra un nuevo punto físico de atención")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sucursal registrada con éxito"),
            @ApiResponse(responseCode = "400", description = "Nombre de sucursal duplicado o datos inválidos")
    })
    public ResponseEntity<SucursalDto.Response> crearSucursal(@Valid @RequestBody SucursalDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sucursalService.crearSucursal(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener sucursal", description = "Recupera una sucursal por su identificador único")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sucursal encontrada"),
            @ApiResponse(responseCode = "404", description = "Sucursal no encontrada")
    })
    public ResponseEntity<SucursalDto.Response> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(sucursalService.obtenerPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar todas las sucursales", description = "Retorna el catálogo global de sucursales")
    @ApiResponse(responseCode = "200", description = "Consulta realizada correctamente")
    public ResponseEntity<Page<SucursalDto.Response>> listarTodas(
            @PageableDefault(page = 0, size = 10, sort = "nombre") Pageable pageable) {
        return ResponseEntity.ok(sucursalService.listarPaginado(pageable));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar sucursales por nombre", description = "Retorna coincidencias parciales del nombre de la sucursal")
    @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    public ResponseEntity<Page<SucursalDto.Response>> buscarPorNombre(
            @RequestParam String nombre,
            @PageableDefault(page = 0, size = 10, sort = "nombre") Pageable pageable) {
        return ResponseEntity.ok(sucursalService.buscarPorNombre(nombre, pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar sucursal", description = "Elimina una sucursal siempre que no posea vehículos asignados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sucursal eliminada exitosamente"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar la sucursal porque posee vehículos asignados"),
            @ApiResponse(responseCode = "404", description = "Sucursal no encontrada")
    })
    public ResponseEntity<Void> eliminarSucursal(@PathVariable Integer id) {
        sucursalService.eliminarSucursal(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar sucursal", description = "Actualiza los datos de una sucursal existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sucursal actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "El nuevo nombre ya pertenece a otra sucursal"),
            @ApiResponse(responseCode = "404", description = "Sucursal no encontrada")
    })
    public ResponseEntity<SucursalDto.Response> actualizarSucursal(
            @PathVariable Integer id,
            @Valid @RequestBody SucursalDto.Request request) {
        return ResponseEntity.ok(sucursalService.actualizarSucursal(id, request));
    }
}
