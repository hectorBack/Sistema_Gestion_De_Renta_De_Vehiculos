package com.sistemas.backend.Pagos.Entity;

import com.sistemas.backend.Rentas.Entity.Renta;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_renta", nullable = false)
    private Renta renta;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago", updatable = false)
    private LocalDateTime fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 30)
    private MetodoPago metodoPago;

    @Column(name = "referencia_transaccion", length = 100)
    private String referenciaTransaccion;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoPago estado;

    @Column(name = "motivo_reembolso")
    private String motivoReembolso;

    @PrePersist
    protected void onCreate() {
        this.fechaPago = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoPago.COMPLETADO;
        }
    }
}
