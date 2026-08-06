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
import { CategoriaService } from '../../../../core/services/categorias/categoria.service';
import { CategoriaRequest, CategoriaResponse } from '../../../../core/models/categoria.model';

export interface CategoriaDialogData {
  categoria?: CategoriaResponse; // Si existe, entra en modo "Edición"
}

@Component({
  selector: 'app-categoria-dialog',
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
  templateUrl: './categorias-dialog.component.html',
  styleUrl: './categorias-dialog.component.scss',
})
export class CategoriaDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly categoriaService = inject(CategoriaService);
  private readonly dialogRef = inject(MatDialogRef<CategoriaDialogComponent>);
  private readonly snackBar = inject(MatSnackBar);

  public readonly data: CategoriaDialogData | null = inject(MAT_DIALOG_DATA, { optional: true });

  categoriaForm!: FormGroup;
  isEditMode = false;
  isLoading = false;

  ngOnInit(): void {
    this.isEditMode = !!this.data?.categoria;
    this.initForm();

    if (this.isEditMode && this.data?.categoria) {
      this.cargarDatosEdicion(this.data.categoria);
    }
  }

  private initForm(): void {
    this.categoriaForm = this.fb.group({
      nombre: ['', [Validators.required]],
      descripcion: [''],
      tarifaDiariaBase: [null, [Validators.required, Validators.min(0)]],
      depositoGarantia: [null, [Validators.required, Validators.min(0)]],
    });
  }

  private cargarDatosEdicion(categoria: CategoriaResponse): void {
    this.categoriaForm.patchValue({
      nombre: categoria.nombre,
      descripcion: categoria.descripcion,
      tarifaDiariaBase: categoria.tarifaDiariaBase,
      depositoGarantia: categoria.depositoGarantia,
    });
  }

  guardar(): void {
    if (this.categoriaForm.invalid) {
      this.categoriaForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const formValue = this.categoriaForm.getRawValue();

    const request: CategoriaRequest = {
      nombre: formValue.nombre,
      descripcion: formValue.descripcion,
      tarifaDiariaBase: Number(formValue.tarifaDiariaBase),
      depositoGarantia: Number(formValue.depositoGarantia),
    };

    if (this.isEditMode && this.data?.categoria) {
      // Endpoint PUT para actualizar la categoría
      this.categoriaService.actualizarCategoria(this.data.categoria.id, request).subscribe({
        next: () => {
          this.snackBar.open('Categoría actualizada correctamente', 'Cerrar', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.isLoading = false;
          this.snackBar.open(err.error?.message || 'Error al actualizar la categoría', 'Cerrar', {
            duration: 4000,
          });
        },
      });
    } else {
      // Endpoint POST /api/v1/vehiculos/categorias
      this.categoriaService.crearCategoria(request).subscribe({
        next: () => {
          this.snackBar.open('Categoría registrada correctamente', 'Cerrar', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.isLoading = false;
          this.snackBar.open(err.error?.message || 'Error al registrar la categoría', 'Cerrar', {
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
