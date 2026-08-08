import { Component, OnInit, inject, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Observable, map, startWith } from 'rxjs';

// Angular Material Modules
import { MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatMenuModule } from '@angular/material/menu';
import { MatAutocompleteModule } from '@angular/material/autocomplete';

// Servicios y Modelos
import { VehiculoService } from '../../../../core/services/vehiculos/vehiculo.service';
import { EstadoVehiculo, VehiculoResponse } from '../../../../core/models/vehiculo.model';
import { VehiculoDialogComponent } from '../vehiculo-dialog/vehiculo-dialog.component';
import { SucursalService } from '../../../../core/services/sucursales/sucursal.service';
import { SucursalResponse } from '../../../../core/models/sucursal.model';

@Component({
  selector: 'app-vehiculo-list',
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
    MatChipsModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatMenuModule,
    MatAutocompleteModule,
  ],
  templateUrl: './vehiculo-list.component.html',
})
export class VehiculoListComponent implements OnInit {
  private readonly vehiculoService = inject(VehiculoService);
  private readonly fb = inject(FormBuilder);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly sucursalService = inject(SucursalService);

  // Formulario de Filtros
  filterForm!: FormGroup;

  // Tabla
  displayedColumns: string[] = [
    'id',
    'vehiculo',
    'placa',
    'vin',
    'sucursalNombre',
    'categoriaNombre',
    'kilometraje',
    'estado',
    'acciones',
  ];
  vehiculos: VehiculoResponse[] = [];

  // Paginación y Carga
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  isLoading = false;

  // Catálogos auxiliares para filtros
  estadosList: EstadoVehiculo[] = ['DISPONIBLE', 'RENTADO', 'MANTENIMIENTO', 'FUERA_SERVICIO'];
  sucursales: SucursalResponse[] = [];
  sucursalesFiltradas$!: Observable<SucursalResponse[]>;

  ngOnInit(): void {
    this.initFilterForm();
    this.cargarSucursales();
    this.cargarVehiculos();
  }

  private initFilterForm(): void {
    this.filterForm = this.fb.group({
      sucursalInput: [null],
      idSucursal: [null],
      estado: [null],
      marca: [''],
    });

    // Escuchar tipeo en sucursalInput para filtrar sugerencias y sincronizar el idSucursal
    this.sucursalesFiltradas$ = this.filterForm.get('sucursalInput')!.valueChanges.pipe(
      startWith(''),
      map((val) => {
        if (typeof val === 'string') {
          // Si es texto libre, limpiamos el ID del filtro si no hay coincidencia exacta
          this.filterForm.patchValue({ idSucursal: null }, { emitEvent: false });
          return this._filtrarSucursales(val);
        } else if (val && typeof val === 'object') {
          // Si seleccionó un objeto de la lista, asignamos su ID
          this.filterForm.patchValue({ idSucursal: val.id }, { emitEvent: false });
          return this._filtrarSucursales(val.nombre);
        }
        return this.sucursales;
      }),
    );
  }

  private _filtrarSucursales(valor: string): SucursalResponse[] {
    const filterValue = (valor || '').toLowerCase();
    return this.sucursales.filter((sucursal) =>
      sucursal.nombre.toLowerCase().includes(filterValue),
    );
  }

  // Función para renderizar el texto en el input cuando se selecciona una opción
  displaySucursalFn(sucursal: SucursalResponse | null): string {
    return sucursal && sucursal.nombre ? sucursal.nombre : '';
  }

  cargarSucursales(): void {
    // Solicitamos un tamaño amplio (100) para traer las sucursales requeridas para el select
    this.sucursalService.listarTodas(0, 500).subscribe({
      next: (page) => {
        this.sucursales = page.content;
      },
      error: (err) => {
        console.error('Error al cargar la lista de sucursales para el filtro:', err);
      },
    });
  }

  abrirDialogoCrear(): void {
    const dialogRef = this.dialog.open(VehiculoDialogComponent, {
      width: '600px', // O el ancho que prefieras para tu formulario
      disableClose: true, // Previene que se cierre al hacer clic afuera accidentalmente
    });

    // Se ejecuta al cerrar el modal
    dialogRef.afterClosed().subscribe((resultado) => {
      // Si el diálogo retornó true (indicando que se guardó exitosamente)
      if (resultado) {
        this.cargarVehiculos(); // Recarga la tabla de vehículos
      }
    });
  }

  // Método opcional para abrir en modo edición desde las acciones de la tabla
  abrirDialogoEditar(vehiculo: any): void {
    const dialogRef = this.dialog.open(VehiculoDialogComponent, {
      width: '600px',
      data: { vehiculo }, // Pasa la información del vehículo al modal
    });

    dialogRef.afterClosed().subscribe((resultado) => {
      if (resultado) {
        this.cargarVehiculos();
      }
    });
  }

  // ==========================================
  // MÉTODO PARA CAMBIAR EL ESTADO DEL VEHÍCULO
  // ==========================================
  cambiarEstado(vehiculo: VehiculoResponse, nuevoEstado: EstadoVehiculo): void {
    if (vehiculo.estado === nuevoEstado) return;

    this.isLoading = true;
    this.vehiculoService.cambiarEstado(vehiculo.id, nuevoEstado).subscribe({
      next: () => {
        this.snackBar.open(
          `Estado de ${vehiculo.marca} ${vehiculo.modelo} cambiado a ${nuevoEstado}`,
          'Cerrar',
          { duration: 3000 },
        );
        this.cargarVehiculos();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error al cambiar el estado del vehículo:', err);
        this.snackBar.open(err.error?.message || 'Error al cambiar el estado', 'Cerrar', {
          duration: 4000,
        });
      },
    });
  }

  cargarVehiculos(): void {
    this.isLoading = true;
    const { idSucursal, estado, marca } = this.filterForm.value;

    this.vehiculoService
      .buscarConFiltros({ idSucursal, estado, marca }, this.pageIndex, this.pageSize)
      .subscribe({
        next: (page) => {
          this.vehiculos = page.content;
          this.totalElements = page.totalElements;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error al cargar la flota de vehículos:', err);
          this.isLoading = false;
        },
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.cargarVehiculos();
  }

  limpiarFiltros(): void {
    this.filterForm.reset();
    this.pageIndex = 0;
    this.cargarVehiculos();
  }

  aplicarFiltros(): void {
    this.pageIndex = 0;
    this.cargarVehiculos();
  }

  // Clase para dar estilo de badge al estado del auto
  getEstadoBadgeClass(estado: EstadoVehiculo): string {
    switch (estado) {
      case 'DISPONIBLE':
        return 'badge-disponible';
      case 'RENTADO':
        return 'badge-rentado';
      case 'MANTENIMIENTO':
        return 'badge-mantenimiento';
      case 'FUERA_SERVICIO':
        return 'badge-fuera-servicio';
      default:
        return '';
    }
  }
}
