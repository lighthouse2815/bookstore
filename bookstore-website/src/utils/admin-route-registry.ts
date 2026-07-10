import {
  ArrowLeftRight,
  BarChart3,
  BellRing,
  BookOpen,
  Boxes,
  Building2,
  ClipboardList,
  FileText,
  Key,
  MessageSquareMore,
  PackagePlus,
  Percent,
  Settings2,
  Shield,
  ShoppingCart,
  Star,
  Tags,
  Truck,
  User,
  Users,
} from 'lucide-react'
import type {
  AdminRouteDefinition,
  AdminRouteItem,
  TranslationFn,
} from '@/types/admin-command'
import type { UserRole } from '@/types/auth'

type GetAdminRouteItemsOptions = {
  pathname: string
  roles: UserRole[]
  t: TranslationFn
}

export const ADMIN_ROUTE_DEFINITIONS: AdminRouteDefinition[] = [
  {
    id: 'dashboard',
    labelKey: 'common.dashboard',
    href: '/admin',
    activeHrefs: ['/admin', '/admin/dashboard'],
    icon: BarChart3,
    allowedRoles: ['ADMIN'],
    keywords: ['dashboard', 'tong quan', 'bao cao', 'overview'],
  },
  {
    id: 'audit-logs',
    labelKey: 'admin.sidebar.auditLogs',
    href: '/admin/audit-logs',
    icon: ClipboardList,
    allowedRoles: ['ADMIN', 'STAFF'],
    keywords: ['audit', 'logs', 'nhat ky', 'lich su'],
  },
  {
    id: 'reports',
    labelKey: 'admin.sidebar.reports',
    href: '/admin/reports',
    icon: BarChart3,
    allowedRoles: ['ADMIN'],
    keywords: ['reports', 'bao cao', 'csv', 'exports'],
  },
  {
    id: 'books',
    labelKey: 'admin.sidebar.books',
    href: '/admin/books',
    icon: BookOpen,
    allowedRoles: ['ADMIN'],
    keywords: ['books', 'sach', 'catalog', 'catalogue'],
  },
  {
    id: 'digital-assets',
    labelKey: 'admin.sidebar.digitalAssets',
    href: '/admin/digital-assets',
    icon: FileText,
    allowedRoles: ['ADMIN'],
    keywords: ['digital', 'ebook', 'tai san so', 'assets'],
  },
  {
    id: 'import-receipts',
    labelKey: 'admin.sidebar.importReceipts',
    href: '/admin/import-receipts',
    icon: PackagePlus,
    allowedRoles: ['ADMIN'],
    keywords: ['import', 'receipts', 'nhap kho', 'warehouse'],
  },
  {
    id: 'inventory',
    labelKey: 'admin.sidebar.inventory',
    href: '/admin/inventory',
    icon: Boxes,
    allowedRoles: ['ADMIN'],
    keywords: ['inventory', 'stock', 'ton kho'],
  },
  {
    id: 'orders',
    labelKey: 'admin.sidebar.orders',
    href: '/admin/orders',
    icon: ShoppingCart,
    allowedRoles: ['ADMIN'],
    keywords: ['orders', 'don hang', 'checkout'],
  },
  {
    id: 'return-requests',
    labelKey: 'admin.sidebar.returnRequests',
    href: '/admin/return-requests',
    icon: ArrowLeftRight,
    allowedRoles: ['ADMIN'],
    keywords: ['returns', 'refund', 'tra hang', 'hoan tien'],
  },
  {
    id: 'shipments',
    labelKey: 'admin.sidebar.shipments',
    href: '/admin/shipments',
    icon: Truck,
    allowedRoles: ['ADMIN'],
    keywords: ['shipments', 'shipping', 'giao hang', 'delivery'],
  },
  {
    id: 'reviews',
    labelKey: 'admin.sidebar.reviews',
    href: '/admin/reviews',
    icon: Star,
    allowedRoles: ['ADMIN'],
    keywords: ['reviews', 'danh gia', 'ratings'],
  },
  {
    id: 'notifications',
    labelKey: 'admin.sidebar.notifications',
    href: '/admin/notifications',
    icon: BellRing,
    allowedRoles: ['ADMIN'],
    keywords: ['notifications', 'thong bao', 'broadcast'],
  },
  {
    id: 'chat',
    labelKey: 'admin.sidebar.chat',
    href: '/admin/chat',
    icon: MessageSquareMore,
    allowedRoles: ['ADMIN', 'STAFF'],
    keywords: ['chat', 'support', 'ho tro', 'messages'],
  },
  {
    id: 'categories',
    labelKey: 'admin.sidebar.categories',
    href: '/admin/categories',
    icon: Tags,
    allowedRoles: ['ADMIN'],
    keywords: ['categories', 'danh muc'],
  },
  {
    id: 'authors',
    labelKey: 'admin.sidebar.authors',
    href: '/admin/authors',
    icon: User,
    allowedRoles: ['ADMIN'],
    keywords: ['authors', 'tac gia'],
  },
  {
    id: 'publishers',
    labelKey: 'admin.sidebar.publishers',
    href: '/admin/publishers',
    icon: Building2,
    allowedRoles: ['ADMIN'],
    keywords: ['publishers', 'nha xuat ban'],
  },
  {
    id: 'suppliers',
    labelKey: 'admin.sidebar.suppliers',
    href: '/admin/suppliers',
    icon: Truck,
    allowedRoles: ['ADMIN'],
    keywords: ['suppliers', 'nha cung cap'],
  },
  {
    id: 'customers',
    labelKey: 'admin.sidebar.customers',
    href: '/admin/customers',
    activeHrefs: ['/admin/customers', '/admin/users'],
    icon: Users,
    allowedRoles: ['ADMIN'],
    keywords: ['customers', 'khach hang', 'users', 'nguoi dung'],
  },
  {
    id: 'staff',
    labelKey: 'admin.sidebar.staff',
    href: '/admin/staff',
    icon: User,
    allowedRoles: ['ADMIN'],
    keywords: ['staff', 'nhan vien', 'admin users'],
  },
  {
    id: 'roles',
    labelKey: 'admin.sidebar.roles',
    href: '/admin/roles',
    icon: Shield,
    allowedRoles: ['ADMIN'],
    keywords: ['roles', 'vai tro'],
  },
  {
    id: 'permissions',
    labelKey: 'admin.sidebar.permissions',
    href: '/admin/permissions',
    icon: Key,
    allowedRoles: ['ADMIN'],
    keywords: ['permissions', 'quyen', 'rbac'],
  },
  {
    id: 'promotions',
    labelKey: 'admin.sidebar.promotions',
    href: '/admin/promotions',
    icon: Percent,
    allowedRoles: ['ADMIN'],
    keywords: ['promotions', 'khuyen mai', 'coupon'],
  },
  {
    id: 'references',
    labelKey: 'admin.sidebar.references',
    href: '/admin/references',
    icon: FileText,
    allowedRoles: ['ADMIN'],
    keywords: ['references', 'tham chieu', 'master data'],
    showInSidebar: false,
  },
  {
    id: 'settings',
    labelKey: 'admin.sidebar.settings',
    href: '/admin/settings',
    icon: Settings2,
    allowedRoles: ['ADMIN'],
    keywords: ['settings', 'cai dat', 'preferences'],
  },
]

export function getAdminPaletteRoutes({
  pathname,
  roles,
  t,
}: GetAdminRouteItemsOptions): AdminRouteItem[] {
  return getMappedRoutes({ pathname, roles, t })
}

export function getAdminSidebarRoutes({
  pathname,
  roles,
  t,
}: GetAdminRouteItemsOptions): AdminRouteItem[] {
  return getMappedRoutes({ pathname, roles, t }).filter(
    (route) => route.showInSidebar !== false,
  )
}

function getMappedRoutes({
  pathname,
  roles,
  t,
}: GetAdminRouteItemsOptions): AdminRouteItem[] {
  return ADMIN_ROUTE_DEFINITIONS.filter((route) =>
    canAccessAdminRoute(route.allowedRoles, roles),
  ).map((route) => ({
    ...route,
    label: t(route.labelKey),
    isActive: isAdminRouteActive(pathname, route),
  }))
}

function canAccessAdminRoute(allowedRoles: UserRole[], roles: UserRole[]) {
  return allowedRoles.some((role) => roles.includes(role))
}

function isAdminRouteActive(pathname: string, route: AdminRouteDefinition) {
  const activeHrefs = route.activeHrefs ?? [route.href]
  return activeHrefs.includes(pathname)
}
