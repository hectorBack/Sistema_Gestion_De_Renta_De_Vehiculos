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
import { MatAutocompleteModule } from '@angular/material/autocomplete';

// RxJS Operators
import { Observable, of } from 'rxjs';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  map,
  startWith,
  switchMap,
} from 'rxjs/operators';

// Servicios y Modelos
import { RentaService } from '../../../../core/services/rentas/renta.service';
import { RentaCreateRequest, RentaResponse } from '../../../../core/models/renta.model';
import { ClienteService } from '../../../../core/services/clientes/cliente.service';
import { ClienteResponse } from '../../../../core/models/cliente.model';
import { VehiculoService } from '../../../../core/services/vehiculos/vehiculo.service';
import { VehiculoResponse } from '../../../../core/models/vehiculo.model';
import { SucursalService } from '../../../../core/services/sucursales/sucursal.service';
import { SucursalResponse } from '../../../../core/models/sucursal.model';

export interface RentaDialogData {
  renta?: RentaResponse; // Si existe, entra en modo "Edición/Detalle"
}

@Component({
  selector: 'app-renta-dialog',
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
    MatAutocompleteModule,
  ],
  templateUrl: './renta-dialog.component.html',
  styleUrl: './renta-dialog.component.scss',
})
export class RentaDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly rentaService = inject(RentaService);
  private readonly clienteService = inject(ClienteService);
  private readonly vehiculoService = inject(VehiculoService);
  private readonly sucursalService = inject(SucursalService);
  private readonly dialogRef = inject(MatDialogRef<RentaDialogComponent>);
  private readonly snackBar = inject(MatSnackBar);

  public readonly data: RentaDialogData | null = inject(MAT_DIALOG_DATA, { optional: true });

  rentaForm!: FormGroup;
  isEditMode = false;
  isLoading = false;
  isLoadingCatalogos = false;

  // Observables para autocompletado
  clientesFiltrados$!: Observable<ClienteResponse[]>;
  vehiculosFiltrados$!: Observable<VehiculoResponse[]>;

  sucursales: SucursalResponse[] = [];

  // Restricción de fecha mínima
  minDate: Date = new Date();

  ngOnInit(): void {
    this.isEditMode = !!this.data?.renta;
    this.initForm();
    this.cargarSucursales();
    this.setupAutocompletes();

    if (this.isEditMode && this.data?.renta) {
      this.cargarDatosEdicion(this.data.renta);
    }
  }

  private initForm(): void {
    this.rentaForm = this.fb.group({
      clienteInput: [null, [Validators.required]],
      vehiculoInput: [null, [Validators.required]],
      sucursalRetiroId: [null, [Validators.required]],
      sucursalDevolucionId: [null, [Validators.required]],
      fechaInicio: [new Date(), [Validators.required]],
      fechaFinEstimada: [null, [Validators.required]],
      kilometrajeInicial: [0, [Validators.required, Validators.min(0)]],
    });

    // Cuando se selecciona un vehículo del autocomplete, asigna automáticamente su kilometraje
    this.rentaForm.get('vehiculoInput')?.valueChanges.subscribe((value) => {
      if (value && typeof value === 'object' && 'kilometraje' in value) {
        this.rentaForm.patchValue({
          kilometrajeInicial: value.kilometraje || 0,
        });
      }
    });
  }

  /**
   * Configura las búsquedas reactivas para los Autocompletes
   */
  setupAutocompletes(): void {
    // Autocomplete Cliente
    this.clientesFiltrados$ = this.rentaForm.get('clienteInput')!.valueChanges.pipe(
      startWith(''),
      debounceTime(300),
      distinctUntilChanged(),
      switchMap((term) => {
        const query = typeof term === 'string' ? term : term?.nombreCompleto || '';
        return this.clienteService.buscarConFiltros(query, true, 0, 20).pipe(
          map((page) => page?.content || []),
          catchError(() => of([])), // 🔴 FIX 2: Evita romper el observable si falla HTTP
        );
      }),
    );

    // Autocomplete Vehículo
    this.vehiculosFiltrados$ = this.rentaForm.get('vehiculoInput')!.valueChanges.pipe(
      startWith(''),
      debounceTime(300),
      distinctUntilChanged(),
      switchMap((term) => {
        const query = typeof term === 'string' ? term : term?.placa || term?.marca || '';
        return this.vehiculoService.buscarConFiltros({ estado: 'DISPONIBLE', query }, 0, 20).pipe(
          map((page) => page?.content || []),
          catchError(() => of([])), // 🔴 FIX 2: Protege la suscripción reactiva
        );
      }),
    );
  }

  private cargarSucursales(): void {
    this.isLoadingCatalogos = true;
    this.sucursalService.listarTodas().subscribe({
      next: (res: any) => {
        this.sucursales = res.content ? res.content : res;
        this.isLoadingCatalogos = false;
      },
      error: (err) => {
        console.error('Error al cargar sucursales:', err);
        this.isLoadingCatalogos = false;
      },
    });
  }

  private cargarDatosEdicion(renta: RentaResponse): void {
    // Si viene fecha en string ISO, la parseamos a Date
    const fInicio = renta.fechaInicio ? new Date(renta.fechaInicio) : null;
    const fFin = renta.fechaFinEstimada ? new Date(renta.fechaFinEstimada) : null;

    this.rentaForm.patchValue({
      fechaInicio: fInicio,
      fechaFinEstimada: fFin,
      kilometrajeInicial: renta.kilometrajeInicial,
    });
  }

  // Funciones de despliegue para los Autocompletes
  displayClienteFn(cliente: ClienteResponse): string {
    return cliente && cliente.nombreCompleto
      ? `${cliente.nombreCompleto} (Lic: ${cliente.numLicencia})`
      : '';
  }

  displayVehiculoFn(vehiculo: VehiculoResponse): string {
    return vehiculo && vehiculo.marca
      ? `${vehiculo.marca} ${vehiculo.modelo} [${vehiculo.placa}]`
      : '';
  }

  guardar(): void {
    if (this.rentaForm.invalid) {
      this.rentaForm.markAllAsTouched();
      return;
    }

    const formValue = this.rentaForm.getRawValue();

    // Validar que se haya seleccionado un objeto válido del Autocomplete y no un texto suelto
    if (typeof formValue.clienteInput !== 'object' || !formValue.clienteInput?.id) {
      this.snackBar.open('Debes seleccionar un cliente válido de la lista', 'Cerrar', {
        duration: 3000,
      });
      return;
    }

    if (typeof formValue.vehiculoInput !== 'object' || !formValue.vehiculoInput?.id) {
      this.snackBar.open('Debes seleccionar un vehículo válido de la lista', 'Cerrar', {
        duration: 3000,
      });
      return;
    }

    this.isLoading = true;

    // 1. Tomamos la fecha seleccionada
    let fechaInicioObj = new Date(formValue.fechaInicio);
    const ahora = new Date();

    // Si la fecha de inicio elegida es "Hoy", le asignamos la hora exacta actual
    // +2 minutos extra para evitar rechazos por milisegundos de latencia con el backend
    if (this.esMismoDia(fechaInicioObj, ahora)) {
      fechaInicioObj = new Date(ahora.getTime() + 2 * 60 * 1000);
    }

    // 2. Formatear ambas fechas a ISO String para Spring Boot
    const fechaInicioIso = this.formatDateTimeLocal(fechaInicioObj);
    const fechaFinIso = this.formatDateTimeLocal(new Date(formValue.fechaFinEstimada));

    const request: RentaCreateRequest = {
      idCliente: Number(formValue.clienteInput.id),
      idVehiculo: Number(formValue.vehiculoInput.id),
      idSucursalRetiro: Number(formValue.sucursalRetiroId),
      idSucursalDevolucion: Number(formValue.sucursalDevolucionId),
      fechaInicio: fechaInicioIso,
      fechaFinEstimada: fechaFinIso,
      kilometrajeInicial: Number(formValue.kilometrajeInicial),
    };

    this.rentaService.crearRenta(request).subscribe({
      next: () => {
        this.snackBar.open('Renta registrada exitosamente', 'Cerrar', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.isLoading = false;
        this.snackBar.open(err.error?.message || 'Error al registrar la renta', 'Cerrar', {
          duration: 4000,
        });
      },
    });
  }

  private esMismoDia(d1: Date, d2: Date): boolean {
    return (
      d1.getFullYear() === d2.getFullYear() &&
      d1.getMonth() === d2.getMonth() &&
      d1.getDate() === d2.getDate()
    );
  }

  cancelar(): void {
    this.dialogRef.close(false);
  }

  /**
   * Helper para convertir un objeto Date a formato ISO YYYY-MM-THH:mm:ss
   */
  private formatDateTimeLocal(date: Date): string {
    if (!(date instanceof Date)) return date;
    const pad = (n: number) => (n < 10 ? '0' + n : n);
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(
      date.getHours(),
    )}:${pad(date.getMinutes())}:00`;
  }
}
