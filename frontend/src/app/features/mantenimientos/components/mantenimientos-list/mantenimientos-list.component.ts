import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';

// Angular Material Modules
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatMenuModule } from '@angular/material/menu';

// Servicios y Modelos
import { MantenimientoService } from '../../../../core/services/mantenimientos/mantenimiento.service';
import { MantenimientoResponse } from '../../../../core/models/mantenimiento.model';
import { MantenimientoDialogComponent } from '../../../mantenimientos/components/mantenimientos-dialog/mantenimientos-dialog.component';

@Component({
  selector: 'app-mantenimientos-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatMenuModule,
  ],
  templateUrl: './mantenimientos-list.component.html',
  styleUrl: './mantenimientos-list.component.scss',
})
export class MantenimientosListComponent implements OnInit {
  private readonly mantenimientoService = inject(MantenimientoService);
  private readonly fb = inject(FormBuilder);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  // Formulario de Filtros
  filterForm!: FormGroup;

  // Tabla
  displayedColumns: string[] = [
    'id',
    'vehiculoModeloInfo',
    'placaVehiculo',
    'tipo',
    'costo',
    'fechaMantenimiento',
    'descripcion',
    'activo',
    'acciones',
  ];
  mantenimientos: MantenimientoResponse[] = [];

  // Paginación y Carga
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  isLoading = false;

  // Catálogo para el filtro de estado
  estadosList = [
    { label: 'Todos', value: null },
    { label: 'Activo', value: true },
    { label: 'Inactivo', value: false },
  ];

  ngOnInit(): void {
    this.initFilterForm();
    this.cargarMantenimientos();
  }

  private initFilterForm(): void {
    this.filterForm = this.fb.group({
      termino: [''],
      activo: [true],
    });
  }

  abrirDialogoCrear(): void {
    const dialogRef = this.dialog.open(MantenimientoDialogComponent, {
      width: '600px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((resultado) => {
      if (resultado) {
        this.cargarMantenimientos();
      }
    });
  }

  abrirDialogoEditar(mantenimiento: MantenimientoResponse): void {
    const dialogRef = this.dialog.open(MantenimientoDialogComponent, {
      width: '600px',
      data: { mantenimiento },
    });

    dialogRef.afterClosed().subscribe((resultado) => {
      if (resultado) {
        this.cargarMantenimientos();
      }
    });
  }

  eliminarMantenimiento(mantenimiento: MantenimientoResponse): void {
    const confirmacion = confirm(
      `¿Está seguro de eliminar el registro de mantenimiento #${mantenimiento.id} del vehículo ${mantenimiento.placaVehiculo}?`,
    );

    if (!confirmacion) return;

    this.isLoading = true;
    this.mantenimientoService.eliminarMantenimiento(mantenimiento.id).subscribe({
      next: () => {
        this.snackBar.open(
          `Mantenimiento #${mantenimiento.id} eliminado correctamente.`,
          'Cerrar',
          { duration: 3000 },
        );
        this.cargarMantenimientos();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error al eliminar el mantenimiento:', err);
        this.snackBar.open(
          err.error?.message || 'Error al eliminar el registro de mantenimiento',
          'Cerrar',
          { duration: 4000 },
        );
      },
    });
  }

  cargarMantenimientos(): void {
    this.isLoading = true;
    const { termino, activo } = this.filterForm.value;

    this.mantenimientoService
      .listarMantenimientosPaginados(this.pageIndex, this.pageSize, termino, null, activo)
      .subscribe({
        next: (page) => {
          this.mantenimientos = page.content;
          this.totalElements = page.totalElements;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error al cargar mantenimientos:', err);
          this.isLoading = false;
        },
      });
  }

  cambiarEstado(mantenimiento: MantenimientoResponse, nuevoEstado: boolean): void {
    if (mantenimiento.activo === nuevoEstado) return;

    this.isLoading = true;
    this.mantenimientoService.cambiarEstadoMantenimiento(mantenimiento.id, nuevoEstado).subscribe({
      next: (res) => {
        const estadoTexto = res.activo ? 'activado' : 'inhabilitado';
        this.snackBar.open(
          `El mantenimiento  del vehiculo ${res.vehiculoModeloInfo} ha sido ${estadoTexto}.`,
          'Cerrar',
          {
            duration: 3000,
          },
        );
        this.cargarMantenimientos();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error al cambiar el estado del mantenimiento:', err);
        this.snackBar.open(
          err.error?.message || 'Error al cambiar el estado del mantenimiento',
          'Cerrar',
          { duration: 4000 },
        );
      },
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.cargarMantenimientos();
  }

  limpiarFiltros(): void {
    this.filterForm.reset({
      termino: '',
      activo: null,
    });
    this.pageIndex = 0;
    this.cargarMantenimientos();
  }

  aplicarFiltros(): void {
    this.pageIndex = 0;
    this.cargarMantenimientos();
  }
}
