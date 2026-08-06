// DTO de respuesta para una Categoría
export interface CategoriaResponse {
  id: number;
  nombre: string;
  descripcion?: string;
  tarifaDiariaBase: number;
  depositoGarantia: number;
  activo: boolean;
}

// DTO para la creación/edición de una Categoría
export interface CategoriaRequest {
  nombre: string;
  descripcion?: string;
  tarifaDiariaBase: number;
  depositoGarantia: number;
}

// Interfaz genérica para la respuesta paginada de Spring Data (Page<T>)
export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}
