import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

// Angular Material Modules
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

// Servicios y Modelos
import { SucursalService } from '../../../../core/services/sucursales/sucursal.service';
import { SucursalRequest, SucursalResponse } from '../../../../core/models/sucursal.model';

export interface SucursalDialogData {
  sucursal?: SucursalResponse; // Si existe, entra en modo "Edición"
}

@Component({
  selector: 'app-sucursal-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  templateUrl: './sucursal-dialog.component.html',
})
export class SucursalDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly sucursalService = inject(SucursalService);
  private readonly dialogRef = inject(MatDialogRef<SucursalDialogComponent>);
  private readonly snackBar = inject(MatSnackBar);

  public readonly data: SucursalDialogData | null = inject(MAT_DIALOG_DATA, { optional: true });

  sucursalForm!: FormGroup;
  isEditMode = false;
  isLoading = false;

  ngOnInit(): void {
    this.isEditMode = !!this.data?.sucursal;
    this.initForm();

    if (this.isEditMode && this.data?.sucursal) {
      this.cargarDatosEdicion(this.data.sucursal);
    }
  }

  private initForm(): void {
    this.sucursalForm = this.fb.group({
      nombre: ['', [Validators.required]],
      direccion: ['', [Validators.required]],
      telefono: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
    });
  }

  private cargarDatosEdicion(sucursal: SucursalResponse): void {
    this.sucursalForm.patchValue({
      nombre: sucursal.nombre,
      direccion: sucursal.direccion,
      telefono: sucursal.telefono,
      email: sucursal.email,
    });
  }

  guardar(): void {
    if (this.sucursalForm.invalid) {
      this.sucursalForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const request: SucursalRequest = this.sucursalForm.getRawValue();

    if (this.isEditMode && this.data?.sucursal) {
      // Endpoint PUT /api/v1/sucursales/{id}
      this.sucursalService.actualizarSucursal(this.data.sucursal.id, request).subscribe({
        next: () => {
          this.snackBar.open('Sucursal actualizada correctamente', 'Cerrar', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.isLoading = false;
          this.snackBar.open(err.error?.message || 'Error al actualizar la sucursal', 'Cerrar', {
            duration: 4000,
          });
        },
      });
    } else {
      // Endpoint POST /api/v1/sucursales
      this.sucursalService.crearSucursal(request).subscribe({
        next: () => {
          this.snackBar.open('Sucursal registrada correctamente', 'Cerrar', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.isLoading = false;
          this.snackBar.open(err.error?.message || 'Error al registrar la sucursal', 'Cerrar', {
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
