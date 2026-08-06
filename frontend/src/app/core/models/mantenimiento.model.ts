export interface MantenimientoRequest {
  idVehiculo: number;
  tipo: string;
  costo: number;
  descripcion?: string;
  fechaMantenimiento: string; // Formato YYYY-MM-DD
}

export interface MantenimientoResponse {
  id: number;
  idVehiculo: number;
  placaVehiculo: string;
  vehiculoModeloInfo: string;
  tipo: string;
  costo: number;
  descripcion?: string;
  fechaMantenimiento: string; // Formato YYYY-MM-DD
  activo: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // Página actual
  first: boolean;
  last: boolean;
  empty: boolean;
}
