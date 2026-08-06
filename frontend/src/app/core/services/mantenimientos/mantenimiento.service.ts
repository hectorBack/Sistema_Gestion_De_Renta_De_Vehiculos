import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  MantenimientoRequest,
  MantenimientoResponse,
  PageResponse,
} from '../../models/mantenimiento.model';

@Injectable({
  providedIn: 'root',
})
export class MantenimientoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8081/api/v1/mantenimientos';

  /**
   * POST /api/v1/mantenimientos
   * Inscribe un nuevo mantenimiento.
   */
  registrarMantenimiento(mantenimiento: MantenimientoRequest): Observable<MantenimientoResponse> {
    return this.http.post<MantenimientoResponse>(this.apiUrl, mantenimiento);
  }

  /**
   * GET /api/v1/mantenimientos/{id}
   * Consulta la información de un mantenimiento por ID.
   */
  obtenerPorId(id: number): Observable<MantenimientoResponse> {
    return this.http.get<MantenimientoResponse>(`${this.apiUrl}/${id}`);
  }

  /**
   * GET /api/v1/mantenimientos/paginados
   * Retorna la lista completa de mantenimientos con paginación básica.
   */
  listarMantenimientosPaginados(
    page: number = 0,
    size: number = 10,
    termino?: string,
    idVehiculo?: number | null,
    activo?: boolean | null,
    sortField: string = 'id',
    sortDirection: string = 'DESC',
  ): Observable<PageResponse<MantenimientoResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sortField},${sortDirection.toLowerCase()}`);

    if (termino && termino.trim() !== '') {
      params = params.set('termino', termino.trim());
    }

    if (idVehiculo !== null && idVehiculo !== undefined) {
      params = params.set('idVehiculo', idVehiculo.toString());
    }

    if (activo !== null && activo !== undefined) {
      params = params.set('activo', activo.toString());
    }

    return this.http.get<PageResponse<MantenimientoResponse>>(`${this.apiUrl}/paginados`, {
      params,
    });
  }

  /**
   * PUT /api/v1/mantenimientos/{id}
   * Actualiza los datos de un mantenimiento existente.
   */
  actualizarMantenimiento(
    id: number,
    mantenimiento: MantenimientoRequest,
  ): Observable<MantenimientoResponse> {
    return this.http.put<MantenimientoResponse>(`${this.apiUrl}/${id}`, mantenimiento);
  }

  /**
   * PATCH /api/v1/mantenimientos/{id}/estado?activo={boolean}
   * Activa o inhabilita un mantenimiento (Bloqueo administrativo).
   */
  cambiarEstadoMantenimiento(id: number, activo: boolean): Observable<MantenimientoResponse> {
    const params = new HttpParams().set('activo', activo);
    return this.http.patch<MantenimientoResponse>(`${this.apiUrl}/${id}/estado`, null, { params });
  }

  /**
   * DELETE /api/v1/vehiculos/mantenimientos/{id}
   * Realiza el borrado lógico del mantenimiento.
   */
  eliminarMantenimiento(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
