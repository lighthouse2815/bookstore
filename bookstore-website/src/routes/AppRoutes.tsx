import { lazy, Suspense, useEffect, type ReactNode } from 'react'
import { BrowserRouter, Route, Routes, useLocation } from 'react-router-dom'
import HomePage from '@/pages/home/home'
import BooksPage from '@/pages/book/books'
import BookDetailPage from '@/pages/book/book-detail'
import CartPage from '@/pages/cart/cart'
import CheckoutPage from '@/pages/cart/checkout'
import NotFoundPage from '@/pages/home/not-found'
import ShippingPolicyPage from '@/pages/support/shipping-policy'
import ReturnsRefundsPage from '@/pages/support/returns-refunds'
import FaqPage from '@/pages/support/faq'
import ContactPage from '@/pages/support/contact'
import OrderConfirmationPage from '@/pages/order/order-confirmation'
import MyOrdersPage from '@/pages/order/my-orders'
import OrderDetailPage from '@/pages/order/order-detail'
import LoginPage from '@/pages/auth/login'
import RegisterPage from '@/pages/auth/register'
import ProfilePage from '@/pages/auth/profile'
import { useLanguage } from '@/contexts/language-context'
import { ProtectedRoute } from './protected-route'

const AdminDashboard = lazy(() => import('@/pages/admin/dashboard'))
const AdminBooksPage = lazy(() => import('@/pages/admin/books'))
const AdminOrdersPage = lazy(() => import('@/pages/admin/orders'))
const AdminCategoriesPage = lazy(() => import('@/pages/admin/categories'))
const AdminAuthorsPage = lazy(() => import('@/pages/admin/authors'))
const AdminPublishersPage = lazy(() => import('@/pages/admin/publishers'))
const AdminUsersPage = lazy(() => import('@/pages/admin/users'))
const AdminRolesPage = lazy(() => import('@/pages/admin/roles'))
const AdminPermissionsPage = lazy(() => import('@/pages/admin/permissions'))
const AdminReferencesPage = lazy(() => import('@/pages/admin/references'))

function ScrollToTop() {
  const { pathname } = useLocation()

  useEffect(() => {
    window.scrollTo(0, 0)
  }, [pathname])

  return null
}

function RouteLoading() {
  const { t } = useLanguage()

  return (
    <div className="flex min-h-[40vh] items-center justify-center px-4 py-12">
      <p className="text-sm text-muted-foreground">{t('common.loading')}</p>
    </div>
  )
}

function LazyAdminPage({ children }: { children: ReactNode }) {
  return <Suspense fallback={<RouteLoading />}>{children}</Suspense>
}

function AppRouteContent() {
  return (
    <>
      <ScrollToTop />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/books" element={<BooksPage />} />
        <Route path="/books/:id" element={<BookDetailPage />} />
        <Route path="/shipping-policy" element={<ShippingPolicyPage />} />
        <Route path="/returns-refunds" element={<ReturnsRefundsPage />} />
        <Route path="/faq" element={<FaqPage />} />
        <Route path="/contact" element={<ContactPage />} />

        <Route
          path="/cart"
          element={
            <ProtectedRoute>
              <CartPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/checkout"
          element={
            <ProtectedRoute>
              <CheckoutPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/orders"
          element={
            <ProtectedRoute>
              <MyOrdersPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/orders/:id"
          element={
            <ProtectedRoute>
              <OrderDetailPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/order-confirmation"
          element={
            <ProtectedRoute>
              <OrderConfirmationPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyAdminPage>
                <AdminDashboard />
              </LazyAdminPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/books"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyAdminPage>
                <AdminBooksPage />
              </LazyAdminPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/orders"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyAdminPage>
                <AdminOrdersPage />
              </LazyAdminPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/categories"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyAdminPage>
                <AdminCategoriesPage />
              </LazyAdminPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/authors"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyAdminPage>
                <AdminAuthorsPage />
              </LazyAdminPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/publishers"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyAdminPage>
                <AdminPublishersPage />
              </LazyAdminPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/users"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyAdminPage>
                <AdminUsersPage />
              </LazyAdminPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/roles"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyAdminPage>
                <AdminRolesPage />
              </LazyAdminPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/permissions"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyAdminPage>
                <AdminPermissionsPage />
              </LazyAdminPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/references"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyAdminPage>
                <AdminReferencesPage />
              </LazyAdminPage>
            </ProtectedRoute>
          }
        />

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </>
  )
}

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <AppRouteContent />
    </BrowserRouter>
  )
}
