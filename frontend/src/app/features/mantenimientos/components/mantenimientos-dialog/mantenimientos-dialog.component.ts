import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

// Angular Material Modules
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

// Servicios y Modelos
import { MantenimientoService } from '../../../../core/services/mantenimientos/mantenimiento.service';
import { VehiculoService } from '../../../../core/services/vehiculos/vehiculo.service';
import {
  MantenimientoRequest,
  MantenimientoResponse,
} from '../../../../core/models/mantenimiento.model';
import { VehiculoResponse } from '../../../../core/models/vehiculo.model';

export interface MantenimientoDialogData {
  mantenimiento?: MantenimientoResponse; // Si existe, entra en modo "Edición"
}

@Component({
  selector: 'app-mantenimientos-dialog',
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
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  templateUrl: './mantenimientos-dialog.component.html',
})
export class MantenimientoDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly mantenimientoService = inject(MantenimientoService);
  private readonly vehiculoService = inject(VehiculoService);
  private readonly dialogRef = inject(MatDialogRef<MantenimientoDialogComponent>);
  private readonly snackBar = inject(MatSnackBar);

  public readonly data: MantenimientoDialogData | null = inject(MAT_DIALOG_DATA, {
    optional: true,
  });

  mantenimientoForm!: FormGroup;
  isEditMode = false;
  isLoading = false;
  isLoadingVehiculos = false;

  // Catálogo de vehículos para el mat-select
  vehiculosList: VehiculoResponse[] = [];

  // Tipos de Mantenimiento sugeridos
  tiposMantenimiento: string[] = [
    'PREVENTIVO',
    'CORRECTIVO',
    'CAMBIO_ACEITE',
    'FRENOS',
    'NEUMATICOS',
    'GENERAL',
  ];

  ngOnInit(): void {
    this.isEditMode = !!this.data?.mantenimiento;
    this.initForm();
    this.cargarVehiculos();

    if (this.isEditMode && this.data?.mantenimiento) {
      this.cargarDatosEdicion(this.data.mantenimiento);
    }
  }

  private initForm(): void {
    this.mantenimientoForm = this.fb.group({
      idVehiculo: [null, [Validators.required]],
      tipo: ['', [Validators.required]],
      costo: [null, [Validators.required, Validators.min(0)]],
      fechaMantenimiento: [new Date(), [Validators.required]],
      descripcion: ['', [Validators.maxLength(500)]],
    });
  }

  private cargarVehiculos(): void {
    this.isLoadingVehiculos = true;
    // Si necesitas todos los vehículos paginados o una lista general, usas buscarConFiltros
    this.vehiculoService.buscarConFiltros({}, 0, 100).subscribe({
      next: (page) => {
        this.vehiculosList = page.content;
        this.isLoadingVehiculos = false;
      },
      error: (err) => {
        console.error('Error al cargar la lista de vehículos:', err);
        this.isLoadingVehiculos = false;
        this.snackBar.open('No se pudieron cargar los vehículos', 'Cerrar', { duration: 3000 });
      },
    });
  }

  private cargarDatosEdicion(mantenimiento: MantenimientoResponse): void {
    // Parsear la fecha 'YYYY-MM-DD' a objeto Date si viene como string
    let fecha: Date = new Date();
    if (mantenimiento.fechaMantenimiento) {
      const [year, month, day] = mantenimiento.fechaMantenimiento.split('-').map(Number);
      fecha = new Date(year, month - 1, day);
    }

    this.mantenimientoForm.patchValue({
      idVehiculo: mantenimiento.idVehiculo,
      tipo: mantenimiento.tipo,
      costo: mantenimiento.costo,
      fechaMantenimiento: fecha,
      descripcion: mantenimiento.descripcion,
    });
  }

  guardar(): void {
    if (this.mantenimientoForm.invalid) {
      this.mantenimientoForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const formValue = this.mantenimientoForm.getRawValue();

    // Formatear Date a 'YYYY-MM-DD' para la API de Spring Boot
    const fecha: Date = formValue.fechaMantenimiento;
    const formattedFecha = fecha instanceof Date ? fecha.toISOString().split('T')[0] : fecha;

    const request: MantenimientoRequest = {
      idVehiculo: formValue.idVehiculo,
      tipo: formValue.tipo,
      costo: formValue.costo,
      fechaMantenimiento: formattedFecha,
      descripcion: formValue.descripcion,
    };

    if (this.isEditMode && this.data?.mantenimiento) {
      this.mantenimientoService
        .actualizarMantenimiento(this.data.mantenimiento.id, request)
        .subscribe({
          next: () => {
            this.snackBar.open('Mantenimiento actualizado correctamente', 'Cerrar', {
              duration: 3000,
            });
            this.dialogRef.close(true);
          },
          error: (err) => {
            this.isLoading = false;
            this.snackBar.open(
              err.error?.message || 'Error al actualizar el mantenimiento',
              'Cerrar',
              {
                duration: 4000,
              },
            );
          },
        });
    } else {
      this.mantenimientoService.registrarMantenimiento(request).subscribe({
        next: () => {
          this.snackBar.open('Mantenimiento registrado correctamente', 'Cerrar', {
            duration: 3000,
          });
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.isLoading = false;
          this.snackBar.open(
            err.error?.message || 'Error al registrar el mantenimiento',
            'Cerrar',
            {
              duration: 4000,
            },
          );
        },
      });
    }
  }

  cancelar(): void {
    this.dialogRef.close(false);
  }
}
