import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CategoriaRequest,
  CategoriaResponse,
  EstadoVehiculo,
  Page,
  VehiculoCreateRequest,
  VehiculoResponse,
  VehiculoUpdateRequest,
} from '../../models/vehiculo.model';

@Injectable({
  providedIn: 'root',
})
export class VehiculoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8081/api/v1/vehiculos';

  // ==========================================
  // --- CATEGORÍAS ---
  // ==========================================

  crearCategoria(request: CategoriaRequest): Observable<CategoriaResponse> {
    return this.http.post<CategoriaResponse>(`${this.apiUrl}/categorias`, request);
  }

  listarCategorias(): Observable<CategoriaResponse[]> {
    return this.http.get<CategoriaResponse[]>(`${this.apiUrl}/categorias`);
  }

  // ==========================================
  // --- VEHÍCULOS (Lectura / Creación) ---
  // ==========================================

  crearVehiculo(request: VehiculoCreateRequest): Observable<VehiculoResponse> {
    return this.http.post<VehiculoResponse>(this.apiUrl, request);
  }

  obtenerPorId(id: number): Observable<VehiculoResponse> {
    return this.http.get<VehiculoResponse>(`${this.apiUrl}/${id}`);
  }

  obtenerPorPlaca(placa: string): Observable<VehiculoResponse> {
    return this.http.get<VehiculoResponse>(`${this.apiUrl}/placa/${placa}`);
  }

  buscarConFiltros(
    filtros: {
      idSucursal?: number;
      estado?: EstadoVehiculo;
      idCategoria?: number;
      marca?: string;
      query?: string;
    } = {},
    page: number = 0,
    size: number = 10,
    sort: string = 'id,asc',
  ): Observable<Page<VehiculoResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    if (filtros.idSucursal !== undefined && filtros.idSucursal !== null) {
      params = params.set('idSucursal', filtros.idSucursal.toString());
    }
    if (filtros.estado) {
      params = params.set('estado', filtros.estado);
    }
    if (filtros.idCategoria !== undefined && filtros.idCategoria !== null) {
      params = params.set('idCategoria', filtros.idCategoria.toString());
    }
    if (filtros.marca && filtros.marca.trim() !== '') {
      params = params.set('marca', filtros.marca.trim());
    }
    if (filtros.query && filtros.query.trim() !== '') {
      params = params.set('query', filtros.query.trim()); // 👈 Lo agregas a las query params
    }

    return this.http.get<Page<VehiculoResponse>>(this.apiUrl, { params });
  }

  listarDisponibles(idSucursal: number): Observable<VehiculoResponse[]> {
    return this.http.get<VehiculoResponse[]>(`${this.apiUrl}/disponibles/sucursal/${idSucursal}`);
  }

  // ==========================================
  // --- ACTUALIZACIONES Y ACCIONES ---
  // ==========================================

  actualizarVehiculo(id: number, request: VehiculoUpdateRequest): Observable<VehiculoResponse> {
    return this.http.put<VehiculoResponse>(`${this.apiUrl}/${id}`, request);
  }

  cambiarEstado(id: number, nuevoEstado: EstadoVehiculo): Observable<void> {
    const params = new HttpParams().set('nuevoEstado', nuevoEstado);
    return this.http.patch<void>(`${this.apiUrl}/${id}/estado`, null, { params });
  }

  trasladarSucursal(id: number, idNuevaSucursal: number): Observable<void> {
    const params = new HttpParams().set('idNuevaSucursal', idNuevaSucursal.toString());
    return this.http.patch<void>(`${this.apiUrl}/${id}/trasladar`, null, { params });
  }

  actualizarKilometraje(id: number, nuevoKilometraje: number): Observable<void> {
    const params = new HttpParams().set('nuevoKilometraje', nuevoKilometraje.toString());
    return this.http.patch<void>(`${this.apiUrl}/${id}/kilometraje`, null, { params });
  }
}
