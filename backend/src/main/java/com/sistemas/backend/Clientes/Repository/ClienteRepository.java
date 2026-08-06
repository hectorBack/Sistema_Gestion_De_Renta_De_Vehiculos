package com.sistemas.backend.Clientes.Repository;

import com.sistemas.backend.Clientes.Entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findByEmail(String email);

    Optional<Cliente> findByNumLicencia(String numLicencia);

    @Query("""
        SELECT c FROM Cliente c
        WHERE (CAST(:termino AS string) IS NULL OR :termino = '' 
           OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :termino, '%'))
           OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :termino, '%'))
           OR LOWER(c.email) LIKE LOWER(CONCAT('%', :termino, '%'))
           OR LOWER(c.numLicencia) LIKE LOWER(CONCAT('%', :termino, '%')))
          AND (:activo IS NULL OR c.activo = :activo)
    """)
    Page<Cliente> buscarPorCriterios(
            @Param("termino") String termino,
            @Param("activo") Boolean activo,
            Pageable pageable
    );
}
