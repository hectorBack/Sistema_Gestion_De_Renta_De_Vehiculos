package com.sistemas.backend.Rentas.Controllers;

import com.sistemas.backend.Rentas.DTO.RentaDto;
import com.sistemas.backend.Rentas.DTO.RentaResumenDto;
import com.sistemas.backend.Rentas.Entity.EstadoRenta;
import com.sistemas.backend.Rentas.Services.RentaServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/rentas")
@Tag(name = "Rentas", description = "Endpoints para transacciones principales: Reservas, entregas y devoluciones")
public class RentaController {

    private final RentaServices rentaService;

    public RentaController(RentaServices rentaService) {
        this.rentaService = rentaService;
    }

    @PostMapping("/reserva")
    @Operation(summary = "Crear reserva", description = "Valida vigencia de licencia, solapamiento de fechas y realiza el cálculo de tarifa estimada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reserva creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Licencia vencida, vehículo no disponible o solapamiento de fechas"),
            @ApiResponse(responseCode = "404", description = "Cliente, vehículo o sucursales no encontrados")
    })
    public ResponseEntity<RentaDto.Response> crearReserva(@Valid @RequestBody RentaDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rentaService.crearReserva(request));
    }

    @PutMapping("/{id}/devolucion")
    @Operation(summary = "Registrar devolución de vehículo", description = "Cierra una renta, actualiza el kilometraje y ubica el auto en la sucursal de destino")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devolución completada exitosamente"),
            @ApiResponse(responseCode = "400", description = "La renta ya fue cerrada o kilometraje final incoherente"),
            @ApiResponse(responseCode = "404", description = "Renta no encontrada")
    })
    public ResponseEntity<RentaDto.Response> registrarDevolucion(
            @PathVariable Long id,
            @Valid @RequestBody RentaDto.DevolucionRequest request) {
        return ResponseEntity.ok(rentaService.registrarDevolucion(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de renta", description = "Obtiene la información detallada de una reserva/renta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Renta encontrada"),
            @ApiResponse(responseCode = "404", description = "Renta no encontrada")
    })
    public ResponseEntity<RentaDto.Response> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(rentaService.obtenerPorId(id));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Listar rentas paginadas con filtros", description = "Obtiene el historial paginado de reservas y rentas con filtros opcionales")
    public ResponseEntity<Page<RentaDto.Response>> buscarConFiltros(
            @RequestParam(required = false) EstadoRenta estado,
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @PageableDefault(page = 0, size = 10, sort = "fechaInicio", direction = Sort.Direction.DESC) Pageable pageable) {

        // Se delega directamente al servicio de filtros.
        // Si los tres filtros son null, la consulta devolverá la lista completa normalmente.
        return ResponseEntity.ok(rentaService.buscarConFiltros(estado, idCliente, fecha, pageable));
    }

    @PatchMapping("/{id}/iniciar")
    @Operation(summary = "Iniciar renta (Entrega de llaves)", description = "Pasa la renta a estado ACTIVA y marca el vehículo como RENTADO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Renta iniciada correctamente"),
            @ApiResponse(responseCode = "400", description = "La renta no está en estado RESERVADA"),
            @ApiResponse(responseCode = "404", description = "Renta no encontrada")
    })
    public ResponseEntity<RentaDto.Response> iniciarRenta(@PathVariable Long id) {
        return ResponseEntity.ok(rentaService.iniciarRenta(id));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar reserva", description = "Cancela una reserva que aún no ha iniciado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva cancelada exitosamente"),
            @ApiResponse(responseCode = "400", description = "No se puede cancelar una renta activa o completada"),
            @ApiResponse(responseCode = "404", description = "Renta no encontrada")
    })
    public ResponseEntity<RentaDto.Response> cancelarReserva(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Cancelación solicitada por el usuario") String motivo) {
        return ResponseEntity.ok(rentaService.cancelarReserva(id, motivo));
    }

    @GetMapping("/resumen")
    @Operation(summary = "Obtener Resumen", description = "Obtiene Resumen de metricas de rentas")
    public ResponseEntity<RentaResumenDto> obtenerResumenDashboard() {
        return ResponseEntity.ok(rentaService.obtenerResumenDashboard());
    }
}
