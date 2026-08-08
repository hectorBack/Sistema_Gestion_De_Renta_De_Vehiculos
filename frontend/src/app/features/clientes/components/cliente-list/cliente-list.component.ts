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
import { ClienteService } from '../../../../core/services/clientes/cliente.service';
import { ClienteResponse } from '../../../../core/models/cliente.model';
import { ClienteDialogComponent } from '../cliente-dialog/cliente-dialog.component';

@Component({
  selector: 'app-cliente-list',
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
  templateUrl: './cliente-list.component.html',
})
export class ClienteListComponent implements OnInit {
  private readonly clienteService = inject(ClienteService);
  private readonly fb = inject(FormBuilder);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  // Formulario de Filtros
  filterForm!: FormGroup;

  // Tabla
  displayedColumns: string[] = [
    'id',
    'nombreCompleto',
    'email',
    'telefono',
    'numLicencia',
    'vencimientoLicencia',
    'activo',
    'acciones',
  ];
  clientes: ClienteResponse[] = [];

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
    this.cargarClientes();
  }

  private initFilterForm(): void {
    this.filterForm = this.fb.group({
      termino: [''],
      activo: [true],
    });
  }

  abrirDialogoCrear(): void {
    const dialogRef = this.dialog.open(ClienteDialogComponent, {
      width: '600px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((resultado) => {
      if (resultado) {
        this.cargarClientes();
      }
    });
  }

  abrirDialogoEditar(cliente: ClienteResponse): void {
    const dialogRef = this.dialog.open(ClienteDialogComponent, {
      width: '600px',
      data: { cliente },
    });

    dialogRef.afterClosed().subscribe((resultado) => {
      if (resultado) {
        this.cargarClientes();
      }
    });
  }

  cambiarEstado(cliente: ClienteResponse, nuevoEstado: boolean): void {
    if (cliente.activo === nuevoEstado) return;

    this.isLoading = true;
    this.clienteService.cambiarEstadoCliente(cliente.id, nuevoEstado).subscribe({
      next: (res) => {
        const estadoTexto = res.activo ? 'activado' : 'inhabilitado';
        this.snackBar.open(`El cliente ${res.nombreCompleto} ha sido ${estadoTexto}.`, 'Cerrar', {
          duration: 3000,
        });
        this.cargarClientes();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error al cambiar el estado del cliente:', err);
        this.snackBar.open(
          err.error?.message || 'Error al cambiar el estado del cliente',
          'Cerrar',
          { duration: 4000 },
        );
      },
    });
  }

  cargarClientes(): void {
    this.isLoading = true;
    const { termino, activo } = this.filterForm.value;

    this.clienteService.buscarConFiltros(termino, activo, this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.clientes = page.content;
        this.totalElements = page.totalElements;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error al cargar clientes:', err);
        this.isLoading = false;
      },
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.cargarClientes();
  }

  limpiarFiltros(): void {
    this.filterForm.reset();
    this.pageIndex = 0;
    this.cargarClientes();
  }

  aplicarFiltros(): void {
    this.pageIndex = 0;
    this.cargarClientes();
  }
}
