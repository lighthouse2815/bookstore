import type { RequestOptions } from '@/src/types/api';
import type { Shipment, UpdateShipmentStatusPayload } from '@/src/types/shipment';

export interface RequestFn {
  <T>(path: string, options?: RequestOptions): Promise<T>;
}

export function fetchMyShipments(request: RequestFn) {
  return request<Shipment[]>('/shipper/shipments/my');
}

export function fetchShipmentById(request: RequestFn, shipmentId: string) {
  return request<Shipment>(`/shipper/shipments/${shipmentId}`);
}

export function updateShipmentStatus(
  request: RequestFn,
  shipmentId: string,
  payload: UpdateShipmentStatusPayload,
) {
  return request<Shipment>(`/shipper/shipments/${shipmentId}/status`, {
    method: 'PUT',
    json: payload,
  });
}
