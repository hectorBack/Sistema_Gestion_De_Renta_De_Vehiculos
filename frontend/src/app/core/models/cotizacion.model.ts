export interface CotizacionRequest {
  idCategoria?: number | null;
  idVehiculo?: number | null;
  fechaInicio: string; // ISO String
  fechaFin: string; // ISO String
  incluyeSeguro: boolean;
  conductorAdicional: boolean;
}

export interface CotizacionResponse {
  vehiculoOcategoriaInfo: string;
  diasTotales: number;
  horasTotales: number;
  tarifaDiariaBase: number;
  subtotalRenta: number;
  costoSeguro: number;
  costoConductorAdicional: number;
  subtotalGeneral: number;
  impuestos: number;
  totalEstimado: number;
  depositoGarantiaSugerido: number;
}
