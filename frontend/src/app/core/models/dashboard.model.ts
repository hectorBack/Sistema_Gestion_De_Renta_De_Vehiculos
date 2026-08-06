export interface KpiMetrics {
  rentasActivas: number;
  ingresosMesActual: number;
  vehiculosDisponibles: number;
  vehiculosEnMantenimiento: number;
}

export interface OcupacionFlota {
  tasaOcupacionPorcentaje: number;
  totalFlota: number;
  disponibles: number;
  rentados: number;
  mantenimiento: number;
}

export interface IngresoMensual {
  anio: number;
  mes: number;
  total: number;
}

export interface VehiculoPopular {
  marca: string;
  modelo: string;
  placa: string;
  totalRentas: number;
}

export interface RentasPorSucursal {
  sucursal: string;
  totalRentas: number;
}

export interface Graficos {
  ingresosMensuales: IngresoMensual[];
  topVehiculosMasRentados: VehiculoPopular[];
  rentasPorSucursal: RentasPorSucursal[];
}

export interface RentaResumen {
  idRenta: number;
  clienteNombre: string;
  vehiculoInfo: string;
  fechaInicio: string; // ISO string enviado desde Java LocalDateTime
  estado: string;
}

export interface AlertasYActividad {
  devolucionesAtrasadasCount: number;
  ultimasRentas: RentaResumen[];
}

export interface DashboardMetrics {
  kpis: KpiMetrics;
  ocupacion: OcupacionFlota;
  graficos: Graficos;
  alertasYActividad: AlertasYActividad;
}
