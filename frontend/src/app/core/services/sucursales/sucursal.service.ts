import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse, SucursalRequest, SucursalResponse } from '../../models/sucursal.model';

@Injectable({
  providedIn: 'root',
})
export class SucursalService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8081/api/v1/sucursales';

  /**
   * Crea una nueva sucursal.
   * POST /api/v1/sucursales
   */
  crearSucursal(request: SucursalRequest): Observable<SucursalResponse> {
    return this.http.post<SucursalResponse>(this.apiUrl, request);
  }

  /**
   * Obtiene una sucursal por su identificador único.
   * GET /api/v1/sucursales/{id}
   */
  obtenerPorId(id: number): Observable<SucursalResponse> {
    return this.http.get<SucursalResponse>(`${this.apiUrl}/${id}`);
  }

  /**
   * Lista todas las sucursales con soporte de paginación.
   * GET /api/v1/sucursales?page={page}&size={size}&sort={sort}
   */
  listarTodas(
    page: number = 0,
    size: number = 10,
    sort: string = 'nombre',
  ): Observable<PageResponse<SucursalResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    return this.http.get<PageResponse<SucursalResponse>>(this.apiUrl, { params });
  }

  /**
   * Busca sucursales filtrando parcialmente por nombre con paginación.
   * GET /api/v1/sucursales/buscar?nombre={nombre}&page={page}&size={size}&sort={sort}
   */
  buscarPorNombre(
    nombre: string,
    page: number = 0,
    size: number = 10,
    sort: string = 'nombre',
  ): Observable<PageResponse<SucursalResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    if (nombre) {
      params = params.set('nombre', nombre);
    }

    return this.http.get<PageResponse<SucursalResponse>>(`${this.apiUrl}/buscar`, { params });
  }

  /**
   * Actualiza los datos de una sucursal existente.
   * PUT /api/v1/sucursales/{id}
   */
  actualizarSucursal(id: number, request: SucursalRequest): Observable<SucursalResponse> {
    return this.http.put<SucursalResponse>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Elimina una sucursal por su identificador único.
   * DELETE /api/v1/sucursales/{id}
   */
  eliminarSucursal(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
