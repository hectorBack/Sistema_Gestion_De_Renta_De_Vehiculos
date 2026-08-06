package com.sistemas.backend.Vehiculos.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Entity
@Table(name = "categorias_vehiculo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tarifa_diaria_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaDiariaBase;

    @Column(name = "deposito_garantia", nullable = false, precision = 10, scale = 2)
    private BigDecimal depositoGarantia;

    @Column(name = "activo", nullable = false)
    @ColumnDefault("true")
    private Boolean activo = true;
}
