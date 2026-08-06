package com.sistemas.backend.dashboard.Repository;

import com.sistemas.backend.Rentas.Entity.EstadoRenta;
import com.sistemas.backend.Rentas.Entity.Renta;
import com.sistemas.backend.Vehiculos.Entity.EstadoVehiculo;
import com.sistemas.backend.dashboard.DTO.IngresoMensualDto;
import com.sistemas.backend.dashboard.DTO.RentaResumenDto;
import com.sistemas.backend.dashboard.DTO.RentasPorSucursalDto;
import com.sistemas.backend.dashboard.DTO.VehiculoPopularDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Renta, Long> {

    // Conteo por estado
    long countByEstado(EstadoRenta estado);

    // Conteo de vehículos filtrados por su estado desde la relación en Renta
    @Query("SELECT COUNT(v) FROM Vehiculo v WHERE v.estado = :estado")
    long contarVehiculosPorEstado(@Param("estado") EstadoVehiculo estado);

    // Suma de ingresos dentro de un rango de fechas
    @Query("""
    SELECT COALESCE(SUM(r.costoTotal), 0.0) 
    FROM Renta r 
    WHERE r.fechaInicio >= :inicioMes 
      AND r.fechaInicio <= :finMes 
      AND r.estado != 'CANCELADA'
""")
    BigDecimal calcularIngresosRango(
            @Param("inicioMes") LocalDateTime inicioMes,
            @Param("finMes") LocalDateTime finMes
    );

    // Devuelve los ingresos agrupados por año y mes (Últimos meses)
    @Query("""
        SELECT new com.sistemas.backend.dashboard.DTO.IngresoMensualDto(
            YEAR(r.fechaInicio), 
            MONTH(r.fechaInicio), 
            SUM(r.costoTotal)
        )
        FROM Renta r
        WHERE r.fechaInicio >= :fechaInicio
          AND r.estado != 'CANCELADA'
        GROUP BY YEAR(r.fechaInicio), MONTH(r.fechaInicio)
        ORDER BY YEAR(r.fechaInicio) ASC, MONTH(r.fechaInicio) ASC
    """)
    List<IngresoMensualDto> obtenerIngresosMensuales(@Param("fechaInicio") LocalDateTime fechaInicio);

    // Top N Vehículos más rentados
    @Query("""
        SELECT new com.sistemas.backend.dashboard.DTO.VehiculoPopularDto(
            v.marca, 
            v.modelo, 
            v.placa, 
            COUNT(r.id)
        )
        FROM Renta r
        JOIN r.vehiculo v
        GROUP BY v.id, v.marca, v.modelo, v.placa
        ORDER BY COUNT(r.id) DESC
    """)
    List<VehiculoPopularDto> obtenerTopVehiculos(Pageable pageable);

    // Distribución de rentas por sucursal de retiro
    @Query("""
        SELECT new com.sistemas.backend.dashboard.DTO.RentasPorSucursalDto(
            s.nombre, 
            COUNT(r.id)
        )
        FROM Renta r
        JOIN r.sucursalRetiro s
        GROUP BY s.id, s.nombre
    """)
    List<RentasPorSucursalDto> obtenerRentasPorSucursal();

    // Rentas atrasadas (Fecha fin estimada vencida y no finalizadas)
    @Query("""
        SELECT COUNT(r.id) 
        FROM Renta r 
        WHERE r.estado = 'EN_CURSO' 
          AND r.fechaFinEstimada < :ahora
    """)
    long contarRentasAtrasadas(@Param("ahora") LocalDateTime ahora);

    // Últimas N rentas registradas
    @Query("""
    SELECT new com.sistemas.backend.dashboard.DTO.RentaResumenDto(
        r.id,
        CONCAT(r.cliente.nombre, ' ', r.cliente.apellido),
        CONCAT(r.vehiculo.marca, ' ', r.vehiculo.modelo, ' (', r.vehiculo.placa, ')'),
        r.fechaInicio,
        CAST(r.estado AS string)
    )
    FROM Renta r
    ORDER BY r.id DESC
""")
    List<RentaResumenDto> obtenerUltimasRentas(Pageable pageable);
}
