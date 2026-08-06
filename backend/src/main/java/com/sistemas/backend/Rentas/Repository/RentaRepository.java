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
            "AND (:idCliente IS NULL OR r.cliente.id = :idCliente)")
    Page<Renta> buscarConFiltros(
            @Param("estado") EstadoRenta estado,
            @Param("idCliente") Integer idCliente,
            Pageable pageable);
}