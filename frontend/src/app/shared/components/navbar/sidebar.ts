import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DOCUMENT } from '@angular/common';
import { RouterModule } from '@angular/router';

// Angular Material Modules
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatButtonModule } from '@angular/material/button';

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
    MatButtonModule,
  ],
  templateUrl: './sidebar.html',
})
export class SidebarComponent implements OnInit {
  private readonly document = inject(DOCUMENT);

  isDarkMode = false;

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

  ngOnInit(): void {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
      this.isDarkMode = true;
      this.document.documentElement.classList.add('dark');
      this.document.body.classList.add('dark');
    }
  }

  toggleTheme(): void {
    this.isDarkMode = !this.isDarkMode;
    if (this.isDarkMode) {
      this.document.documentElement.classList.add('dark');
      this.document.body.classList.add('dark');
      localStorage.setItem('theme', 'dark');
    } else {
      this.document.documentElement.classList.remove('dark');
      this.document.body.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    }
  }
}
