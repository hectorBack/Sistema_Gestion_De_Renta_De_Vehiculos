export type EstadoVehiculo = 'DISPONIBLE' | 'RENTADO' | 'MANTENIMIENTO' | 'FUERA_SERVICIO';

export interface CategoriaRequest {
  nombre: string;
  descripcion?: string;
  tarifaDiariaBase: number;
}

export interface CategoriaResponse {
  id: number;
  nombre: string;
  descripcion: string;
  tarifaDiariaBase: number;
}

export interface VehiculoCreateRequest {
  idCategoria: number;
  idSucursalActual: number;
  vin: string;
  placa: string;
  marca: string;
  modelo: string;
  anio: number;
  kilometraje: number;
}

export interface VehiculoUpdateRequest {
  idCategoria: number;
  placa: string;
  marca: string;
  modelo: string;
  anio: number;
}

export interface VehiculoResponse {
  id: number;
  categoriaNombre: string;
  sucursalNombre: string;
  vin: string;
  placa: string;
  marca: string;
  modelo: string;
  anio: number;
  kilometraje: number;
  estado: EstadoVehiculo;
}

// Estructura genérica para respuestas paginadas de Spring Data Pageable
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // Página actual (index 0)
  first: boolean;
  last: boolean;
  empty: boolean;
}
