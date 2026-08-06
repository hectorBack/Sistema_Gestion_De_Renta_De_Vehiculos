import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

// Angular Material Modules
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';

interface MenuItem {
  label: string;
  icon: string;
  route: string;
  badge?: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatListModule,
    MatIconModule,
    MatDividerModule,
    MatTooltipModule,
  ],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class SidebarComponent {
  // Ítems de navegación principal
  mainNavItems: MenuItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
    { label: 'Vehículos', icon: 'directions_car', route: '/vehiculos' },
    { label: 'Sucursales', icon: 'storefront', route: '/sucursales' },
    { label: 'Rentas', icon: 'key', route: '/rentas' },
    { label: 'Clientes', icon: 'people', route: '/clientes' },
  ];

  // Ítems secundarios o de configuración
  secondaryNavItems: MenuItem[] = [
    { label: 'Categorías', icon: 'category', route: '/categorias' },
    { label: 'Mantenimientos', icon: 'build', route: '/mantenimientos' },
    { label: 'Cotizaciones', icon: 'request_quote', route: '/cotizaciones' },
  ];
}
