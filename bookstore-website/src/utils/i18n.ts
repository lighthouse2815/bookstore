import type {
  OrderStatus,
  OrderPaymentMethod,
  OrderPaymentStatus,
} from '@/types/order'
import type { Gender, UserRole } from '@/types/auth'
import type { ShipmentStatus } from '@/types/shipment'
import type { ReturnRequestStatus } from '@/types/return-request'
import type {
  DigitalAccessStatus,
  DigitalAccessType,
  DigitalAssetFormat,
} from '@/types/digital-library'
import type { AppLanguage } from '@/locales/messages'
import type { LocalizedCategory } from '@/types/book'

type TranslateFunction = (
  key: string,
  params?: Record<string, number | string>,
) => string

const digitalAccessStatusKeys: Record<DigitalAccessStatus, string> = {
  ACTIVE: 'library.accessStatus.ACTIVE',
  EXPIRED: 'library.accessStatus.EXPIRED',
  REVOKED: 'library.accessStatus.REVOKED',
}

const digitalAccessTypeKeys: Record<DigitalAccessType, string> = {
  PURCHASED: 'library.accessType.PURCHASED',
  BORROWED: 'library.accessType.BORROWED',
  SUBSCRIPTION: 'library.accessType.SUBSCRIPTION',
}

const digitalAssetFormatKeys: Record<DigitalAssetFormat, string> = {
  PDF: 'library.format.PDF',
  EPUB: 'library.format.EPUB',
  AUDIO: 'library.format.AUDIO',
}

const orderStatusKeys: Record<string, string> = {
  PENDING: 'orderStatus.PENDING',
  CONFIRMED: 'orderStatus.CONFIRMED',
  SHIPPING: 'orderStatus.SHIPPING',
  DELIVERED: 'orderStatus.DELIVERED',
  CANCELLED: 'orderStatus.CANCELLED',
  APPROVED: 'returnRequests.status.APPROVED',
  REJECTED: 'returnRequests.status.REJECTED',
  pending: 'orderStatus.pending',
  processing: 'orderStatus.processing',
  shipped: 'orderStatus.shipped',
  delivered: 'orderStatus.delivered',
  cancelled: 'orderStatus.cancelled',
}

const returnRequestStatusKeys: Record<ReturnRequestStatus, string> = {
  PENDING: 'returnRequests.status.PENDING',
  APPROVED: 'returnRequests.status.APPROVED',
  REJECTED: 'returnRequests.status.REJECTED',
  CANCELLED: 'returnRequests.status.CANCELLED',
}

const paymentMethodKeys: Record<OrderPaymentMethod, string> = {
  BANK_TRANSFER_QR: 'paymentMethods.BANK_TRANSFER_QR',
  COD: 'paymentMethods.COD',
  CASH: 'paymentMethods.CASH',
  BANK_TRANSFER: 'paymentMethods.BANK_TRANSFER',
  VNPAY: 'paymentMethods.VNPAY',
  MOMO: 'paymentMethods.MOMO',
}

const paymentStatusKeys: Record<OrderPaymentStatus, string> = {
  PENDING: 'paymentStatus.PENDING',
  UNPAID: 'paymentStatus.UNPAID',
  PAID: 'paymentStatus.PAID',
  FAILED: 'paymentStatus.FAILED',
  CANCELLED: 'paymentStatus.CANCELLED',
  REFUNDED: 'paymentStatus.REFUNDED',
  EXPIRED: 'paymentStatus.EXPIRED',
}

const shipmentStatusKeys: Record<ShipmentStatus, string> = {
  ASSIGNED: 'shipmentStatus.ASSIGNED',
  PICKED_UP: 'shipmentStatus.PICKED_UP',
  DELIVERING: 'shipmentStatus.DELIVERING',
  DELIVERED: 'shipmentStatus.DELIVERED',
  FAILED: 'shipmentStatus.FAILED',
}

const roleKeys: Record<UserRole, string> = {
  ADMIN: 'roles.ADMIN',
  STAFF: 'roles.STAFF',
  SHIPPER: 'roles.SHIPPER',
  USER: 'roles.USER',
}

const genderKeys: Record<Gender, string> = {
  MALE: 'genders.MALE',
  FEMALE: 'genders.FEMALE',
  OTHER: 'genders.OTHER',
}

export function getCategoryLabel(
  category: LocalizedCategory | string | null | undefined,
  language: AppLanguage,
  fallback = '',
) {
  if (!category) {
    return fallback
  }
  if (typeof category === 'string') {
    return category.trim() || fallback
  }

  return category.translations?.[language]?.name?.trim() || category.name || fallback
}

export function getCategoryDescription(
  category: (LocalizedCategory & { description?: string | null }) | null | undefined,
  language: AppLanguage,
) {
  if (!category) {
    return null
  }
  return (
    category.translations?.[language]?.description?.trim() ||
    category.description?.trim() ||
    null
  )
}

export function getDigitalAccessStatusLabel(
  status: DigitalAccessStatus,
  t: TranslateFunction,
) {
  return t(digitalAccessStatusKeys[status])
}

export function getDigitalAccessTypeLabel(
  accessType: DigitalAccessType,
  t: TranslateFunction,
) {
  return t(digitalAccessTypeKeys[accessType])
}

export function getDigitalAssetFormatLabel(
  format: DigitalAssetFormat,
  t: TranslateFunction,
) {
  return t(digitalAssetFormatKeys[format])
}

export function getOrderStatusLabel(status: string, t: TranslateFunction) {
  const key = orderStatusKeys[status]
  return key ? t(key) : status
}

export function getPaymentMethodLabel(
  paymentMethod: OrderPaymentMethod,
  t: TranslateFunction,
) {
  return t(paymentMethodKeys[paymentMethod])
}

export function getPaymentStatusLabel(
  paymentStatus: OrderPaymentStatus,
  t: TranslateFunction,
) {
  return t(paymentStatusKeys[paymentStatus])
}

export function getShipmentStatusLabel(
  shipmentStatus: ShipmentStatus,
  t: TranslateFunction,
) {
  return t(shipmentStatusKeys[shipmentStatus])
}

export function getReturnRequestStatusLabel(
  status: ReturnRequestStatus,
  t: TranslateFunction,
) {
  return t(returnRequestStatusKeys[status])
}

export function getUserRoleLabel(role: UserRole, t: TranslateFunction) {
  return t(roleKeys[role])
}

export function getGenderLabel(gender: Gender, t: TranslateFunction) {
  return t(genderKeys[gender])
}
