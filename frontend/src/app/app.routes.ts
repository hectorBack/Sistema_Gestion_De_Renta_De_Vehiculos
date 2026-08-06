import { Routes } from '@angular/router';
import { VehiculoListComponent } from './features/vehiculos/components/vehiculo-list/vehiculo-list.component';
import { ClienteListComponent } from './features/clientes/components/cliente-list/cliente-list.component';
import { SucursalListComponent } from './features/sucursal/components/sucursal-list/sucursal-list.component';
import { RentaListComponent } from './features/rentas/components/renta-list/renta-list.component';
import { DashboardListComponent } from './features/dashboard/components/dashboard-list.component';
import { CategoriasListComponent } from './features/categorias/components/categorias-list/categorias-list.component';
import { MantenimientosListComponent } from './features/mantenimientos/components/mantenimientos-list/mantenimientos-list.component';
import { CotizacionesListComponent } from './features/cotizaciones/cotizaciones-list/cotizaciones-list.component';

export const routes: Routes = [
  // Redirecciona la raíz '/' directamente a '/vehiculos'
  { path: '', redirectTo: 'vehiculos', pathMatch: 'full' },

  // Ruta directa a la lista de vehículos
  { path: 'vehiculos', component: VehiculoListComponent },
  { path: 'clientes', component: ClienteListComponent },
  { path: 'sucursales', component: SucursalListComponent },
  { path: 'rentas', component: RentaListComponent },
  { path: 'dashboard', component: DashboardListComponent },
  { path: 'categorias', component: CategoriasListComponent },
  { path: 'mantenimientos', component: MantenimientosListComponent },
  { path: 'cotizaciones', component: CotizacionesListComponent },

  // Ruta por defecto para URLs no encontradas
  { path: '**', redirectTo: 'vehiculos' },
];
