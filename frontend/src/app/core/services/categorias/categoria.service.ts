import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CategoriaRequest, CategoriaResponse, PageResponse } from '../../models/categoria.model';

@Injectable({
  providedIn: 'root',
})
export class CategoriaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8081/api/v1/vehiculos/categorias';

  /**
   * Obtiene la lista completa de categorías (sin paginar, para selects/dropdowns)
   */
  listarCategorias(): Observable<CategoriaResponse[]> {
    return this.http.get<CategoriaResponse[]>(this.apiUrl);
  }

  /**
   * Obtiene las categorías de forma paginada con filtro de búsqueda y ordenamiento
   */
  listarCategoriasPaginadas(
    page: number = 0,
    size: number = 10,
    termino?: string,
    activo?: boolean | null,
    sortField: string = 'id',
    sortDirection: string = 'DESC',
  ): Observable<PageResponse<CategoriaResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sortField},${sortDirection.toLowerCase()}`);

    if (termino && termino.trim() !== '') {
      params = params.set('termino', termino.trim());
    }

    // Se adjunta el parámetro de estado solo si no es nulo/indefinido
    if (activo !== null && activo !== undefined) {
      params = params.set('activo', activo.toString());
    }

    return this.http.get<PageResponse<CategoriaResponse>>(`${this.apiUrl}/paginadas`, { params });
  }

  /**
   * Registra una nueva categoría tarifaria
   */
  crearCategoria(categoria: CategoriaRequest): Observable<CategoriaResponse> {
    return this.http.post<CategoriaResponse>(this.apiUrl, categoria);
  }

  /**
   * PATCH /api/v1/vehiculos/categorias/{id}/estado?activo={boolean}
   * Activa o inhabilita una categoría (Bloqueo administrativo).
   */
  cambiarEstadoCategoria(id: number, activo: boolean): Observable<CategoriaResponse> {
    const params = new HttpParams().set('activo', activo);
    return this.http.patch<CategoriaResponse>(`${this.apiUrl}/${id}/estado`, null, { params });
  }

  /**
   * Actualiza una categoría existente mediante su ID
   */
  actualizarCategoria(id: number, categoria: CategoriaRequest): Observable<CategoriaResponse> {
    return this.http.put<CategoriaResponse>(`${this.apiUrl}/${id}`, categoria);
  }

  /**
   * Elimina una categoría (borrado lógico en backend) mediante su ID
   */
  eliminarCategoria(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
