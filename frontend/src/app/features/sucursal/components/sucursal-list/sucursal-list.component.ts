import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';

// Angular Material Modules
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

// Servicios y Modelos
import { SucursalService } from '../../../../core/services/sucursales/sucursal.service';
import { SucursalResponse } from '../../../../core/models/sucursal.model';
import { SucursalDialogComponent } from '../sucursal-dialog/sucursal-dialog.component';

@Component({
  selector: 'app-sucursal-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatTooltipModule,
    MatSnackBarModule,
  ],
  templateUrl: './sucursal-list.component.html',
  styleUrl: './sucursal-list.component.scss',
})
export class SucursalListComponent implements OnInit {
  private readonly sucursalService = inject(SucursalService);
  private readonly fb = inject(FormBuilder);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  // Formulario de Filtros
  filterForm!: FormGroup;

  // Tabla
  displayedColumns: string[] = ['id', 'nombre', 'direccion', 'telefono', 'email', 'acciones'];
  sucursales: SucursalResponse[] = [];

  // Paginación y Carga
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  isLoading = false;

  ngOnInit(): void {
    this.initFilterForm();
    this.cargarSucursales();
  }

  private initFilterForm(): void {
    this.filterForm = this.fb.group({
      nombre: [''],
    });
  }

  abrirDialogoCrear(): void {
    const dialogRef = this.dialog.open(SucursalDialogComponent, {
      width: '600px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((resultado) => {
      if (resultado) {
        this.cargarSucursales();
      }
    });
  }

  abrirDialogoEditar(sucursal: SucursalResponse): void {
    const dialogRef = this.dialog.open(SucursalDialogComponent, {
      width: '600px',
      data: { sucursal },
    });

    dialogRef.afterClosed().subscribe((resultado) => {
      if (resultado) {
        this.cargarSucursales();
      }
    });
  }

  eliminarSucursal(sucursal: SucursalResponse): void {
    const confirmacion = confirm(
      `¿Estás seguro de que deseas eliminar la sucursal "${sucursal.nombre}"?`,
    );

    if (!confirmacion) return;

    this.isLoading = true;
    this.sucursalService.eliminarSucursal(sucursal.id).subscribe({
      next: () => {
        this.snackBar.open(
          `La sucursal ${sucursal.nombre} ha sido eliminada con éxito.`,
          'Cerrar',
          { duration: 3000 },
        );
        this.cargarSucursales();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error al eliminar sucursal:', err);
        const errorMsg =
          err.error?.message ||
          'No se puede eliminar la sucursal porque tiene vehículos asignados o no existe.';
        this.snackBar.open(errorMsg, 'Cerrar', { duration: 4000 });
      },
    });
  }

  cargarSucursales(): void {
    this.isLoading = true;
    const { nombre } = this.filterForm.value;

    const query$ = nombre?.trim()
      ? this.sucursalService.buscarPorNombre(nombre.trim(), this.pageIndex, this.pageSize)
      : this.sucursalService.listarTodas(this.pageIndex, this.pageSize);

    query$.subscribe({
      next: (page) => {
        this.sucursales = page.content;
        this.totalElements = page.totalElements;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error al cargar sucursales:', err);
        this.isLoading = false;
        this.snackBar.open('Error al cargar la lista de sucursales', 'Cerrar', {
          duration: 3000,
        });
      },
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.cargarSucursales();
  }

  limpiarFiltros(): void {
    this.filterForm.reset({ nombre: '' });
    this.pageIndex = 0;
    this.cargarSucursales();
  }

  aplicarFiltros(): void {
    this.pageIndex = 0;
    this.cargarSucursales();
  }
}
