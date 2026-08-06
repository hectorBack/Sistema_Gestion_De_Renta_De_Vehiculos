import { Component, OnInit, ElementRef, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

// Angular Material Modules
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

// Chart.js e integración
import { Chart, registerables } from 'chart.js';
Chart.register(...registerables);

// Servicios y Modelos
import { DashboardService } from '../../../core/services/dashboard/dashboard.service';
import { DashboardMetrics } from '../../../core/models/dashboard.model';

@Component({
  selector: 'app-dashboard-list',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatSnackBarModule,
  ],
  templateUrl: './dashboard-list.component.html',
  styleUrl: './dashboard-list.component.scss',
})
export class DashboardListComponent implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly snackBar = inject(MatSnackBar);

  // Referencias HTML para dibujar los Canvas de Chart.js
  @ViewChild('ingresosChart') ingresosChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('topVehiculosChart') topVehiculosChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('sucursalesChart') sucursalesChartRef!: ElementRef<HTMLCanvasElement>;

  // Instancias de los gráficos para destruirlos/actualizarlos limpiamente
  private ingresosChart?: Chart;
  private topVehiculosChart?: Chart;
  private sucursalesChart?: Chart;

  // Estado del componente
  metrics: DashboardMetrics | null = null;
  isLoading = false;

  // Columnas para la tabla de últimas rentas
  displayedColumns: string[] = [
    'idRenta',
    'clienteNombre',
    'vehiculoInfo',
    'fechaInicio',
    'estado',
  ];

  ngOnInit(): void {
    this.cargarMetricas();
  }

  /**
   * Obtiene la información consolidada desde el backend
   */
  cargarMetricas(): void {
    this.isLoading = true;
    this.dashboardService.obtenerMetricas().subscribe({
      next: (data) => {
        this.metrics = data;
        this.isLoading = false;

        // Renderizamos las gráficas en el siguiente ciclo del Event Loop
        setTimeout(() => this.inicializarGraficos(), 100);
      },
      error: (err) => {
        console.error('Error al cargar las métricas del dashboard:', err);
        this.isLoading = false;
        this.snackBar.open('Error al cargar la información del Dashboard', 'Cerrar', {
          duration: 4000,
        });
      },
    });
  }

  /**
   * Crea y renderiza los 3 gráficos en la pantalla
   */
  private inicializarGraficos(): void {
    if (!this.metrics) return;

    this.destruirGraficos();

    // 1. Gráfico de Líneas: Ingresos Mensuales
    if (this.ingresosChartRef) {
      const labels = this.metrics.graficos.ingresosMensuales.map(
        (i) => `${this.obtenerNombreMes(i.mes)} ${i.anio}`,
      );
      const data = this.metrics.graficos.ingresosMensuales.map((i) => i.total);

      this.ingresosChart = new Chart(this.ingresosChartRef.nativeElement, {
        type: 'line',
        data: {
          labels,
          datasets: [
            {
              label: 'Ingresos ($)',
              data,
              borderColor: '#1a73e8',
              backgroundColor: 'rgba(26, 115, 232, 0.1)',
              fill: true,
              tension: 0.3,
              pointBackgroundColor: '#1a73e8',
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { display: false } },
        },
      });
    }

    // 2. Gráfico de Barras Horizontales: Top Vehículos
    if (this.topVehiculosChartRef) {
      const labels = this.metrics.graficos.topVehiculosMasRentados.map(
        (v) => `${v.marca} ${v.modelo}`,
      );
      const data = this.metrics.graficos.topVehiculosMasRentados.map((v) => v.totalRentas);

      this.topVehiculosChart = new Chart(this.topVehiculosChartRef.nativeElement, {
        type: 'bar',
        data: {
          labels,
          datasets: [
            {
              label: 'Total Rentas',
              data,
              backgroundColor: '#137333',
              borderRadius: 6,
            },
          ],
        },
        options: {
          indexAxis: 'y', // Barras horizontales
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { display: false } },
        },
      });
    }

    // 3. Gráfico Dona / Pastel: Rentas por Sucursal
    if (this.sucursalesChartRef) {
      const labels = this.metrics.graficos.rentasPorSucursal.map((s) => s.sucursal);
      const data = this.metrics.graficos.rentasPorSucursal.map((s) => s.totalRentas);

      this.sucursalesChart = new Chart(this.sucursalesChartRef.nativeElement, {
        type: 'doughnut',
        data: {
          labels,
          datasets: [
            {
              data,
              backgroundColor: ['#1a73e8', '#137333', '#f9ab00', '#d93025', '#a142f4'],
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { position: 'bottom' } },
        },
      });
    }
  }

  /**
   * Destruye las instancias para evitar fugas de memoria o solapamientos
   */
  private destruirGraficos(): void {
    if (this.ingresosChart) this.ingresosChart.destroy();
    if (this.topVehiculosChart) this.topVehiculosChart.destroy();
    if (this.sucursalesChart) this.sucursalesChart.destroy();
  }

  /**
   * Helper para formatear el número de mes a nombre legible
   */
  private obtenerNombreMes(mes: number): string {
    const meses = [
      'Ene',
      'Feb',
      'Mar',
      'Abr',
      'May',
      'Jun',
      'Jul',
      'Ago',
      'Sep',
      'Oct',
      'Nov',
      'Dic',
    ];
    return meses[mes - 1] || '';
  }

  /**
   * Asigna la clase del badge según el estado de la renta
   */
  getEstadoBadgeClass(estado: string): string {
    switch (estado?.toUpperCase()) {
      case 'ACTIVA':
      case 'EN_CURSO':
        return 'badge-activa';
      case 'RESERVADA':
        return 'badge-reservada';
      case 'COMPLETADA':
      case 'FINALIZADA':
        return 'badge-completada';
      case 'CANCELADA':
        return 'badge-cancelada';
      default:
        return 'badge-completada';
    }
  }
}
