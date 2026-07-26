import type { ShipmentStatus, UpdateShipmentStatusPayload } from '@/src/types/shipment';

const allowedTransitions: Record<ShipmentStatus, readonly ShipmentStatus[]> = {
  ASSIGNED: ['PICKED_UP', 'FAILED'],
  PICKED_UP: ['DELIVERING', 'FAILED'],
  DELIVERING: ['DELIVERED', 'FAILED'],
  DELIVERED: [],
  FAILED: [],
};

export function getAllowedShipmentTransitions(status: ShipmentStatus): readonly ShipmentStatus[] {
  return allowedTransitions[status];
}

export function isShipmentTransitionAllowed(from: ShipmentStatus, to: ShipmentStatus): boolean {
  return getAllowedShipmentTransitions(from).includes(to);
}

export function prepareShipmentStatusUpdate(
  currentStatus: ShipmentStatus,
  nextStatus: ShipmentStatus,
  failureReason: string,
): UpdateShipmentStatusPayload {
  if (!isShipmentTransitionAllowed(currentStatus, nextStatus)) {
    throw new Error('Trang thai chuyen giao khong duoc phep chuyen theo buoc nay.');
  }

  const trimmedFailureReason = failureReason.trim();
  if (nextStatus === 'FAILED' && !trimmedFailureReason) {
    throw new Error('Nhap ly do that bai truoc khi gui cap nhat');
  }

  return {
    status: nextStatus,
    failureReason: nextStatus === 'FAILED' ? trimmedFailureReason : null,
  };
}

export function requiresShipmentStatusConfirmation(status: ShipmentStatus): boolean {
  return status === 'DELIVERED' || status === 'FAILED';
}
