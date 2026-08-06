package com.sistemas.backend.dashboard.Controller;


import com.sistemas.backend.dashboard.DTO.DashboardMetricsDto;
import com.sistemas.backend.dashboard.Service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Endpoints para métricas, gráficos e indicadores clave del negocio (KPIs)")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/metricas")
    @Operation(
            summary = "Obtener métricas consolidadas del dashboard",
            description = "Recupera los KPIs principales, ocupación de flota, datos para gráficos y actividad reciente en un solo llamado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas recuperadas exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno al procesar los datos estadísticos")
    })
    public ResponseEntity<DashboardMetricsDto> obtenerMetricas() {
        return ResponseEntity.ok(dashboardService.obtenerMetricasDashboard());
    }
}
