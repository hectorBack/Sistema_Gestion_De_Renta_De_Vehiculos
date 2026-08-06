package com.sistemas.backend.Vehiculos.Repository;

import com.sistemas.backend.Vehiculos.Entity.EstadoVehiculo;
import com.sistemas.backend.Vehiculos.Entity.Vehiculo;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class VehiculoSpecification {

    public static Specification<Vehiculo> conFiltros(
            Integer idSucursal, EstadoVehiculo estado, Integer idCategoria, String marca) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (idSucursal != null) predicates.add(cb.equal(root.get("sucursalActual").get("id"), idSucursal));
            if (estado != null) predicates.add(cb.equal(root.get("estado"), estado));
            if (idCategoria != null) predicates.add(cb.equal(root.get("categoria").get("id"), idCategoria));
            if (marca != null && !marca.isBlank()) predicates.add(cb.like(cb.lower(root.get("marca")), "%" + marca.toLowerCase() + "%"));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
