package com.sistemas.backend.Rentas.Repository;

import com.sistemas.backend.Rentas.Entity.EstadoRenta;
import com.sistemas.backend.Rentas.Entity.Renta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentaRepository extends JpaRepository<Renta, Long> {

    // JOIN FETCH para optimizar vistas de detalle
    @Query("SELECT r FROM Renta r " +
            "JOIN FETCH r.vehiculo v " +
            "JOIN FETCH v.categoria " +
            "JOIN FETCH r.cliente " +
            "JOIN FETCH r.sucursalRetiro " +
            "JOIN FETCH r.sucursalDevolucion " +
            "WHERE r.id = :id")
    Optional<Renta> findByIdWithDetails(@Param("id") Long id);

    // Carga ansiosa eficiente con EntityGraph para evitar N+1 en la paginación
    @EntityGraph(attributePaths = {"vehiculo", "vehiculo.categoria", "cliente", "sucursalRetiro", "sucursalDevolucion"})
    Page<Renta> findAll(Pageable pageable);

    // Validación crítica: Verifica si un vehículo ya está reservado/activo en un rango de fechas
    @Query("SELECT COUNT(r) > 0 FROM Renta r " +
            "WHERE r.vehiculo.id = :idVehiculo " +
            "AND r.estado IN ('RESERVADA', 'ACTIVA') " +
            "AND (:fechaInicio < r.fechaFinEstimada AND :fechaFin > r.fechaInicio)")
    boolean existeSolapamientoReserva(
            @Param("idVehiculo") Integer idVehiculo,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @EntityGraph(attributePaths = {"vehiculo", "vehiculo.categoria", "cliente", "sucursalRetiro", "sucursalDevolucion"})
    @Query("SELECT r FROM Renta r " +
            "WHERE (:estado IS NULL OR r.estado = :estado) " +
            "AND (:idCliente IS NULL OR r.cliente.id = :idCliente) " +
            "AND (r.fechaInicio >= :inicioDia OR cast(:inicioDia as timestamp) IS NULL) " +
            "AND (r.fechaInicio <= :finDia OR cast(:finDia as timestamp) IS NULL)")
    Page<Renta> buscarConFiltros(
            @Param("estado") EstadoRenta estado,
            @Param("idCliente") Integer idCliente,
            @Param("inicioDia") LocalDateTime inicioDia,
            @Param("finDia") LocalDateTime finDia,
            Pageable pageable);

    // 1. Conteo total de rentas creadas o iniciadas hoy
    @Query("SELECT COUNT(r) FROM Renta r WHERE r.fechaInicio BETWEEN :inicioDia AND :finDia")
    long contarRentasHoy(
            @Param("inicioDia") LocalDateTime inicioDia,
            @Param("finDia") LocalDateTime finDia);

    // 2. Conteo por estado específico en un rango del día (o global según necesites)
    @Query("SELECT COUNT(r) FROM Renta r WHERE r.estado = :estado")
    long contarPorEstado(@Param("estado") EstadoRenta estado);

    // 3. Suma total de dinero generado en el día (ACTIVAS + COMPLETADAS)
    @Query("SELECT COALESCE(SUM(r.costoTotal), 0) FROM Renta r " +
            "WHERE r.estado IN ('ACTIVA', 'COMPLETADA') " +
            "AND r.fechaInicio BETWEEN :inicioDia AND :finDia")
    BigDecimal calcularIngresosDelDia(
            @Param("inicioDia") LocalDateTime inicioDia,
            @Param("finDia") LocalDateTime finDia);
}