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
import { MatSelectModule } from '@angular/material/select';
import { MatMenuModule } from '@angular/material/menu';

// Servicios y Modelos
import { CategoriaService } from '../../../../core/services/categorias/categoria.service';
import { CategoriaResponse } from '../../../../core/models/categoria.model';
import { CategoriaDialogComponent } from '../categorias-dialog/categorias-dialog.component';

@Component({
  selector: 'app-categorias-list',
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
    MatSelectModule,
    MatMenuModule,
  ],
  templateUrl: './categorias-list.component.html',
})
export class CategoriasListComponent implements OnInit {
  private readonly categoriaService = inject(CategoriaService);
  private readonly fb = inject(FormBuilder);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  // Formulario de Filtros
  filterForm!: FormGroup;

  // Tabla
  displayedColumns: string[] = [
    'id',
    'nombre',
    'descripcion',
    'tarifaDiariaBase',
    'depositoGarantia',
    'activo',
    'acciones',
  ];
  categorias: CategoriaResponse[] = [];

  // Catálogo para el filtro de estado 👈 AGREGADO
  estadosList = [
    { label: 'Todos', value: null },
    { label: 'Activo', value: true },
    { label: 'Inactivo', value: false },
  ];

  // Paginación y Carga
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  isLoading = false;

  ngOnInit(): void {
    this.initFilterForm();
    this.cargarCategorias();
  }

  private initFilterForm(): void {
    this.filterForm = this.fb.group({
      termino: [''],
      activo: [true],
    });
  }

  abrirDialogoCrear(): void {
    const dialogRef = this.dialog.open(CategoriaDialogComponent, {
      width: '600px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((resultado) => {
      if (resultado) {
        this.cargarCategorias();
      }
    });
  }

  abrirDialogoEditar(categoria: CategoriaResponse): void {
    const dialogRef = this.dialog.open(CategoriaDialogComponent, {
      width: '600px',
      data: { categoria },
    });

    dialogRef.afterClosed().subscribe((resultado) => {
      if (resultado) {
        this.cargarCategorias();
      }
    });
  }

  cambiarEstado(categoria: CategoriaResponse, nuevoEstado: boolean): void {
    if (categoria.activo === nuevoEstado) return;

    this.isLoading = true;
    this.categoriaService.cambiarEstadoCategoria(categoria.id, nuevoEstado).subscribe({
      next: (res) => {
        const estadoTexto = res.activo ? 'activado' : 'Inactivo';
        this.snackBar.open(`La categoria ${res.nombre} ha sido ${estadoTexto}.`, 'Cerrar', {
          duration: 3000,
        });
        this.cargarCategorias();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error al cambiar el estado de la categoria:', err);
        this.snackBar.open(
          err.error?.message || 'Error al cambiar el estado de la categoria',
          'Cerrar',
          { duration: 4000 },
        );
      },
    });
  }

  cargarCategorias(): void {
    this.isLoading = true;
    const { termino, activo } = this.filterForm.value;

    this.categoriaService
      .listarCategoriasPaginadas(this.pageIndex, this.pageSize, termino, activo)
      .subscribe({
        next: (page) => {
          this.categorias = page.content;
          this.totalElements = page.totalElements;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error al cargar categorías:', err);
          this.isLoading = false;
          this.snackBar.open('Error al obtener la lista de categorías', 'Cerrar', {
            duration: 4000,
          });
        },
      });
  }

  eliminarCategoria(categoria: CategoriaResponse): void {
    const confirmacion = confirm(
      `¿Estás seguro de que deseas eliminar la categoría "${categoria.nombre}"?`,
    );

    if (confirmacion) {
      this.isLoading = true;
      this.categoriaService.eliminarCategoria(categoria.id).subscribe({
        next: () => {
          this.snackBar.open('Categoría eliminada correctamente', 'Cerrar', {
            duration: 3000,
          });
          this.cargarCategorias();
        },
        error: (err) => {
          this.isLoading = false;
          console.error('Error al eliminar categoría:', err);
          this.snackBar.open(err.error?.message || 'No se pudo eliminar la categoría', 'Cerrar', {
            duration: 4000,
          });
        },
      });
    }
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.cargarCategorias();
  }

  limpiarFiltros(): void {
    this.filterForm.reset({
      termino: '',
      activo: true,
    });
    this.pageIndex = 0;
    this.cargarCategorias();
  }

  aplicarFiltros(): void {
    this.pageIndex = 0;
    this.cargarCategorias();
  }
}
