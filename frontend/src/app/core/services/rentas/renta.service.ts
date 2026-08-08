import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  EstadoRenta,
  PageResponse,
  RentaCreateRequest,
  RentaDevolucionRequest,
  RentaResponse,
  RentaResumenResponse,
} from '../../models/renta.model';

@Injectable({
  providedIn: 'root',
})
export class RentaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8081/api/v1/rentas';

  /**
   * Registra una nueva renta en el sistema.
   * POST /api/v1/rentas
   */
  crearRenta(request: RentaCreateRequest): Observable<RentaResponse> {
    return this.http.post<RentaResponse>(`${this.apiUrl}/reserva`, request);
  }

  /**
   * Obtiene la información detallada de una renta por su ID.
   * GET /api/v1/rentas/{id}
   */
  obtenerPorId(id: number): Observable<RentaResponse> {
    return this.http.get<RentaResponse>(`${this.apiUrl}/${id}`);
  }

  /**
   * Lista todas las rentas con soporte de paginación.
   * GET /api/v1/rentas?page={page}&size={size}&sort={sort}
   */
  listarTodas(
    page: number = 0,
    size: number = 10,
    sort: string = 'id,desc',
  ): Observable<PageResponse<RentaResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    return this.http.get<PageResponse<RentaResponse>>(this.apiUrl, { params });
  }

  /**
   * Busca rentas aplicando filtros opcionales (estado, cliente, fechaInicio, fechaFin) con paginación.
   * GET /api/v1/rentas/buscar?estado={estado}&idCliente={idCliente}&fechaInicio={fechaInicio}&fechaFin={fechaFin}&page={page}&size={size}
   */
  buscarConFiltros(
    filtros: {
      estado?: EstadoRenta | null;
      idCliente?: number | null;
      fecha?: Date | string | null;
    },
    page: number = 0,
    size: number = 10,
    sort: string = 'id,desc',
  ): Observable<PageResponse<RentaResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    if (filtros.estado) {
      params = params.set('estado', filtros.estado);
    }
    if (filtros.idCliente) {
      params = params.set('idCliente', filtros.idCliente.toString());
    }
    if (filtros.fecha) {
      let fechaFormatted: string;
      if (filtros.fecha instanceof Date) {
        // Formato YYYY-MM-DD para compatibilidad con LocalDate en Spring
        const year = filtros.fecha.getFullYear();
        const month = String(filtros.fecha.getMonth() + 1).padStart(2, '0');
        const day = String(filtros.fecha.getDate()).padStart(2, '0');
        fechaFormatted = `${year}-${month}-${day}`;
      } else {
        fechaFormatted = filtros.fecha;
      }
      params = params.set('fecha', fechaFormatted);
    }

    return this.http.get<PageResponse<RentaResponse>>(`${this.apiUrl}/buscar`, { params });
  }

  /**
   * Procesa la devolución del vehículo de una renta activa.
   * PUT /api/v1/rentas/{id}/devolucion
   */
  registrarDevolucion(id: number, request: RentaDevolucionRequest): Observable<RentaResponse> {
    return this.http.put<RentaResponse>(`${this.apiUrl}/${id}/devolucion`, request);
  }

  /**
   * Cancela una renta activa o reservada.
   * PATCH /api/v1/rentas/{id}/cancelar
   */
  cancelarRenta(id: number): Observable<RentaResponse> {
    return this.http.patch<RentaResponse>(`${this.apiUrl}/${id}/cancelar`, {});
  }

  /**
   * Inicia una renta reservada (Entrega de llaves -> Estado cambia a ACTIVA).
   * PATCH /api/v1/rentas/{id}/iniciar
   */
  iniciarRenta(id: number): Observable<RentaResponse> {
    return this.http.patch<RentaResponse>(`${this.apiUrl}/${id}/iniciar`, {});
  }

  /**
   * Obtiene las métricas globales y del día para las tarjetas KPI
   */
  obtenerResumenDashboard(): Observable<RentaResumenResponse> {
    return this.http.get<RentaResumenResponse>(`${this.apiUrl}/resumen`);
  }
}
