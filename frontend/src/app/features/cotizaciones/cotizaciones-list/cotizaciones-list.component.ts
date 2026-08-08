import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';

// Angular Material
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

// Servicios y Modelos
import { CotizacionService } from '../../../core/services/cotizaciones/cotizacion.service';
import { VehiculoService } from '../../../core/services/vehiculos/vehiculo.service';
import { CotizacionResponse } from '../../../core/models/cotizacion.model';
import { VehiculoResponse, CategoriaResponse } from '../../../core/models/vehiculo.model';

@Component({
  selector: 'app-cotizador-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './cotizaciones-list.component.html',
})
export class CotizacionesListComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly cotizacionService = inject(CotizacionService);
  private readonly vehiculoService = inject(VehiculoService);
  private readonly dialogRef = inject(MatDialogRef<CotizacionesListComponent>, {
    optional: true,
  });

  // Formularios
  cotizadorForm!: FormGroup;
  filterForm!: FormGroup;

  // Estados y Listas
  isLoading = false;
  resultado: CotizacionResponse | null = null;

  vehiculosList: VehiculoResponse[] = [];
  categoriasList: CategoriaResponse[] = [];
  cotizaciones: CotizacionResponse[] = [];

  // Columnas para la tabla
  displayedColumns: string[] = [
    'id',
    'vehiculoOcategoriaInfo',
    'diasTotales',
    'tarifaDiariaBase',
    'subtotalGeneral',
    'impuestos',
    'totalEstimado',
    'acciones',
  ];

  ngOnInit(): void {
    this.initForm();
    this.initFilterForm();
    this.cargarCatalogos();
  }

  private initForm(): void {
    const hoy = new Date();
    const manana = new Date();
    manana.setDate(hoy.getDate() + 1);

    this.cotizadorForm = this.fb.group({
      tipoSeleccion: ['categoria'], // 'categoria' | 'vehiculo'
      idCategoria: [null],
      idVehiculo: [null],
      fechaInicio: [hoy, [Validators.required]],
      fechaFin: [manana, [Validators.required]],
      incluyeSeguro: [true],
      conductorAdicional: [false],
    });
  }

  private initFilterForm(): void {
    this.filterForm = this.fb.group({
      termino: [''],
      idCategoria: [null],
    });
  }

  private cargarCatalogos(): void {
    this.vehiculoService.listarCategorias().subscribe((cats) => (this.categoriasList = cats));
    this.vehiculoService
      .buscarConFiltros({}, 0, 100)
      .subscribe((page) => (this.vehiculosList = page.content));
  }

  aplicarFiltros(): void {
    // Lógica para filtrar cotizaciones locales si se requiere
  }

  limpiarFiltros(): void {
    this.filterForm.reset({
      termino: '',
      idCategoria: null,
    });
  }

  calcular(): void {
    if (this.cotizadorForm.invalid) {
      this.cotizadorForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const formVal = this.cotizadorForm.value;

    const request = {
      idCategoria: formVal.tipoSeleccion === 'categoria' ? formVal.idCategoria : null,
      idVehiculo: formVal.tipoSeleccion === 'vehiculo' ? formVal.idVehiculo : null,
      fechaInicio: new Date(formVal.fechaInicio).toISOString(),
      fechaFin: new Date(formVal.fechaFin).toISOString(),
      incluyeSeguro: formVal.incluyeSeguro,
      conductorAdicional: formVal.conductorAdicional,
    };

    this.cotizacionService.calcularCotizacion(request).subscribe({
      next: (res) => {
        this.resultado = res;
        this.cotizaciones = [res, ...this.cotizaciones];
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error al cotizar:', err);
        this.isLoading = false;
      },
    });
  }

  limpiar(): void {
    this.resultado = null;
    this.cotizadorForm.reset({
      tipoSeleccion: 'categoria',
      fechaInicio: new Date(),
      fechaFin: new Date(Date.now() + 86400000),
      incluyeSeguro: true,
      conductorAdicional: false,
    });
  }

  cerrar(): void {
    if (this.dialogRef) {
      this.dialogRef.close();
    } else {
      this.limpiar();
    }
  }

  abrirCotizadorDialog(): void {
    // Método para desencadenar el cálculo o acción principal
    this.calcular();
  }

  verDetalleCotizacion(cotizacion: CotizacionResponse): void {
    this.resultado = cotizacion;
  }

  convertirEnRenta(cotizacion: CotizacionResponse): void {
    // Acción personalizada al seleccionar una cotización
  }
}
