package com.sistemas.backend.Mantenimiento.Repository;

import com.sistemas.backend.Mantenimiento.Entity.Mantenimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {

    @Query("""
        SELECT m FROM Mantenimiento m 
        JOIN m.vehiculo v 
        WHERE (:activo IS NULL OR m.activo = :activo)
          AND (:idVehiculo IS NULL OR v.id = :idVehiculo)
          AND (CAST(:termino AS string) IS NULL OR (
                LOWER(m.tipo) LIKE LOWER(CONCAT('%', CAST(:termino AS string), '%')) 
             OR LOWER(CAST(m.descripcion AS string)) LIKE LOWER(CONCAT('%', CAST(:termino AS string), '%'))
             OR LOWER(v.placa) LIKE LOWER(CONCAT('%', CAST(:termino AS string), '%'))
          ))
    """)
    Page<Mantenimiento> buscarPaginados(
            @Param("termino") String termino,
            @Param("idVehiculo") Integer idVehiculo,
            @Param("activo") Boolean activo,
            Pageable pageable
    );
}
