package com.sistemas.backend.Rentas.Entity;

import com.sistemas.backend.Clientes.Entity.Cliente;
import com.sistemas.backend.Sucursal.Entity.Sucursal;
import com.sistemas.backend.Vehiculos.Entity.Vehiculo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rentas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Renta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_renta")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal_retiro", nullable = false)
    private Sucursal sucursalRetiro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal_devolucion", nullable = false)
    private Sucursal sucursalDevolucion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin_estimada", nullable = false)
    private LocalDateTime fechaFinEstimada;

    @Column(name = "fecha_devolucion_real")
    private LocalDateTime fechaDevolucionReal;

    @Column(name = "kilometraje_inicial", nullable = false)
    private Integer kilometrajeInicial;

    @Column(name = "kilometraje_final")
    private Integer kilometrajeFinal;

    @Column(name = "costo_total", precision = 10, scale = 2)
    private BigDecimal costoTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRenta estado;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        this.creadoEn = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoRenta.RESERVADA;
        }
        if (this.costoTotal == null) {
            this.costoTotal = BigDecimal.ZERO;
        }
    }
}
