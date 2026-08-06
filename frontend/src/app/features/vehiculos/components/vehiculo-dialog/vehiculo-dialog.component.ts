import { Component, Inject, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
  ValidationErrors,
  ValidatorFn,
} from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { Observable, map, startWith } from 'rxjs';

// Angular Material Modules
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatAutocompleteModule } from '@angular/material/autocomplete';

// Servicios y Modelos
import { VehiculoService } from '../../../../core/services/vehiculos/vehiculo.service';
import { CategoriaResponse, VehiculoResponse } from '../../../../core/models/vehiculo.model';
import { SucursalService } from '../../../../core/services/sucursales/sucursal.service';
import { SucursalResponse } from '../../../../core/models/sucursal.model';

export interface VehiculoDialogData {
  vehiculo?: VehiculoResponse; // Si existe, el diálogo entra en modo "Edición"
}

// Validador para asegurar que la opción seleccionada sea un objeto Sucursal real
function requiereSucursalValida(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const val = control.value;
    if (!val) return null; // Los validators nativos (Required) manejan vacíos
    return typeof val === 'object' && val.id ? null : { sucursalInvalida: true };
  };
}

@Component({
  selector: 'app-vehiculo-dialog',
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
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatAutocompleteModule,
  ],
  templateUrl: '../vehiculo-dialog/vehiculo-dialog.component.html',
  styleUrl: '../vehiculo-dialog/vehiculo-dialog.component.scss',
})
export class VehiculoDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly vehiculoService = inject(VehiculoService);
  private readonly sucursalService = inject(SucursalService);
  private readonly dialogRef = inject(MatDialogRef<VehiculoDialogComponent>);
  private readonly snackBar = inject(MatSnackBar);

  public readonly data: VehiculoDialogData = inject(MAT_DIALOG_DATA);

  vehiculoForm!: FormGroup;
  isEditMode = false;
  isLoading = false;

  // Catálogos auxiliares
  categorias: CategoriaResponse[] = [];
  sucursales: SucursalResponse[] = [];
  sucursalesFiltradas$!: Observable<SucursalResponse[]>;

  ngOnInit(): void {
    this.isEditMode = !!this.data?.vehiculo;
    this.initForm();
    this.cargarCategorias();
    this.cargarSucursales();

    if (this.isEditMode && this.data.vehiculo) {
      this.cargarDatosEdicion(this.data.vehiculo);
    }
  }

  private initForm(): void {
    const currentYear = new Date().getFullYear();

    this.vehiculoForm = this.fb.group({
      idCategoria: [null, [Validators.required]],
      // Al editar, la sucursal, VIN y kilometraje se gestionan con otros endpoints específicos
      sucursalInput: [null, this.isEditMode ? [] : [Validators.required, requiereSucursalValida()]],
      idSucursalActual: [null, this.isEditMode ? [] : [Validators.required]],
      vin: ['', this.isEditMode ? [] : [Validators.required, Validators.maxLength(17)]],
      placa: ['', [Validators.required, Validators.maxLength(10)]],
      marca: ['', [Validators.required]],
      modelo: ['', [Validators.required]],
      anio: [
        currentYear,
        [Validators.required, Validators.min(1900), Validators.max(currentYear + 1)],
      ],
      kilometraje: [0, this.isEditMode ? [] : [Validators.required, Validators.min(0)]],
    });

    // Filtro dinámico para el Autocomplete
    this.sucursalesFiltradas$ = this.vehiculoForm.get('sucursalInput')!.valueChanges.pipe(
      startWith(''),
      map((val) => {
        if (typeof val === 'string') {
          this.vehiculoForm.patchValue({ idSucursalActual: null }, { emitEvent: false });
          return this._filtrarSucursales(val);
        } else if (val && typeof val === 'object') {
          this.vehiculoForm.patchValue({ idSucursalActual: val.id }, { emitEvent: false });
          return this._filtrarSucursales(val.nombre);
        }
        return this.sucursales;
      }),
    );
  }

  private _filtrarSucursales(valor: string): SucursalResponse[] {
    const filterValue = (valor || '').toLowerCase();
    return this.sucursales.filter((suc) => suc.nombre.toLowerCase().includes(filterValue));
  }

  displaySucursalFn(sucursal: SucursalResponse | null): string {
    return sucursal && sucursal.nombre ? sucursal.nombre : '';
  }

  private cargarSucursales(): void {
    this.sucursalService.listarTodas(0, 500).subscribe({
      next: (page) => {
        this.sucursales = page.content;
      },
      error: (err) => console.error('Error al cargar sucursales:', err),
    });
  }

  private cargarDatosEdicion(vehiculo: VehiculoResponse): void {
    // Deshabilitar campos que no forman parte del UpdateRequest
    this.vehiculoForm.get('vin')?.disable();
    this.vehiculoForm.get('sucursalInput')?.disable();
    this.vehiculoForm.get('idSucursalActual')?.disable();
    this.vehiculoForm.get('kilometraje')?.disable();

    this.vehiculoForm.patchValue({
      placa: vehiculo.placa,
      marca: vehiculo.marca,
      modelo: vehiculo.modelo,
      anio: vehiculo.anio,
      vin: vehiculo.vin,
      kilometraje: vehiculo.kilometraje,
      sucursalInput: vehiculo.sucursalNombre || '',
    });
  }

  private cargarCategorias(): void {
    this.vehiculoService.listarCategorias().subscribe({
      next: (data) => {
        this.categorias = data;

        // Si estamos editando, asociar la categoría activa si coincide por nombre
        if (this.isEditMode && this.data.vehiculo) {
          const cat = this.categorias.find((c) => c.nombre === this.data.vehiculo?.categoriaNombre);
          if (cat) {
            this.vehiculoForm.patchValue({ idCategoria: cat.id });
          }
        }
      },
      error: (err) => console.error('Error al cargar categorías:', err),
    });
  }

  guardar(): void {
    if (this.vehiculoForm.invalid) {
      this.vehiculoForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const formValue = this.vehiculoForm.getRawValue();

    if (this.isEditMode && this.data.vehiculo) {
      // Endpoint PUT /api/v1/vehiculos/{id}
      const updateReq = {
        idCategoria: formValue.idCategoria,
        placa: formValue.placa,
        marca: formValue.marca,
        modelo: formValue.modelo,
        anio: formValue.anio,
      };

      this.vehiculoService.actualizarVehiculo(this.data.vehiculo.id, updateReq).subscribe({
        next: (res) => {
          this.snackBar.open('Vehículo actualizado correctamente', 'Cerrar', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.isLoading = false;
          this.snackBar.open(err.error?.message || 'Error al actualizar el vehículo', 'Cerrar', {
            duration: 4000,
          });
        },
      });
    } else {
      // Endpoint POST /api/v1/vehiculos
      this.vehiculoService.crearVehiculo(formValue).subscribe({
        next: (res) => {
          this.snackBar.open('Vehículo registrado correctamente', 'Cerrar', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.isLoading = false;
          this.snackBar.open(err.error?.message || 'Error al guardar el vehículo', 'Cerrar', {
            duration: 4000,
          });
        },
      });
    }
  }

  cancelar(): void {
    this.dialogRef.close(false);
  }
}
