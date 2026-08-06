package com.sistemas.backend.Vehiculos.Repository;

import com.sistemas.backend.Vehiculos.Entity.EstadoVehiculo;
import com.sistemas.backend.Vehiculos.Entity.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer>, JpaSpecificationExecutor<Vehiculo> {

    boolean existsByVin(String vin);

    boolean existsByPlaca(String placa);

    // Evita el problema N+1 al consultar los detalles de un vehículo
    @Query("SELECT v FROM Vehiculo v " +
            "JOIN FETCH v.categoria " +
            "JOIN FETCH v.sucursalActual " +
            "WHERE v.id = :id")
    Optional<Vehiculo> findByIdWithDetails(@Param("id") Integer id);

    // Búsqueda eficiente de vehículos disponibles por sucursal
    @Query("SELECT v FROM Vehiculo v " +
            "JOIN FETCH v.categoria " +
            "WHERE v.sucursalActual.id = :idSucursal AND v.estado = :estado")
    List<Vehiculo> findBySucursalAndEstado(
            @Param("idSucursal") Integer idSucursal,
            @Param("estado") EstadoVehiculo estado
    );

    Optional<Vehiculo> findByVin(String vin);
    Optional<Vehiculo> findByPlacaIgnoreCase(String placa);

    boolean existsBySucursalActualId(Integer idSucursal);
}
