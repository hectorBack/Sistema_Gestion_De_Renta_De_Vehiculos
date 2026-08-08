import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

// Angular Material Modules
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

// Servicios y Modelos
import { ClienteService } from '../../../../core/services/clientes/cliente.service';
import { ClienteRequest, ClienteResponse } from '../../../../core/models/cliente.model';

export interface ClienteDialogData {
  cliente?: ClienteResponse; // Si existe, entra en modo "Edición"
}

@Component({
  selector: 'app-cliente-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  templateUrl: './cliente-dialog.component.html',
})
export class ClienteDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly clienteService = inject(ClienteService);
  private readonly dialogRef = inject(MatDialogRef<ClienteDialogComponent>);
  private readonly snackBar = inject(MatSnackBar);

  public readonly data: ClienteDialogData | null = inject(MAT_DIALOG_DATA, { optional: true });

  clienteForm!: FormGroup;
  isEditMode = false;
  isLoading = false;

  // Fecha mínima para el datepicker de la licencia (debe ser mayor a la fecha actual)
  minDate: Date = new Date();

  ngOnInit(): void {
    this.isEditMode = !!this.data?.cliente;
    this.initForm();

    if (this.isEditMode && this.data?.cliente) {
      this.cargarDatosEdicion(this.data.cliente);
    }
  }

  private initForm(): void {
    this.clienteForm = this.fb.group({
      nombre: ['', [Validators.required]],
      apellido: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      telefono: ['', [Validators.required]],
      numLicencia: ['', [Validators.required]],
      vencimientoLicencia: [null, [Validators.required]],
    });
  }

  private cargarDatosEdicion(cliente: ClienteResponse): void {
    // Si la API devuelve "Nombre Apellido" juntos en nombreCompleto,
    // intentamos separar el primer espacio para precargar los inputs.
    const partesNombre = cliente.nombreCompleto ? cliente.nombreCompleto.split(' ') : ['', ''];
    const nombre = partesNombre[0] || '';
    const apellido = partesNombre.slice(1).join(' ') || '';

    // Convertir la fecha 'YYYY-MM-DD' a objeto Date si viene en formato string
    let fechaVencimiento: Date | null = null;
    if (cliente.vencimientoLicencia) {
      const [year, month, day] = cliente.vencimientoLicencia.split('-').map(Number);
      fechaVencimiento = new Date(year, month - 1, day);
    }

    this.clienteForm.patchValue({
      nombre: nombre,
      apellido: apellido,
      email: cliente.email,
      telefono: cliente.telefono,
      numLicencia: cliente.numLicencia,
      vencimientoLicencia: fechaVencimiento,
    });
  }

  guardar(): void {
    if (this.clienteForm.invalid) {
      this.clienteForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const formValue = this.clienteForm.getRawValue();

    // Formatear Date a 'YYYY-MM-DD' para la API Spring Boot
    const fecha: Date = formValue.vencimientoLicencia;
    const formattedFecha = fecha instanceof Date ? fecha.toISOString().split('T')[0] : fecha;

    const request: ClienteRequest = {
      nombre: formValue.nombre,
      apellido: formValue.apellido,
      email: formValue.email,
      telefono: formValue.telefono,
      numLicencia: formValue.numLicencia,
      vencimientoLicencia: formattedFecha,
    };

    if (this.isEditMode && this.data?.cliente) {
      // Endpoint PUT /api/v1/clientes/{id}
      this.clienteService.actualizarCliente(this.data.cliente.id, request).subscribe({
        next: () => {
          this.snackBar.open('Cliente actualizado correctamente', 'Cerrar', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.isLoading = false;
          this.snackBar.open(err.error?.message || 'Error al actualizar el cliente', 'Cerrar', {
            duration: 4000,
          });
        },
      });
    } else {
      // Endpoint POST /api/v1/clientes
      this.clienteService.registrarCliente(request).subscribe({
        next: () => {
          this.snackBar.open('Cliente registrado correctamente', 'Cerrar', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.isLoading = false;
          this.snackBar.open(err.error?.message || 'Error al registrar el cliente', 'Cerrar', {
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
