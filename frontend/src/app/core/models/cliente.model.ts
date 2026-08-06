export interface ClienteRequest {
  nombre: string;
  apellido: string;
  email: string;
  telefono: string;
  numLicencia: string;
  vencimientoLicencia: string; // Formato 'YYYY-MM-DD'
}

export interface ClienteResponse {
  id: number;
  nombreCompleto: string;
  email: string;
  telefono: string;
  numLicencia: string;
  vencimientoLicencia: string; // Formato 'YYYY-MM-DD'
  activo: boolean;
}

// Estructura de respuesta paginada de Spring Data
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
