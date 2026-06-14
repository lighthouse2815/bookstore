import type {
  OrderStatus,
  OrderPaymentMethod,
  OrderPaymentStatus,
} from '@/types/order'
import type { Gender, UserRole } from '@/types/auth'

type TranslateFunction = (
  key: string,
  params?: Record<string, number | string>,
) => string

const categoryKeys: Record<string, string> = {
  novel: 'categories.novel',
  'tieu thuyet': 'categories.novel',
  'tiểu thuyết': 'categories.novel',
  'life skills': 'categories.lifeSkills',
  'ky nang song': 'categories.lifeSkills',
  'kỹ năng sống': 'categories.lifeSkills',
  science: 'categories.science',
  'khoa hoc': 'categories.science',
  'khoa học': 'categories.science',
  literature: 'categories.literature',
  'van hoc': 'categories.literature',
  'văn học': 'categories.literature',
  'science fiction': 'categories.sciFi',
  'sci-fi': 'categories.sciFi',
  scifi: 'categories.sciFi',
  'vien tuong': 'categories.sciFi',
  'viễn tưởng': 'categories.sciFi',
}

const orderStatusKeys: Record<string, string> = {
  PENDING: 'orderStatus.PENDING',
  CONFIRMED: 'orderStatus.CONFIRMED',
  SHIPPING: 'orderStatus.SHIPPING',
  DELIVERED: 'orderStatus.DELIVERED',
  CANCELLED: 'orderStatus.CANCELLED',
  pending: 'orderStatus.pending',
  processing: 'orderStatus.processing',
  shipped: 'orderStatus.shipped',
  delivered: 'orderStatus.delivered',
  cancelled: 'orderStatus.cancelled',
}

const paymentMethodKeys: Record<OrderPaymentMethod, string> = {
  BANK_TRANSFER_QR: 'paymentMethods.BANK_TRANSFER_QR',
  COD: 'paymentMethods.COD',
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

const roleKeys: Record<UserRole, string> = {
  ADMIN: 'roles.ADMIN',
  STAFF: 'roles.STAFF',
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
    .toLowerCase()
    .trim()
}
