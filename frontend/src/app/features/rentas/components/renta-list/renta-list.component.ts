import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Observable, map, startWith } from 'rxjs';

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
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatDatepickerModule } from '@angular/material/datepicker';

// Servicios y Modelos
import { RentaService } from '../../../../core/services/rentas/renta.service';
import {
  EstadoRenta,
  RentaResponse,
  RentaResumenResponse,
} from '../../../../core/models/renta.model';
import { ClienteService } from '../../../../core/services/clientes/cliente.service';
import { ClienteResponse } from '../../../../core/models/cliente.model';

// Diálogos del módulo de rentas (Ajusta la ruta según tus componentes finales)
import { RentaDialogComponent } from '../renta-dialog/renta-dialog.component';
import { DevolucionDialogComponent } from '../devolucion-dialog/devolucion-dialog.component';

@Component({
  selector: 'app-renta-list',
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
    MatAutocompleteModule,
    MatCardModule,
    MatDatepickerModule,
  ],
  templateUrl: './renta-list.component.html',
  styleUrl: './renta-list.component.scss',
})
export class RentaListComponent implements OnInit {
  private readonly rentaService = inject(RentaService);
  private readonly clienteService = inject(ClienteService);
  private readonly fb = inject(FormBuilder);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  // Estado del Resumen KPI
  resumen: RentaResumenResponse | null = null;
  isLoadingResumen = false;

  // Formulario de Filtros
  filterForm!: FormGroup;

  // Columnas para la tabla
  displayedColumns: string[] = [
    'id',
    'vehiculoDetalle',
    'clienteNombre',
    'sucursalRetiro',
    'sucursalDevolucion',
    'fechaInicio',
    'fechaFinEstimada',
    'costoTotal',
    'estado',
    'acciones',
  ];
  rentas: RentaResponse[] = [];

  // Catálogo de clientes para el select de filtro
  clientes: ClienteResponse[] = [];

  clientesFiltrados$!: Observable<ClienteResponse[]>;

  // Paginación y Carga
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  isLoading = false;

  // Catálogo para el filtro de estados
  estadosList: { label: string; value: EstadoRenta | null }[] = [
    { label: 'Todos', value: null },
    { label: 'Reservada', value: 'RESERVADA' },
    { label: 'Activa', value: 'ACTIVA' },
    { label: 'Completada', value: 'COMPLETADA' },
    { label: 'Cancelada', value: 'CANCELADA' },
  ];

  ngOnInit(): void {
    this.initFilterForm();
    this.cargarClientesFiltro();
    this.cargarRentas();
    this.cargarResumen();
  }

  private initFilterForm(): void {
    this.filterForm = this.fb.group({
      clienteInput: [null],
      estado: ['ACTIVA'],
      idCliente: [null],
      fecha: [null],
    });

    // Escuchar tipeo en sucursalInput para filtrar sugerencias y sincronizar el idSucursal
    this.clientesFiltrados$ = this.filterForm.get('clienteInput')!.valueChanges.pipe(
      startWith(''),
      map((val) => {
        if (typeof val === 'string') {
          // Si es texto libre, limpiamos el ID del filtro si no hay coincidencia exacta
          this.filterForm.patchValue({ idCliente: null }, { emitEvent: false });
          return this._filtrarClientes(val);
        } else if (val && typeof val === 'object') {
          // Si seleccionó un objeto de la lista, asignamos su ID
          this.filterForm.patchValue({ idCliente: val.id }, { emitEvent: false });
          return this._filtrarClientes(val.nombre);
        }
        return this.clientes;
      }),
    );
  }

  private _filtrarClientes(valor: string): ClienteResponse[] {
    const filterValue = (valor || '').toLowerCase();
    return this.clientes.filter((cliente) =>
      cliente.nombreCompleto.toLowerCase().includes(filterValue),
    );
  }

  // Función para renderizar el texto en el input cuando se selecciona una opción
  displayClienteFn(cliente: ClienteResponse | null): string {
    return cliente && cliente.nombreCompleto ? cliente.nombreCompleto : '';
  }

  /**
   * Carga clientes para poblar el dropdown de filtrado por cliente.
   */
  private cargarClientesFiltro(): void {
    this.clienteService.buscarConFiltros('', true, 0, 100).subscribe({
      next: (page) => {
        this.clientes = page.content;
      },
      error: (err) => {
        console.error('Error al cargar la lista de clientes:', err);
      },
    });
  }

  /**
   * Carga la información de las tarjetas KPI desde el backend
   */
  public cargarResumen(): void {
    this.isLoadingResumen = true;
    this.rentaService.obtenerResumenDashboard().subscribe({
      next: (data) => {
        this.resumen = data;
        this.isLoadingResumen = false;
      },
      error: (err) => {
        console.error('Error al obtener el resumen del dashboard:', err);
        this.isLoadingResumen = false;
        this.snackBar.open('No se pudieron cargar las métricas del día', 'Cerrar', {
          duration: 3000,
        });
      },
    });
  }

  /**
   * Carga el listado paginado de rentas aplicando los filtros configurados.
   */
  cargarRentas(): void {
    this.isLoading = true;
    const { estado, idCliente, fecha } = this.filterForm.value;

    this.rentaService
      .buscarConFiltros({ estado, idCliente, fecha }, this.pageIndex, this.pageSize)
      .subscribe({
        next: (page) => {
          this.rentas = page.content;
          this.totalElements = page.totalElements;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error al cargar la lista de rentas:', err);
          this.isLoading = false;
          this.snackBar.open('Error al cargar las rentas', 'Cerrar', { duration: 4000 });
        },
      });
  }

  /**
   * Ajusta automáticamente el filtro para mostrar únicamente las rentas del día de ayer.
   */
  filtrarAyer(): void {
    const ayer = new Date();
    ayer.setDate(ayer.getDate() - 1);

    this.filterForm.patchValue({
      estado: null, // Muestra todas las de ayer independientemente de su estado
      fecha: ayer,
    });

    this.aplicarFiltros();
  }

  /**
   * Abre el modal para registrar/crear una nueva renta.
   */
  abrirDialogoCrear(): void {
    const dialogRef = this.dialog.open(RentaDialogComponent, {
      width: '650px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((resultado) => {
      if (resultado) {
        this.cargarRentas();
        this.cargarResumen();
      }
    });
  }

  /**
   * Abre el modal para registrar la devolución del vehículo (para rentas en estado ACTIVA).
   */
  abrirDialogoDevolucion(renta: RentaResponse): void {
    const dialogRef = this.dialog.open(DevolucionDialogComponent, {
      width: '500px',
      disableClose: true,
      data: { renta },
    });

    dialogRef.afterClosed().subscribe((resultado) => {
      if (resultado) {
        this.cargarRentas();
      }
    });
  }

  iniciarRenta(renta: RentaResponse): void {
    if (confirm(`¿Deseas iniciar la renta #${renta.id} y entregar las llaves?`)) {
      this.rentaService.iniciarRenta(renta.id).subscribe({
        next: () => {
          this.snackBar.open('Renta iniciada correctamente (Estado: ACTIVA)', 'Cerrar', {
            duration: 3000,
          });
          this.cargarRentas(); // Recargar la tabla para ver el cambio de estado
        },
        error: (err) => {
          this.snackBar.open(err.error?.message || 'Error al iniciar la renta', 'Cerrar', {
            duration: 4000,
          });
        },
      });
    }
  }

  /**
   * Procesa la cancelación de una renta pendiente/activa.
   */
  cancelarRenta(renta: RentaResponse): void {
    if (confirm(`¿Está seguro de que desea cancelar la renta #${renta.id}?`)) {
      this.isLoading = true;
      this.rentaService.cancelarRenta(renta.id).subscribe({
        next: () => {
          this.snackBar.open(`Renta #${renta.id} cancelada correctamente.`, 'Cerrar', {
            duration: 3000,
          });
          this.cargarRentas();
        },
        error: (err) => {
          this.isLoading = false;
          console.error('Error al cancelar la renta:', err);
          this.snackBar.open(err.error?.message || 'Error al cancelar la renta', 'Cerrar', {
            duration: 4000,
          });
        },
      });
    }
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.cargarRentas();
  }

  limpiarFiltros(): void {
    this.filterForm.reset({
      clienteInput: null,
      estado: 'ACTIVA', // <-- Mantiene 'ACTIVA' como estado por defecto
      idCliente: null,
      fecha: null,
    });
    this.pageIndex = 0;
    this.cargarRentas();
  }

  aplicarFiltros(): void {
    this.pageIndex = 0;
    this.cargarRentas();
  }

  /**
   * Devuelve la clase CSS correspondiente para cada estado del enum
   */
  getEstadoBadgeClass(estado: EstadoRenta): string {
    switch (estado) {
      case 'RESERVADA':
        return 'badge-reservada';
      case 'ACTIVA':
        return 'badge-activa';
      case 'COMPLETADA':
        return 'badge-completada';
      case 'CANCELADA':
        return 'badge-cancelada';
      default:
        return '';
    }
  }
}
