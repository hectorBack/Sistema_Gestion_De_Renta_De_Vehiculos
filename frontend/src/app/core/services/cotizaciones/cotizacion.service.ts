// src/app/core/services/cotizaciones/cotizacion.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CotizacionRequest, CotizacionResponse } from '../../models/cotizacion.model';

@Injectable({
  providedIn: 'root',
})
export class CotizacionService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8081/api/v1/cotizaciones';

  calcularCotizacion(request: CotizacionRequest): Observable<CotizacionResponse> {
    return this.http.post<CotizacionResponse>(`${this.apiUrl}/calcular`, request);
  }
}
