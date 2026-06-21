export type ShipmentStatus = 'ASSIGNED' | 'PICKED_UP' | 'DELIVERING' | 'DELIVERED' | 'FAILED';

export interface Shipment {
  shipmentId: string;
  orderId: string;
  orderCode: string;
  shipperId: string;
  paymentMethod: string;
  paymentStatus: string;
  orderStatus: string;
  shipmentStatus: ShipmentStatus;
  totalAmount: number | string;
  finalAmount: number | string;
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  failureReason: string | null;
  assignedAt: string;
  updatedAt: string;
  pickedUpAt: string | null;
  deliveringAt: string | null;
  deliveredAt: string | null;
  failedAt: string | null;
}

export interface UpdateShipmentStatusPayload {
  status: ShipmentStatus;
  failureReason?: string | null;
}

export type ShipmentFilter = ShipmentStatus | 'ALL';
