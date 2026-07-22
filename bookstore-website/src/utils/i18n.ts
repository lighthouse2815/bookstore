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

type TranslateFunction = (
  key: string,
  params?: Record<string, number | string>,
) => string

const categoryKeys: Record<string, string> = {
  novel: 'categories.novel',
  'tieu thuyet': 'categories.novel',
  'life skills': 'categories.lifeSkills',
  'ky nang song': 'categories.lifeSkills',
  'ky nang & phat trien ban than': 'categories.personalDevelopment',
  'ky nang va phat trien ban than': 'categories.personalDevelopment',
  'personal development': 'categories.personalDevelopment',
  'business & management': 'categories.businessManagement',
  'business and management': 'categories.businessManagement',
  'kinh doanh & quan tri': 'categories.businessManagement',
  'kinh doanh va quan tri': 'categories.businessManagement',
  'art & creativity': 'categories.artsCreativity',
  'arts & creativity': 'categories.artsCreativity',
  'nghe thuat & sang tao': 'categories.artsCreativity',
  'nghe thuat va sang tao': 'categories.artsCreativity',
  philosophy: 'categories.philosophy',
  'triet hoc': 'categories.philosophy',
  'contemporary literature': 'categories.contemporaryLiterature',
  'van hoc duong dai': 'categories.contemporaryLiterature',
  mystery: 'categories.mystery',
  detective: 'categories.mystery',
  'trinh tham': 'categories.mystery',
  education: 'categories.education',
  'giao duc': 'categories.education',
  science: 'categories.science',
  'khoa hoc': 'categories.science',
  'science & technology': 'categories.scienceTechnology',
  'science and technology': 'categories.scienceTechnology',
  'khoa hoc & cong nghe': 'categories.scienceTechnology',
  'khoa hoc va cong nghe': 'categories.scienceTechnology',
  literature: 'categories.literature',
  'van hoc': 'categories.literature',
  'science fiction': 'categories.sciFi',
  'sci-fi': 'categories.sciFi',
  scifi: 'categories.sciFi',
  'vien tuong': 'categories.sciFi',
  'khoa hoc vien tuong': 'categories.sciFi',
  psychology: 'categories.psychology',
  'tam ly hoc': 'categories.psychology',
  'history & memoir': 'categories.historyMemoir',
  'history and memoir': 'categories.historyMemoir',
  'lich su & hoi ky': 'categories.historyMemoir',
  'lich su va hoi ky': 'categories.historyMemoir',
  children: 'categories.children',
  kids: 'categories.children',
  'thieu nhi': 'categories.children',
  fantasy: 'categories.fantasy',
  'gia tuong & ky ao': 'categories.fantasy',
  'gia tuong va ky ao': 'categories.fantasy',
}

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

export function getCategoryLabel(category: string, t: TranslateFunction) {
  if (category.trim() === '') {
    return t('book.fallback.category')
  }

  const normalizedCategory = normalizeCategoryKey(category)
  const key = categoryKeys[normalizedCategory]
  return key ? t(key) : category
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

function normalizeCategoryKey(category: string) {
  return category
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'D')
    .toLowerCase()
    .replace(/\s+/g, ' ')
    .trim()
}
