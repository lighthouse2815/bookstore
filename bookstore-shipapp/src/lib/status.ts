import { palette } from '@/src/config';
import type { ShipmentStatus } from '@/src/types/shipment';

export const shipmentStatusMeta: Record<
  ShipmentStatus,
  { label: string; tone: string; soft: string; icon: string }
> = {
  ASSIGNED: {
    label: 'Da giao ship',
    tone: '#1D4ED8',
    soft: '#DBEAFE',
    icon: 'clipboard-check-outline',
  },
  PICKED_UP: {
    label: 'Da lay hang',
    tone: palette.accent,
    soft: '#FDE8C8',
    icon: 'package-variant-closed',
  },
  DELIVERING: {
    label: 'Dang giao',
    tone: palette.primary,
    soft: '#CCFBF1',
    icon: 'truck-delivery-outline',
  },
  DELIVERED: {
    label: 'Da giao xong',
    tone: palette.success,
    soft: '#DCFCE7',
    icon: 'check-decagram-outline',
  },
  FAILED: {
    label: 'Giao that bai',
    tone: palette.danger,
    soft: '#FEE2E2',
    icon: 'alert-circle-outline',
  },
};

export function getNextShipmentActions(status: ShipmentStatus) {
  switch (status) {
    case 'ASSIGNED':
      return [
        { status: 'PICKED_UP' as const, label: 'Lay hang', icon: 'package-variant-closed' },
        { status: 'FAILED' as const, label: 'Bao that bai', icon: 'alert-circle-outline' },
      ];
    case 'PICKED_UP':
      return [
        { status: 'DELIVERING' as const, label: 'Bat dau giao', icon: 'truck-fast-outline' },
        { status: 'FAILED' as const, label: 'Bao that bai', icon: 'alert-circle-outline' },
      ];
    case 'DELIVERING':
      return [
        { status: 'DELIVERED' as const, label: 'Giao thanh cong', icon: 'check-decagram-outline' },
        { status: 'FAILED' as const, label: 'Bao that bai', icon: 'alert-circle-outline' },
      ];
    default:
      return [];
  }
}
