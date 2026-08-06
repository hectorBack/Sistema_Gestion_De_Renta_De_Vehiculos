import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClienteRequest, ClienteResponse, PageResponse } from '../../models/cliente.model';

@Injectable({
  providedIn: 'root',
})
export class ClienteService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8081/api/v1/clientes';

  /**
   * POST /api/v1/clientes
   * Inscribe un nuevo cliente.
   */
  registrarCliente(cliente: ClienteRequest): Observable<ClienteResponse> {
    return this.http.post<ClienteResponse>(this.apiUrl, cliente);
  }

  /**
   * GET /api/v1/clientes/{id}
   * Consulta la información de un cliente por ID.
   */
  obtenerPorId(id: number): Observable<ClienteResponse> {
    return this.http.get<ClienteResponse>(`${this.apiUrl}/${id}`);
  }

  /**
   * GET /api/v1/clientes
   * Retorna la lista completa de clientes con paginación básica.
   */
  listarTodos(page = 0, size = 10, sort = 'nombre'): Observable<PageResponse<ClienteResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    return this.http.get<PageResponse<ClienteResponse>>(this.apiUrl, { params });
  }

  /**
   * PUT /api/v1/clientes/{id}
   * Actualiza los datos de un cliente existente.
   */
  actualizarCliente(id: number, cliente: ClienteRequest): Observable<ClienteResponse> {
    return this.http.put<ClienteResponse>(`${this.apiUrl}/${id}`, cliente);
  }

  /**
   * PATCH /api/v1/clientes/{id}/estado?activo={boolean}
   * Activa o inhabilita un cliente (Bloqueo administrativo).
   */
  cambiarEstadoCliente(id: number, activo: boolean): Observable<ClienteResponse> {
    const params = new HttpParams().set('activo', activo);
    return this.http.patch<ClienteResponse>(`${this.apiUrl}/${id}/estado`, null, { params });
  }

  /**
   * GET /api/v1/clientes/buscar?termino=...&activo=...&page=...&size=...
   * Busca clientes por término (nombre, apellido, email o licencia) y estado con paginación.
   */
  buscarConFiltros(
    termino?: string,
    activo?: boolean,
    page = 0,
    size = 10,
    sort = 'apellido',
  ): Observable<PageResponse<ClienteResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    if (termino && termino.trim() !== '') {
      params = params.set('termino', termino.trim());
    }

    if (activo !== undefined && activo !== null) {
      params = params.set('activo', activo);
    }

    return this.http.get<PageResponse<ClienteResponse>>(`${this.apiUrl}/buscar`, { params });
  }

  /**
   * GET /api/v1/clientes/licencia/{numLicencia}
   * Consulta cliente por número de licencia.
   */
  obtenerPorLicencia(numLicencia: string): Observable<ClienteResponse> {
    return this.http.get<ClienteResponse>(`${this.apiUrl}/licencia/${numLicencia}`);
  }
}
