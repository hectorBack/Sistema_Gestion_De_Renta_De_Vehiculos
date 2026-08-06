export interface SucursalRequest {
  nombre: string;
  direccion: string;
  telefono?: string;
  email?: string;
}

export interface SucursalResponse {
  id: number;
  nombre: string;
  direccion: string;
  telefono: string;
  email: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
