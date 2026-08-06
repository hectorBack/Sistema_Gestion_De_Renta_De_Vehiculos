package com.sistemas.backend.Sucursal.Repository;

import com.sistemas.backend.Sucursal.Entity.Sucursal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Integer> {

    boolean existsByNombreIgnoreCase(String nombre);

    List<Sucursal> findByNombreContainingIgnoreCase(String nombre);

    // Paginación para búsquedas por término
    Page<Sucursal> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}
