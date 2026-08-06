// Enum que representa los estados posibles de una renta en la API
export type EstadoRenta = 'ACTIVA' | 'COMPLETADA' | 'CANCELADA' | 'RESERVADA';

// DTO para registrar/crear una nueva renta
export interface RentaCreateRequest {
  idVehiculo: number;
  idCliente: number;
  idSucursalRetiro: number;
  idSucursalDevolucion: number;
  fechaInicio: string; // Formato LocalDateTime ISO string ('YYYY-MM-DDTHH:mm:ss')
  fechaFinEstimada: string; // Formato LocalDateTime ISO string ('YYYY-MM-DDTHH:mm:ss')
  kilometrajeInicial: number;
}

// DTO para procesar la devolución de un vehículo
export interface RentaDevolucionRequest {
  fechaDevolucionReal: string; // Formato LocalDateTime ISO string ('YYYY-MM-DDTHH:mm:ss')
  kilometrajeFinal: number;
}

// DTO de respuesta detallada de la renta
export interface RentaResponse {
  id: number;
  vehiculoDetalle: string;
  clienteNombre: string;
  sucursalRetiro: string;
  sucursalDevolucion: string;
  fechaInicio: string;
  fechaFinEstimada: string;
  fechaDevolucionReal?: string;
  kilometrajeInicial: number;
  kilometrajeFinal?: number;
  costoTotal: number;
  estado: EstadoRenta;
}

// Interfaz reutilizable para respuestas paginadas de Spring Boot (Page<RentaResponse>)
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
