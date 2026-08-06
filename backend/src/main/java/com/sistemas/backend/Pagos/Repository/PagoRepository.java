package com.sistemas.backend.Pagos.Repository;

import com.sistemas.backend.Pagos.Entity.EstadoPago;
import com.sistemas.backend.Pagos.Entity.MetodoPago;
import com.sistemas.backend.Pagos.Entity.Pago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByRentaId(Long idRenta);

    // Suma total pagada para una renta específica (útil para verificar liquidación)
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p " +
            "WHERE p.renta.id = :idRenta AND p.estado = 'COMPLETADO'")
    BigDecimal obtenerTotalPagadoPorRenta(@Param("idRenta") Long idRenta);

    @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.renta.id = :idRenta AND p.estado = :estado")
    Optional<BigDecimal> sumMontoByRentaIdAndEstado(@Param("idRenta") Long idRenta, @Param("estado") EstadoPago estado);

    @Query("SELECT p FROM Pago p " +
            "WHERE (:idRenta IS NULL OR p.renta.id = :idRenta) " +
            "AND (:estado IS NULL OR p.estado = :estado) " +
            "AND (:metodoPago IS NULL OR p.metodoPago = :metodoPago)")
    Page<Pago> buscarConFiltros(
            @Param("idRenta") Long idRenta,
            @Param("estado") EstadoPago estado,
            @Param("metodoPago") MetodoPago metodoPago,
            Pageable pageable);
}
