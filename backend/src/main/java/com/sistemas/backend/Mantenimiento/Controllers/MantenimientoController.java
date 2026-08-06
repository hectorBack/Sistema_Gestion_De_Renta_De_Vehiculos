package com.sistemas.backend.Mantenimiento.Controllers;

import com.sistemas.backend.Mantenimiento.DTO.MantenimientoDto;
import com.sistemas.backend.Mantenimiento.Services.MantenimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mantenimientos")
@Tag(name = "Mantenimientos", description = "Endpoints para la gestión de mantenimientos de la flota de vehículos")
public class MantenimientoController {

    private final MantenimientoService mantenimientoService;

    public MantenimientoController(MantenimientoService mantenimientoService) {
        this.mantenimientoService = mantenimientoService;
    }

    @PostMapping
    @Operation(summary = "Registrar mantenimiento", description = "Crea un nuevo registro de mantenimiento para un vehículo")
    public ResponseEntity<MantenimientoDto.Response> crear(@Valid @RequestBody MantenimientoDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mantenimientoService.crearMantenimiento(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener mantenimiento por ID")
    public ResponseEntity<MantenimientoDto.Response> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(mantenimientoService.obtenerPorId(id));
    }

    @GetMapping("/paginados")
    @Operation(summary = "Listar mantenimientos paginados con filtros")
    public ResponseEntity<Page<MantenimientoDto.Response>> listarPaginados(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) Integer idVehiculo,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(mantenimientoService.listarPaginados(termino, idVehiculo, activo, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar mantenimiento por ID")
    public ResponseEntity<MantenimientoDto.Response> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MantenimientoDto.Request request) {
        return ResponseEntity.ok(mantenimientoService.actualizarMantenimiento(id, request));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de mantenimiento", description = "Activa o Inactiva un mantenimiento (Bloqueo administrativo)")
    public ResponseEntity<MantenimientoDto.Response> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(mantenimientoService.cambiarEstadoMantenimiento(id, activo));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar mantenimiento (Borrado lógico)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mantenimientoService.eliminarMantenimiento(id);
        return ResponseEntity.noContent().build();
    }
}
