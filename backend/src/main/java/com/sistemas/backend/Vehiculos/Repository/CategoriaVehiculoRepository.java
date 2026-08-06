package com.sistemas.backend.Vehiculos.Repository;

import com.sistemas.backend.Vehiculos.Entity.CategoriaVehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaVehiculoRepository extends JpaRepository<CategoriaVehiculo, Integer> {

    boolean existsByNombreIgnoreCase(String nombre);

    Optional<CategoriaVehiculo> findByNombreIgnoreCase(String nombre);

    @Query("""
        SELECT c FROM CategoriaVehiculo c 
        WHERE (CAST(:termino AS string) IS NULL 
           OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', CAST(:termino AS string), '%'))
           OR LOWER(c.descripcion) LIKE LOWER(CONCAT('%', CAST(:termino AS string), '%')))
          AND (:activo IS NULL OR c.activo = :activo)
    """)
    Page<CategoriaVehiculo> buscarCategoriasPaginadas(
            @Param("termino") String termino,
            @Param("activo") Boolean activo,
            Pageable pageable
    );
}
