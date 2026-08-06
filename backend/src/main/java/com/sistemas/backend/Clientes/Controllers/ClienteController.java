package com.sistemas.backend.Clientes.Controllers;

import com.sistemas.backend.Clientes.DTO.ClienteDto;
import com.sistemas.backend.Clientes.Services.ClienteService;
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


@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Endpoints para la gestión e inscripción de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @Operation(summary = "Registrar cliente", description = "Inscribe un nuevo cliente verificando vigencia de licencia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente registrado con éxito"),
            @ApiResponse(responseCode = "400", description = "Email o Número de Licencia ya existentes")
    })
    public ResponseEntity<ClienteDto.Response> registrarCliente(@Valid @RequestBody ClienteDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.registrarCliente(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente", description = "Consulta la información de un cliente por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ClienteDto.Response> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Retorna la lista completa de clientes registrados")
    @ApiResponse(responseCode = "200", description = "Consulta realizada correctamente")
    public ResponseEntity<Page<ClienteDto.Response>> listarTodos(
            @PageableDefault(page = 0, size = 10, sort = "nombre") Pageable pageable) {
        return ResponseEntity.ok(clienteService.listarPaginado(pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Modifica los datos del cliente asegurando vigencia de licencia y unicidad")
    public ResponseEntity<ClienteDto.Response> actualizarCliente(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteDto.Request request) {
        return ResponseEntity.ok(clienteService.actualizarCliente(id, request));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de cliente", description = "Activa o inhabilita un cliente (Bloqueo administrativo)")
    public ResponseEntity<ClienteDto.Response> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(clienteService.cambiarEstadoCliente(id, activo));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar clientes con filtros", description = "Busca por nombre, apellido, email o licencia con paginación")
    public ResponseEntity<Page<ClienteDto.Response>> buscarClientes(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(page = 0, size = 10, sort = "apellido") Pageable pageable) {
        return ResponseEntity.ok(clienteService.buscarConFiltros(termino, activo, pageable));
    }

    @GetMapping("/licencia/{numLicencia}")
    @Operation(summary = "Obtener cliente por número de licencia")
    public ResponseEntity<ClienteDto.Response> obtenerPorLicencia(@PathVariable String numLicencia) {
        return ResponseEntity.ok(clienteService.obtenerPorLicencia(numLicencia));
    }
}
