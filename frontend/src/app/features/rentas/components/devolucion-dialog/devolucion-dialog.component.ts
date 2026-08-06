import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
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
import { RentaService } from '../../../../core/services/rentas/renta.service';
import { RentaDevolucionRequest, RentaResponse } from '../../../../core/models/renta.model';

export interface DevolucionDialogData {
  renta: RentaResponse; // Renta activa a procesar
}

// Validador personalizado para asegurar que el kilometraje final sea mayor o igual al inicial
function kilometrajeMinimoValidator(kmInicial: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const kmFinal = control.value;
    if (kmFinal === null || kmFinal === undefined || kmFinal === '') return null;
    return kmFinal >= kmInicial ? null : { kilometrajeMenor: { minRequired: kmInicial } };
  };
}

@Component({
  selector: 'app-devolucion-dialog',
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
  templateUrl: './devolucion-dialog.component.html',
  styleUrl: './devolucion-dialog.component.scss',
})
export class DevolucionDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly rentaService = inject(RentaService);
  private readonly dialogRef = inject(MatDialogRef<DevolucionDialogComponent>);
  private readonly snackBar = inject(MatSnackBar);

  public readonly data: DevolucionDialogData = inject(MAT_DIALOG_DATA);

  devolucionForm!: FormGroup;
  isLoading = false;

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    const kmInicial = this.data.renta.kilometrajeInicial || 0;

    this.devolucionForm = this.fb.group({
      fechaDevolucionReal: [new Date(), [Validators.required]],
      kilometrajeFinal: [
        kmInicial,
        [Validators.required, Validators.min(0), kilometrajeMinimoValidator(kmInicial)],
      ],
    });
  }

  guardar(): void {
    if (this.devolucionForm.invalid) {
      this.devolucionForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const formValue = this.devolucionForm.getRawValue();

    const request: RentaDevolucionRequest = {
      fechaDevolucionReal: this.formatDateTimeLocal(formValue.fechaDevolucionReal),
      kilometrajeFinal: Number(formValue.kilometrajeFinal),
    };

    // Endpoint POST /api/v1/rentas/{id}/devolucion (o el método definido en tu RentaService)
    this.rentaService.registrarDevolucion(this.data.renta.id, request).subscribe({
      next: () => {
        this.snackBar.open('Devolución procesada exitosamente', 'Cerrar', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.isLoading = false;
        this.snackBar.open(err.error?.message || 'Error al registrar la devolución', 'Cerrar', {
          duration: 4000,
        });
      },
    });
  }

  cancelar(): void {
    this.dialogRef.close(false);
  }

  /**
   * Convierte un objeto Date a formato LocalDateTime ISO string 'YYYY-MM-DDTHH:mm:ss'
   */
  private formatDateTimeLocal(date: Date): string {
    if (!(date instanceof Date)) return date;
    const pad = (n: number) => (n < 10 ? '0' + n : n);
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(
      date.getHours(),
    )}:${pad(date.getMinutes())}:00`;
  }
}
