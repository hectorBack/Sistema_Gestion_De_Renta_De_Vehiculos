package com.sistemas.backend.Pagos.Controllers;

import com.sistemas.backend.Pagos.DTO.PagoDto;
import com.sistemas.backend.Pagos.Entity.EstadoPago;
import com.sistemas.backend.Pagos.Entity.MetodoPago;
import com.sistemas.backend.Pagos.Services.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/pagos")
@Tag(name = "Pagos", description = "Endpoints para procesamiento e historial financiero de las rentas")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    @Operation(summary = "Registrar pago", description = "Asocia un cobro exitoso a una transacción de renta existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pago registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Monto o método de pago inválido"),
            @ApiResponse(responseCode = "404", description = "Renta no encontrada")
    })
    public ResponseEntity<PagoDto.Response> registrarPago(@Valid @RequestBody PagoDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.registrarPago(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pago por ID", description = "Recupera los comprobantes y referencias de pago")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    public ResponseEntity<PagoDto.Response> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.obtenerPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar pagos con filtros", description = "Obtiene el historial paginado de pagos con filtros por estado o método")
    public ResponseEntity<Page<PagoDto.Response>> listarPaginado(
            @RequestParam(required = false) Long idRenta,
            @RequestParam(required = false) EstadoPago estado,
            @RequestParam(required = false) MetodoPago metodoPago,
            @PageableDefault(page = 0, size = 10, sort = "fechaPago", direction = Sort.Direction.DESC) Pageable pageable) {

        if (idRenta != null || estado != null || metodoPago != null) {
            return ResponseEntity.ok(pagoService.buscarConFiltros(idRenta, estado, metodoPago, pageable));
        }
        return ResponseEntity.ok(pagoService.listarPaginado(pageable));
    }

    @PostMapping("/{id}/reembolso")
    @Operation(summary = "Procesar reembolso", description = "Devuelve el dinero total o parcial de un pago completado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reembolso procesado correctamente"),
            @ApiResponse(responseCode = "400", description = "Pago no está completado o monto a reembolsar excede el original")
    })
    public ResponseEntity<PagoDto.Response> procesarReembolso(
            @PathVariable Long id,
            @Valid @RequestBody PagoDto.ReembolsoRequest request) {
        return ResponseEntity.ok(pagoService.procesarReembolso(id, request));
    }

    @GetMapping("/renta/{idRenta}/saldo")
    @Operation(summary = "Consultar saldo pendiente", description = "Devuelve el monto restante a pagar de una renta específica")
    public ResponseEntity<BigDecimal> obtenerSaldoPendiente(@PathVariable Long idRenta) {
        return ResponseEntity.ok(pagoService.obtenerSaldoPendiente(idRenta));
    }
}
